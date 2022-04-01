package org.swasth.hcx.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.objenesis.ObjenesisHelper;
import org.springframework.util.ResourceUtils;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.dto.*;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.exception.ServerException;
import org.swasth.hcx.exception.ServiceUnavailbleException;
import org.swasth.hcx.utils.JSONUtils;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.service.HeaderAuditService;
import org.swasth.jose.jwe.JweRequest;
import org.swasth.jose.jwe.key.PrivateKeyLoader;
import org.swasth.jose.jwe.key.PublicKeyLoader;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static org.swasth.hcx.utils.Constants.*;

public class BaseController {

    @Autowired
    private EventGenerator eventGenerator;

    @Autowired
    protected Environment env;

    @Autowired
    protected HeaderAuditService auditService;

    private String baseURL;

    private RSAPrivateKey rsaPrivateKey;

    @Value("${hcx_application.url}")
    private String hcxBasePath;

    private String onCheckPayloadType;


    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }
    public static String getRandomChestItem(List<String> items) {
        return items.get(new Random().nextInt(items.size()));
    }

    protected void processAndSendEvent(String apiAction, String metadataTopic, Request request) throws Exception {
        String mid = UUID.randomUUID().toString();
        String serviceMode = env.getProperty(SERVICE_MODE);
        String payloadTopic = env.getProperty(KAFKA_TOPIC_PAYLOAD);
        String key = request.getSenderCode();
        String payloadEvent = eventGenerator.generatePayloadEvent(mid, request);
        String metadataEvent = eventGenerator.generateMetadataEvent(mid, apiAction, request);
        System.out.println("Mode: " + serviceMode + " :: mid: " + mid + " :: Event: " + metadataEvent);
        if(StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
            System.out.println("decoding the request");

        }
    }

    protected void sendOnAction(String onApiCall, Map<String,?> encryptedPayload) throws Exception{
        HttpResponse<String> response = Unirest.post("http://a9dd63de91ee94d59847a1225da8b111-273954130.ap-south-1.elb.amazonaws.com:8080/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token")
                .header("content-type", "application/x-www-form-urlencoded")
                .field("client_id", "registry-frontend")
                .field("username", "swasth_mock_payer@swasthapp.org")
                .field("password", "Opensaber@123")
                .field("grant_type", "password")
                .asString();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseBody = mapper.readValue(response.getBody(), Map.class);

        HttpResponse<String> onActionResponse = Unirest.post(hcxBasePath + onApiCall)
                .header("Authorization", "Bearer " + responseBody.get("access_token").toString())
                .header("Content-Type", "application/json")
                .body(encryptedPayload)
                .asString();

        Map<String, String> res = mapper.readValue(onActionResponse.getBody(), Map.class);
        System.out.println("response "+ res);
    }


    protected Map<String, String> encryptPayload(String filePath, Map<String, Object> headers, Map<String, Object> payload) throws Exception{
        Map<String, String> encryptedObject;
        //File file = new File(filePath);
        InputStream io = getFileAsIOStream(filePath);
        Reader fileReader = new InputStreamReader(io);
        RSAPublicKey rsaPublicKey = PublicKeyLoader.loadPublicKeyFromX509Certificate(fileReader);
        JweRequest jweRequest = new JweRequest(headers, payload);
        jweRequest.encryptRequest(rsaPublicKey);
        encryptedObject = jweRequest.getEncryptedObject();
        return encryptedObject;

    }

    protected Map<String, Object> decryptPayload(String filePath, Map<String, String> payload) throws Exception{
        FileReader fileReader = new FileReader(new File(filePath));
        PemReader pemReader = new PemReader(fileReader);
        PemObject pemObject = pemReader.readPemObject();
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
        KeyFactory factory = KeyFactory.getInstance("RSA");
        rsaPrivateKey = (RSAPrivateKey) factory.generatePrivate(privateKeySpec);
        JweRequest jweRequest = new JweRequest(payload);
        jweRequest.decryptRequest(rsaPrivateKey);
        Map<String, Object> retrievedHeader = jweRequest.getHeaders();
        Map<String, Object> retrievedPayload = jweRequest.getPayload();
        Map<String, Object> returnObj = new HashMap<>();
        returnObj.put("headers",retrievedHeader);
        returnObj.put("payload",retrievedPayload);
        return returnObj;
    }

    protected Map<String, Object> createOnActionHeaders(Map<String, Object> headers) throws Exception{
        Map<String, Object> returnHeaders =  new HashMap<>();
        DateTime currentTime = DateTime.now();
        returnHeaders.putAll(headers);
        System.out.println("headers before" + headers);
        returnHeaders.put("x-hcx-sender_code",headers.get("x-hcx-recipient_code"));
        returnHeaders.put("x-hcx-recipient_code",headers.get("x-hcx-sender_code"));
        returnHeaders.put("x-hcx-api_call_id", UUID.randomUUID().toString());
        returnHeaders.put("x-hcx-timestamp",currentTime.toString());
        returnHeaders.put("x-hcx-status", Constants.COMPLETE_STATUS);
        onCheckPayloadType = "jweResponse";

        if(headers.containsKey("x-hcx-test_random") == true){
            returnHeaders.remove("x-hcx-test_random");
            returnHeaders.put("x-hcx-status", getRandomChestItem(Constants.STATUS_RESPONSES));
            if(returnHeaders.get("x-hcx-status") == Constants.COMPLETE_STATUS || returnHeaders.get("x-hcx-status") == PARTIAL_STATUS) {
                onCheckPayloadType = "jweResponse";
            }else if(returnHeaders.get("x-hcx-status") == Constants.ERROR_STATUS){
                Map<String,String> error = new HashMap<>();
                error.put("code",getRandomChestItem(Constants.RECIPIENT_ERROR_CODES));
                error.put("message","Error encountered");
                error.put("trace",null);
                returnHeaders.put("x-hcx-error_details",error);
                onCheckPayloadType = "protocolResponse";
            }else if(returnHeaders.get("x-hcx-status") == Constants.REDIRECT_STATUS){
                returnHeaders.put("x-hcx-redirect_to",UUID.randomUUID().toString());
                onCheckPayloadType = "protocolResponse";
            }
        }else {
            if (headers.containsKey("x-hcx-debug_flag_test")) {
                returnHeaders.put("x-hcx-debug_flag", headers.get("x-hcx-debug_flag_test"));
                returnHeaders.remove("x-hcx-debug_flag_test");
                onCheckPayloadType = "protocolResponse";
            }
            if (headers.containsKey("x-hcx-status_test")) {
                returnHeaders.put("x-hcx-status", headers.get("x-hcx-status_test"));
                returnHeaders.remove("x-hcx-status_test");
                onCheckPayloadType = "protocolResponse";
            }
            if (headers.containsKey("x-hcx-error_details_test")) {
                returnHeaders.put("x-hcx-error_details", headers.get("x-hcx-error_details_test"));
                returnHeaders.remove("x-hcx-error_details_test");
                returnHeaders.put("x-hcx-status", ERROR_STATUS);
                onCheckPayloadType = "protocolResponse";
            }
            if (headers.containsKey("x-hcx-debug_details_test")) {
                returnHeaders.put("x-hcx-debug_details", headers.get("x-hcx-debug_details_test"));
                returnHeaders.remove("x-hcx-debug_details_test");
                onCheckPayloadType = "protocolResponse";
            }
        }
        return returnHeaders;
    }

    private InputStream getFileAsIOStream(final String fileName)
    {
        InputStream ioStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }

    protected void processAndValidate(String onApiAction, String metadataTopic, Request request, Map<String, Object> requestBody) throws Exception {
        String mid = UUID.randomUUID().toString();
        String serviceMode = env.getProperty(SERVICE_MODE);
        System.out.println("Mode: " + serviceMode + " :: mid: " + mid + " :: Event: " + onApiAction);
        if(StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
            ClassLoader classLoader = this.getClass().getClassLoader();
            baseURL = classLoader.getResource("").getFile();
            String publicKeyPath  =  "key/x509-self-signed-certificate.pem";
            String privateKeyPath =  "key/x509-private-key.pem";

            System.out.println("create the oncheck payload");
            ObjectMapper mapper = new ObjectMapper();
            InputStream file = getFileAsIOStream("static/coverage_eligibility_oncheck.json");
            Map<String, Object> map = mapper.readValue(file, Map.class);
            Map<String, Object> onHeaders = createOnActionHeaders(request.getHcxHeaders());
            //creating an on check payload
            System.out.println("onCheckPayloadType"+ onCheckPayloadType);
            if (onCheckPayloadType == "jweResponse") {
                Map<String, String> encryptedOnPayload = encryptPayload(publicKeyPath, onHeaders, map);
                sendOnAction(onApiAction,encryptedOnPayload);
                System.out.println("on check payload    " + encryptedOnPayload);
            }else{
                sendOnAction(onApiAction,onHeaders);
                System.out.println("on check payload    " + onHeaders);
            }
//            //creating a check payload for temp purpose
//            Map<String, Object> map1 = mapper.readValue(new File(baseURL+"static/coverage_eligibility_check.json"), Map.class);
//            Map<String,String> encryptedPayload1 = encryptPayload(publicKeyPath,request.getHcxHeaders(),map1);
//            System.out.println("check payload    " + encryptedPayload1);
        }
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction, String onApiAction, String kafkaTopic) {
        Response response = new Response();
        try {
            Request request = new Request(requestBody);

            setResponseParams(request, response);
            //processAndSendEvent(apiAction, kafkaTopic, request);
            processAndValidate(onApiAction, kafkaTopic, request, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    protected void setResponseParams(Request request, Response response){
        response.setCorrelationId(request.getCorrelationId());
        response.setApiCallId(request.getApiCallId());
    }

    protected ResponseEntity<Object> exceptionHandler(Response response, Exception e){
        if (e instanceof ClientException) {
            return new ResponseEntity<>(errorResponse(response, ((ClientException) e).getErrCode(), e), HttpStatus.BAD_REQUEST);
        } else if (e instanceof ServiceUnavailbleException) {
            return new ResponseEntity<>(errorResponse(response, ((ServiceUnavailbleException) e).getErrCode(), e), HttpStatus.SERVICE_UNAVAILABLE);
        } else if (e instanceof ServerException) {
            return new ResponseEntity<>(errorResponse(response, ((ServerException) e).getErrCode(), e), HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.INTERNAL_SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

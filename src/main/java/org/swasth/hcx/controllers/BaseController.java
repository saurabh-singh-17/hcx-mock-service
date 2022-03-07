package org.swasth.hcx.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
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
import org.swasth.common.dto.*;
import org.swasth.common.exception.ClientException;
import org.swasth.common.exception.ErrorCodes;
import org.swasth.common.exception.ServerException;
import org.swasth.common.exception.ServiceUnavailbleException;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.service.HeaderAuditService;
import org.swasth.jose.jwe.JweRequest;
import org.swasth.jose.jwe.key.PrivateKeyLoader;
import org.swasth.jose.jwe.key.PublicKeyLoader;
import org.swasth.kafka.client.IEventService;
import org.swasth.postgresql.IDatabaseService;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static org.swasth.common.utils.Constants.*;

public class BaseController {

    @Autowired
    private EventGenerator eventGenerator;

    @Autowired
    protected Environment env;

    @Autowired
    protected HeaderAuditService auditService;

    private String baseURL;

    private RSAPrivateKey rsaPrivateKey;


    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
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


    protected Map<String, String> encryptPayload(String filePath, Map<String, Object> headers, Map<String, Object> payload) throws Exception{
        Map<String, String> encryptedObject;
        File file = new File(filePath);
        FileReader fileReader = new FileReader(file);
        RSAPublicKey rsaPublicKey = PublicKeyLoader.loadPublicKeyFromX509Certificate(fileReader);
        System.out.println("rsaPublicKey " + rsaPublicKey);
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
        System.out.println("currenttime" +  currentTime + " " + UUID.randomUUID().toString());
        returnHeaders = headers;
        returnHeaders.put("x-hcx-sender_code",headers.get("x-hcx-recipient_code"));
        returnHeaders.put("x-hcx-recipient_code",headers.get("x-hcx-recipient_code"));
        returnHeaders.put("x-hcx-api_call_id", UUID.randomUUID().toString());
        returnHeaders.put("x-hcx-timestamp",currentTime.toString());
        returnHeaders.put("x-hcx-status","response.complete");
        return returnHeaders;
    }

    protected void processAndValidate(String apiAction, String metadataTopic, Request request, Map<String, Object> requestBody) throws Exception {
        String mid = UUID.randomUUID().toString();
        String serviceMode = env.getProperty(SERVICE_MODE);
        String payloadTopic = env.getProperty(KAFKA_TOPIC_PAYLOAD);
        String key = request.getSenderCode();
        String payloadEvent = eventGenerator.generatePayloadEvent(mid, request);
        String metadataEvent = eventGenerator.generateMetadataEvent(mid, apiAction, request);
        System.out.println("Mode: " + serviceMode + " :: mid: " + mid + " :: Event: " + metadataEvent);
        if(StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
            ClassLoader classLoader = this.getClass().getClassLoader();
            baseURL = classLoader.getResource("").getFile();
            String publicKeyPath  = baseURL + "key/x509-self-signed-certificate.pem";
            String privateKeyPath = baseURL + "key/x509-private-key.pem";

            System.out.println("create the oncheck payload");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(new File(baseURL+"static/coverage_eligibility_oncheck.json"), Map.class);
            Map<String, Object> onHeaders = createOnActionHeaders(request.getHcxHeaders());
            Map<String,String> encryptedOnPayload = encryptPayload(publicKeyPath,onHeaders,map);
            System.out.println("on payload" +  encryptedOnPayload);


        }
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction, String onApiAction, String kafkaTopic) {
        Response response = new Response();
        try {
            Request request = new Request(requestBody);

            setResponseParams(request, response);
            System.out.println("set response params called");
            //processAndSendEvent(apiAction, kafkaTopic, request);
            processAndValidate(onApiAction, kafkaTopic, request, requestBody);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    protected void setResponseParams(Request request, Response response){
        System.out.println("response is start"+ request.getHcxHeaders());
        response.setCorrelationId(request.getCorrelationId());
        response.setApiCallId(request.getApiCallId());
        System.out.println("response is srt");
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

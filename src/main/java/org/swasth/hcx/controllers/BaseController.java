package org.swasth.hcx.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.dto.ResponseError;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.exception.ServerException;
import org.swasth.hcx.exception.ServiceUnavailbleException;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.service.HeaderAuditService;
import org.swasth.hcx.service.NotificationService;
import org.swasth.hcx.service.PayerService;
import org.swasth.hcx.utils.OnActionCall;
import org.swasth.jose.jwe.JweRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static org.swasth.hcx.utils.Constants.*;

public class BaseController {

    @Autowired
    private OnActionCall onActionCall;

    @Autowired
    private EventGenerator eventGenerator;

    @Autowired
    protected Environment env;

    @Autowired
    protected HeaderAuditService auditService;

    @Autowired
    protected NotificationService notificationService;

    private String baseURL;

    private RSAPrivateKey rsaPrivateKey;

    @Value("${hcx_application.url}")
    private String hcxBasePath;

    @Value("${autoresponse}")
    private Boolean autoResponse;

    @Autowired
    private PayerService payerService;

    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    protected InputStream getFileAsIOStream(final String fileName)
    {
        InputStream ioStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
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
            System.out.println("Process and send event");
        }
    }

    protected Map<String, Object> decryptPayload(String filePath, Map<String, String> payload) throws Exception{
        InputStream io = getFileAsIOStream(filePath);
        Reader fileReader = new InputStreamReader(io);
        PemReader pemReader = new PemReader(fileReader);
        PemObject pemObject = pemReader.readPemObject();
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
        KeyFactory factory = KeyFactory.getInstance("RSA");
        rsaPrivateKey = (RSAPrivateKey) factory.generatePrivate(privateKeySpec);
        JweRequest jweRequest = new JweRequest(payload);
        System.out.println("\n Decryption : JWE request made \n");
        jweRequest.decryptRequest(rsaPrivateKey);
        System.out.println("\n Decryption : JWE decryption done \n");
        Map<String, Object> retrievedHeader = jweRequest.getHeaders();
        Map<String, Object> retrievedPayload = jweRequest.getPayload();
        Map<String, Object> returnObj = new HashMap<>();
        returnObj.put("headers",retrievedHeader);
        returnObj.put("payload",retrievedPayload);
        System.out.println("decrypted payload " + returnObj);
        return returnObj;
    }

    protected void processAndValidate(String onApiAction, String metadataTopic, Request request, Map<String, Object> requestBody) throws Exception {
        String mid = UUID.randomUUID().toString();
        System.out.println("\n" + "mid: " + mid + " :: Event: " + onApiAction);
        Map<String, Object> map;
        ClassLoader classLoader = this.getClass().getClassLoader();
        baseURL = classLoader.getResource("").getFile();
        String publicKeyPath = "key/x509-self-signed-certificate.pem";
        String privateKeyPath = "key/x509-private-key.pem";
        Map<String, String> pay = new HashMap<>();
        System.out.println("payload received " + requestBody);
        pay.put("payload", String.valueOf(requestBody.get("payload")));
        Map<String, Object> decodedPayload = decryptPayload(privateKeyPath, pay);
        System.out.println("initial decryption done");
        if (request.getHcxHeaders().containsKey("x-hcx-get_object")) {
            try {
                map = (Map<String, Object>) request.getHcxHeaders().get("x-hcx-get_object");
            } catch (Exception e) {
                map = new ObjectMapper().readValue((String) request.getHcxHeaders().get("x-hcx-get_object"), HashMap.class);
            }
        } else {
            //checking for invalid encryption
            String name = "John Doe";
            String gender = "Male";
            Double money = Double.valueOf(100000);
            try {
                ArrayList<Object> entries = (ArrayList<Object>) ((Map) decodedPayload.get("payload")).get("entry");
                name = (String) ((Map) ((ArrayList<Object>) ((Map) ((Map) entries.get(2)).get("resource")).get("name")).get(0)).get("text");
                gender = (String) ((Map) ((Map) entries.get(2)).get("resource")).get("gender");
                if (CLAIM_ONSUBMIT.equalsIgnoreCase(onApiAction) || PRE_AUTH_ONSUBMIT.equalsIgnoreCase(onApiAction)) {
                    System.out.println("money" + money);
                    //System.out.println("extracted money" + ((Map) ((Map) ((Map) entries.get(3)).get("resource")).get("total")).get("value"));
                    try {
                        money = Double.parseDouble(String.valueOf(((Map) ((Map) ((Map) entries.get(3)).get("resource")).get("total")).get("value")));
                    } catch (NullPointerException e) {
                        System.out.println("could not feteh money from payload");
                    }
                    System.out.println("money" + money);
                }
                System.out.println("decryption successful" + name + gender);
            } catch (Exception e) {
                System.out.println("Could not get the name and money from the input" + e.getMessage().toString());
                //throw new ClientException(ErrorCodes.ERR_INVALID_ENCRYPTION, "Decryption unsuccessful");
            }

            System.out.println("create the oncheck payload");
            ObjectMapper mapper = new ObjectMapper();
            InputStream file;
            if (COVERAGE_ELIGIBILITY_ONCHECK.equalsIgnoreCase(onApiAction)) {
                file = getFileAsIOStream("static/coverage_eligibility_oncheck.json");
            } else if (CLAIM_ONSUBMIT.equalsIgnoreCase(onApiAction)) {
                file = getFileAsIOStream("static/claimresponse.json");
            } else if (PRE_AUTH_ONSUBMIT.equalsIgnoreCase(onApiAction)) {
                file = getFileAsIOStream("static/preauthresponse.json");
            } else {//Default response set it to coverage
                file = getFileAsIOStream("static/coverage_eligibility_oncheck.json");
            }
            map = mapper.readValue(file, Map.class);
            ArrayList<Object> entries = (ArrayList<Object>) map.get("entry");
            try {

                ((Map) ((Map) ((Map) entries.get(0)).get("resource")).get("subject")).put("display", name);
                ((Map) ((Map) ((Map) entries.get(1)).get("resource")).get("patient")).put("display", name);
                ((Map) ((Map) entries.get(2)).get("resource")).put("gender", gender);
            } catch (Exception e) {
                System.out.println("Could not substitute the name");
            }
            if (CLAIM_ONSUBMIT.equalsIgnoreCase(onApiAction) || PRE_AUTH_ONSUBMIT.equalsIgnoreCase(onApiAction)) {
                //generating random number between 70 to 100
                Random r = new Random();
                int low = 70;
                int high = 100;
                int result = r.nextInt(high - low) + low;
                double copay = Math.round(money * 0.1);
                double remaining = money - copay;
                double payment = Math.round(result * (double) remaining / 100);
                double totalpay = payment + copay;
                System.out.println("String.valueOf(money) " + String.valueOf(money) + " " + String.valueOf(payment) + " " + String.valueOf(totalpay));
                try {
                    ((Map) ((Map) ((ArrayList<Object>) ((Map) ((Map) entries.get(1)).get("resource")).get("total")).get(0)).get("amount")).put("value", String.valueOf(money));
                    ((Map) ((Map) ((ArrayList<Object>) ((Map) ((Map) entries.get(1)).get("resource")).get("total")).get(1)).get("amount")).put("value", String.valueOf(payment));
                    ((Map) ((Map) ((Map) ((Map) entries.get(1)).get("resource")).get("payment")).get("amount")).put("value", String.valueOf(totalpay));
                    System.out.println("Money substituted");
                } catch (Exception e) {
                    System.out.println("Could not substitute the money");
                }

            }
        }
        //notificationService.notify(request, onApiAction.split("/")[2], "Request processed");
        if (autoResponse)
            onActionCall.createOnActionHeaders(request.getHcxHeaders(), map, onApiAction, publicKeyPath);
        else
            payerService.process(request, (Map<String, Object>) decodedPayload.get("payload"), map);

    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction, String onApiAction, String kafkaTopic) {
        Response response = new Response();
        try {
            Request request = new Request(requestBody, apiAction);
            notificationService.notify(request,onApiAction.split("/")[2],"Request recieved");
            setResponseParams(request, response);
            processAndValidate(onApiAction, kafkaTopic, request, requestBody);
            System.out.println("http respond sent");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
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

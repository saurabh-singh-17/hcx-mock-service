package org.swasth.hcx.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.hcxprotocol.impl.HCXIncomingRequest;
import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.utils.Operations;
import lombok.SneakyThrows;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
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
import org.swasth.hcx.fhirexamples.OnActionFhirExamples;
import org.swasth.hcx.helpers.EventGenerator;
import org.swasth.hcx.service.HcxIntegratorService;
import org.swasth.hcx.service.HeaderAuditService;
import org.swasth.hcx.service.NotificationService;
import org.swasth.hcx.service.PayerService;
import org.swasth.hcx.utils.JSONUtils;
import org.swasth.hcx.utils.OnActionCall;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.swasth.hcx.utils.Constants.*;

public class BaseController {

    @Autowired
    protected OnActionCall onActionCall;

    @Autowired
    private EventGenerator eventGenerator;

    @Autowired
    protected Environment env;

    @Autowired
    protected HeaderAuditService auditService;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    protected  HcxIntegratorService hcxIntegratorService;

    private String baseURL;


    @Value("${hcx_application.url}")
    private String hcxBasePath;

    @Value("${autoresponse}")
    private Boolean autoResponse;

    @Autowired
    private PayerService payerService;

    private HCXIntegrator hcxIntegrator;

    @SneakyThrows
    @PostConstruct
    public void init(){
        hcxIntegrator = hcxIntegratorService.initialiseHcxIntegrator();
    }

    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e){
        ResponseError error= new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }


    protected void replaceResourceInBundleEntry(Bundle bundle, String bundleURL, Class matchClass, Bundle.BundleEntryComponent bundleEntry){

        //updating the meta
        Meta meta = new Meta();
        meta.getProfile().add(new CanonicalType(bundleURL));
        meta.setLastUpdated(new Date());
        bundle.setMeta(meta);

        for(int i=0; i < bundle.getEntry().size(); i++){
            System.out.println("in the loop " + i);
            Bundle.BundleEntryComponent par = bundle.getEntry().get(i);
            DomainResource dm = (DomainResource) par.getResource();
            if(dm.getClass() == matchClass){
                bundle.getEntry().set(i,bundleEntry);
            }
        }
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
    protected void processAndValidate(String onApiAction, String metadataTopic, Request request, Map<String, Object> requestBody, String apiAction) throws Exception {
        IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
        String mid = UUID.randomUUID().toString();
        String serviceMode = env.getProperty(SERVICE_MODE);
        System.out.println("\n" + "Mode: " + serviceMode + " :: mid: " + mid + " :: Event: " + onApiAction);
        if(StringUtils.equalsIgnoreCase(serviceMode, GATEWAY)) {
                Map<String, String> pay = new HashMap<>();
                System.out.println("payload received " + requestBody);
                pay.put("payload", String.valueOf(requestBody.get("payload")));
                Map<String, Object> output = new HashMap<>();
                Map<String, Object> outputOfOnAction = new HashMap<>();
                HCXIncomingRequest incoming = new HCXIncomingRequest();
                System.out.println("create the oncheck payload");
                Bundle bundle = new Bundle();
                if (COVERAGE_ELIGIBILITY_ONCHECK.equalsIgnoreCase(onApiAction)) {
                    boolean result = incoming.process(JSONUtils.serialize(pay), Operations.COVERAGE_ELIGIBILITY_CHECK,output);
                    if(!result){
                        System.out.println("Error while processing incoming request: " +  output);
                    }
                    System.out.println("outmap after decryption " +  output.get("fhirPayload"));
                    System.out.println("decryption successful");
                    //processing the decrypted incoming bundle
                    bundle = p.parseResource(Bundle.class, (String) output.get("fhirPayload"));
                    CoverageEligibilityResponse covRes = OnActionFhirExamples.coverageEligibilityResponseExample();
                    replaceResourceInBundleEntry(bundle, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityResponseBundle.html", CoverageEligibilityRequest.class, new Bundle.BundleEntryComponent().setFullUrl(covRes.getResourceType() + "/" + covRes.getId().toString().replace("#","")).setResource(covRes));
                    System.out.println("bundle reply " + p.encodeResourceToString(bundle));
                    //sending the onaction call
                    sendResponse(apiAction,p.encodeResourceToString(bundle),(String) output.get("fhirPayload"), Operations.COVERAGE_ELIGIBILITY_ON_CHECK,  String.valueOf(requestBody.get("payload")),"response.complete" ,outputOfOnAction);
                } else if (CLAIM_ONSUBMIT.equalsIgnoreCase(onApiAction)) {
                    boolean result = incoming.process(JSONUtils.serialize(pay), Operations.CLAIM_SUBMIT,output);
                    if(!result){
                        System.out.println("Error while processing incoming request: " +  output);
                    }
                    System.out.println("outmap after decryption " +  output);
                    System.out.println("decryption successful");
                    //processing the decrypted incoming bundle
                    bundle = p.parseResource(Bundle.class, (String) output.get("fhirPayload"));
                    ClaimResponse claimRes = OnActionFhirExamples.claimResponseExample();
                    replaceResourceInBundleEntry(bundle, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-ClaimResponseBundle.html", Claim.class, new Bundle.BundleEntryComponent().setFullUrl(claimRes.getResourceType() + "/" + claimRes.getId().toString().replace("#","")).setResource(claimRes));
                    sendResponse(apiAction,p.encodeResourceToString(bundle), (String) output.get("fhirPayload"), Operations.CLAIM_ON_SUBMIT,  String.valueOf(requestBody.get("payload")),"response.complete" ,outputOfOnAction);
                } else if (PRE_AUTH_ONSUBMIT.equalsIgnoreCase(onApiAction)) {
                    boolean result = incoming.process(JSONUtils.serialize(pay), Operations.PRE_AUTH_SUBMIT,output);
                    if(!result){
                        System.out.println("Error while processing incoming request: " +  output);
                    }
                    System.out.println("outmap after decryption " +  output);
                    System.out.println("decryption successful");
                    //processing the decrypted incoming bundle
                    bundle = p.parseResource(Bundle.class, (String) output.get("fhirPayload"));
                    ClaimResponse preAuthRes = OnActionFhirExamples.claimResponseExample();
                    preAuthRes.setUse(ClaimResponse.Use.PREAUTHORIZATION);
                    replaceResourceInBundleEntry(bundle, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-ClaimResponseBundle.html", Claim.class, new Bundle.BundleEntryComponent().setFullUrl(preAuthRes.getResourceType() + "/" + preAuthRes.getId().toString().replace("#","")).setResource(preAuthRes));
                    sendResponse(apiAction,p.encodeResourceToString(bundle), (String) output.get("fhirPayload"), Operations.PRE_AUTH_ON_SUBMIT,  String.valueOf(requestBody.get("payload")),"response.complete" ,outputOfOnAction);
                }
        }

    }

    private void sendResponse(String apiAction, String respfhir, String reqFhir, Operations operation, String actionJwe, String onActionStatus, Map<String,Object> output) throws Exception {
        if (autoResponse) {
            onActionCall.sendOnAction(respfhir, operation, actionJwe, onActionStatus, output);
        } else {
            Request request = new Request(Collections.singletonMap("payload", actionJwe), apiAction);
            payerService.process(request, reqFhir, respfhir);
        }
    }

    public ResponseEntity<Object> validateReqAndPushToKafka(Map<String, Object> requestBody, String apiAction, String onApiAction, String kafkaTopic) {
        Response response = new Response();
        try {
            Request request = new Request(requestBody, apiAction);
            notificationService.notify(request,onApiAction.split("/")[2],"Request received");
            setResponseParams(request, response);
            processAndValidate(onApiAction, kafkaTopic, request, requestBody,apiAction);
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

    protected void validateStr(String field, String value) throws ClientException {
        if(StringUtils.isEmpty(value))
            throw new ClientException("Missing required field " + field);
    }

    protected void validateMap(String field, Map<String,Object> value) throws ClientException {
        if(MapUtils.isEmpty(value))
            throw new ClientException("Missing required field " + field);
    }

}

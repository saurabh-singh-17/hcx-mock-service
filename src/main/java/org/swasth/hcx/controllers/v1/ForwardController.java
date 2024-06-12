package org.swasth.hcx.controllers.v1;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.utils.Operations;
import org.apache.http.protocol.HTTP;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class ForwardController extends BaseController {

    IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

    @PostMapping(value = "/payer/request/forward")
    @CrossOrigin(origins = "http://127.0.0.1:5173")
    public ResponseEntity<Object> requestList(@RequestBody Map<String, Object> requestBody) throws Exception {
        try{
        String senderCode = (String) requestBody.getOrDefault("sender_code", "");
        String recipientCode = (String) requestBody.getOrDefault("recipient_code", "");
        String requestFhir = (String) requestBody.getOrDefault("request_fhir", "");
        String correlationId = (String) requestBody.getOrDefault("correlation_id", "");
        String operation = (String) requestBody.getOrDefault("operation", "");
        String apiCallId = UUID.randomUUID().toString();
        //gettting the request FHIR from the database

        Bundle bundle = new Bundle();
        bundle = parser.parseResource(Bundle.class, requestFhir);
        HCXIntegrator hcxIntegrator = hcxIntegratorService.getHCXIntegrator(senderCode);
        System.out.println("fhir object " + requestFhir);
        Map<String, Object> output = new HashMap<>();
        hcxIntegrator.processOutgoingRequest(parser.encodeResourceToString(bundle), Operations.CLAIM_SUBMIT, recipientCode, apiCallId, correlationId, "", new HashMap<>(), output);
        System.out.println("The outgoing request has been successfully generated." + output);
        Response response = new Response();
        response.setApiCallId(apiCallId);
        response.setCorrelationId(correlationId);
        response.setTimestamp(System.currentTimeMillis());
        return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

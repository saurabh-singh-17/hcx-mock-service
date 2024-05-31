package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.hcxprotocol.utils.Operations;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import static org.swasth.hcx.utils.Constants.PENDING;

@Service
public class PatientService {

    @Value("${postgres.table.patientData}")
    private String table;

    @Autowired
    private PostgresService postgres;

    private final IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);


    private String getEntity(String action) {
        Map<String, String> actionMap = new HashMap<>();
        actionMap.put(Constants.COVERAGE_ELIGIBILITY_CHECK, Constants.COVERAGE_ELIGIBILITY);
        actionMap.put(Constants.PRE_AUTH_SUBMIT, Constants.PRE_AUTH);
        actionMap.put(Constants.CLAIM_SUBMIT, Constants.CLAIM);
        actionMap.put(Constants.COMMUNICATION_REQUEST, Constants.COMMUNICATION);
        return actionMap.get(action);
    }

    public void process(Operations operation, Request request, String actionJwe) throws ClientException {
        Bundle requestBundle = parser.parseResource(Bundle.class, actionJwe);
        String name = "";
        String number = "";
        String insurance_id = "";
        for (Bundle.BundleEntryComponent bundleEntryComponent : requestBundle.getEntry()) {
            if(bundleEntryComponent.getResource().getResourceType().toString() == "Patient"){
                Patient PatientRes = parser.parseResource(Patient.class, parser.encodeResourceToString(bundleEntryComponent.getResource()));
                name = PatientRes.getName().get(0).getText();
                number = PatientRes.getTelecom().get(0).getValue().toString();
            }
            if(bundleEntryComponent.getResource().getResourceType().toString() == "Coverage"){
                Coverage coverage = parser.parseResource(Coverage.class, parser.encodeResourceToString(bundleEntryComponent.getResource()));
                insurance_id = coverage.getSubscriberId();
            }

        }
        String query = String.format("INSERT INTO %s (request_id,phone_number,sender_code,recipient_code,insurance_id,patient_name,action,created_on) VALUES ('%s','%s','%s','%s','%s','%s','%s',%d);",
                table, request.getApiCallId(), number, request.getSenderCode(), request.getRecipientCode(), insurance_id, name, getEntity(request.getAction()), System.currentTimeMillis());
        postgres.execute(query);
    }
}

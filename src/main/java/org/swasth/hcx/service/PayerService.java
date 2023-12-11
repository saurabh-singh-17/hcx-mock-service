package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import javax.annotation.PostConstruct;
import java.util.*;

import static org.swasth.hcx.utils.Constants.PENDING;

@Service
public class PayerService {

    @Value("${postgres.table.payerData}")
    private String table;

    @Autowired
    private PostgresService postgres;
    private final IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

    public void process(Request request, String reqFhirObj, String respFhirObj) throws ClientException, JsonProcessingException {
        Map<String, Object> info = new HashMap<>();
        if (request.getAction().contains("coverageeligibility")) {
            String query = String.format("INSERT INTO %s (request_id,sender_code,recipient_code,action,raw_payload,request_fhir,response_fhir,status,additional_info,created_on,updated_on,correlation_id,mobile,otp_verification,workflow_id,account_number,ifsc_code,bank_details,app,supporting_documents,bill_amount,insurance_id,patient_name,service_type) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');",
                    table, request.getApiCallId(), request.getSenderCode(), request.getRecipientCode(), getEntity(request.getAction()), request.getPayload().getOrDefault("payload", ""), reqFhirObj, respFhirObj, PENDING, JSONUtils.serialize(info), System.currentTimeMillis(), System.currentTimeMillis(), request.getCorrelationId(), "", PENDING, request.getWorkflowId(), "1234", "1234", PENDING, "", "{}", "", getInsuranceId(reqFhirObj), getPatientName(reqFhirObj),"");
            postgres.execute(query);
        } else if (request.getAction().contains("communication")) {
            String query = String.format("INSERT INTO %s (request_id,sender_code,recipient_code,action,raw_payload,request_fhir,response_fhir,status,additional_info,created_on,updated_on,correlation_id,mobile,otp_verification,workflow_id,account_number,ifsc_code,bank_details,app,supporting_documents,bill_amount,insurance_id,patient_name,service_type) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');",
                    table, request.getApiCallId(), request.getSenderCode(), request.getRecipientCode(), getEntity(request.getAction()), request.getPayload().getOrDefault("payload", ""), reqFhirObj, respFhirObj, PENDING, JSONUtils.serialize(info), System.currentTimeMillis(), System.currentTimeMillis(), request.getCorrelationId(), "", PENDING, request.getWorkflowId(), "1234", "1234", PENDING, "", "{}", "", "", "","");
            postgres.execute(query);
        } else {
            info.put("medical", Collections.singletonMap("status", PENDING));
            info.put("financial", Collections.singletonMap("status", PENDING));
            Map<String, List<String>> getDocuments = getSupportingDocuments(reqFhirObj);
            String amount = "";
            String serializeDocuments = "";
            serializeDocuments = JSONUtils.serialize(getDocuments);
            amount = getAmount(reqFhirObj);
            String query = String.format("INSERT INTO %s (request_id,sender_code,recipient_code,action,raw_payload,request_fhir,response_fhir,status,additional_info,created_on,updated_on,correlation_id,mobile,otp_verification,workflow_id,account_number,ifsc_code,bank_details,app,supporting_documents,bill_amount,insurance_id,patient_name,service_type) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');",
                    table, request.getApiCallId(), request.getSenderCode(), request.getRecipientCode(), getEntity(request.getAction()), request.getPayload().getOrDefault("payload", ""), reqFhirObj, respFhirObj, PENDING, JSONUtils.serialize(info), System.currentTimeMillis(), System.currentTimeMillis(), request.getCorrelationId(), "", PENDING, request.getWorkflowId(), "1234", "1234", PENDING, "", serializeDocuments, amount, getInsuranceId(reqFhirObj), getPatientName(reqFhirObj), "");
            postgres.execute(query);
        }
    }

    private String getEntity(String action){
        Map<String,String> actionMap = new HashMap<>();
        actionMap.put("/v0.7/coverageeligibility/check", "coverageeligibility");
        actionMap.put("/v0.7/preauth/submit", "preauth");
        actionMap.put("/v0.7/claim/submit", "claim");
        actionMap.put("/communication/request", "communication");
        return actionMap.get(action);
    }

    public Map<String, List<String>> getSupportingDocuments(String fhirPayload) {
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        Map<String, List<String>> documentMap = new HashMap<>();
        for (Bundle.BundleEntryComponent bundleEntryComponent : parsed.getEntry()) {
            if (Objects.equals(bundleEntryComponent.getResource().getResourceType().toString(), "Claim")) {
                Claim claim = parser.parseResource(Claim.class, parser.encodeResourceToString(bundleEntryComponent.getResource()));
                for (Claim.SupportingInformationComponent supportingInfo : claim.getSupportingInfo()) {
                    if (supportingInfo.hasValueAttachment() && supportingInfo.getValueAttachment().hasUrl()) {
                        String url = supportingInfo.getValueAttachment().getUrl();
                        String documentType = supportingInfo.getCategory().getCoding().get(0).getDisplay();
                        if (!documentMap.containsKey(documentType)) {
                            documentMap.put(documentType, new ArrayList<>());
                        }
                        documentMap.get(documentType).add(url);
                    }
                }
            }
        }
        return documentMap;
    }

    public String getAmount(String fhirPayload) {
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        String amount = "";
        for (Bundle.BundleEntryComponent bundleEntryComponent : parsed.getEntry()) {
            if (Objects.equals(bundleEntryComponent.getResource().getResourceType().toString(), "Claim")) {
                Claim claim = parser.parseResource(Claim.class, parser.encodeResourceToString(bundleEntryComponent.getResource()));
                amount = claim.getTotal().getValue().toString();
            }
        }
        return amount;
    }

    public String getInsuranceId(String fhirPayload) {
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        String insuranceId = "";
        for (Bundle.BundleEntryComponent bundleEntryComponent : parsed.getEntry()) {
            if (Objects.equals(bundleEntryComponent.getResource().getResourceType().toString(), "Coverage")) {
                Coverage coverage = parser.parseResource(Coverage.class, parser.encodeResourceToString(bundleEntryComponent.getResource()));
                insuranceId = coverage.getSubscriberId();
            }
        }
        return insuranceId;
    }

    public String getPatientName(String fhirPayload){
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        String patientName = "";
        for (Bundle.BundleEntryComponent bundleEntryComponent : parsed.getEntry()) {
            if (Objects.equals(bundleEntryComponent.getResource().getResourceType().toString(), "Patient")) {
                Patient patient = parser.parseResource(Patient.class, parser.encodeResourceToString(bundleEntryComponent.getResource()));
                patientName = patient.getName().get(0).getTextElement().getValue();
            }
        }
        return patientName;
    }
}

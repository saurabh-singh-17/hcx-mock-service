package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;

import static org.swasth.hcx.utils.Constants.PENDING;

@Service
public class PayerService {

    @Value("${postgres.table.payerData}")
    private String table;

    @Autowired
    private PostgresService postgres;
    private final IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

    public void process(Request request, String reqFhirObj, String respFhirObj) throws ClientException, JsonProcessingException, SQLException {
        Map<String, Object> info = new HashMap<>();
        if (request.getAction().contains("coverageeligibility")) {
            String query = String.format("INSERT INTO %s (request_id,sender_code,recipient_code,action,raw_payload,request_fhir,response_fhir,status,additional_info,created_on,updated_on,correlation_id,mobile,otp_verification,workflow_id,account_number,ifsc_code,bank_details,app,supporting_documents,bill_amount,insurance_id,patient_name) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');",
                    table, request.getApiCallId(), request.getSenderCode(), request.getRecipientCode(), getEntity(request.getAction()), request.getPayload().getOrDefault("payload", ""), reqFhirObj, respFhirObj, PENDING, JSONUtils.serialize(info), System.currentTimeMillis(), System.currentTimeMillis(), request.getCorrelationId(), getPatientMobile(reqFhirObj), PENDING, request.getWorkflowId(), "1234", "1234", PENDING, getApp(request.getAction(),reqFhirObj), "{}", "", getInsuranceId(reqFhirObj), getPatientName(reqFhirObj));
            postgres.execute(query);
        } else if (request.getAction().contains("communication")) {
            String query = String.format("INSERT INTO %s (request_id,sender_code,recipient_code,action,raw_payload,request_fhir,response_fhir,status,additional_info,created_on,updated_on,correlation_id,mobile,otp_verification,workflow_id,account_number,ifsc_code,bank_details,app,supporting_documents,bill_amount,insurance_id,patient_name) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');",
                    table, request.getApiCallId(), request.getSenderCode(), request.getRecipientCode(), getEntity(request.getAction()), request.getPayload().getOrDefault("payload", ""), reqFhirObj, respFhirObj, PENDING, JSONUtils.serialize(info), System.currentTimeMillis(), System.currentTimeMillis(), request.getCorrelationId(), "", PENDING, request.getWorkflowId(), "1234", "1234", PENDING, "", "{}", "", "", "");
            postgres.execute(query);
        } else {
            info.put("medical", Collections.singletonMap("status", PENDING));
            info.put("financial", Collections.singletonMap("status", PENDING));
            Map<String, List<String>> getDocuments = getSupportingDocuments(reqFhirObj);
            String amount = "";
            String serializeDocuments = "";
            serializeDocuments = JSONUtils.serialize(getDocuments);
            amount = getAmount(reqFhirObj);
            String query = String.format("INSERT INTO %s (request_id,sender_code,recipient_code,action,raw_payload,request_fhir,response_fhir,status,additional_info,created_on,updated_on,correlation_id,mobile,otp_verification,workflow_id,account_number,ifsc_code,bank_details,app,supporting_documents,bill_amount,insurance_id,patient_name) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,%d,'%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s');",
                    table, request.getApiCallId(), request.getSenderCode(), request.getRecipientCode(), getEntity(request.getAction()), request.getPayload().getOrDefault("payload", ""), reqFhirObj, respFhirObj, PENDING, JSONUtils.serialize(info), System.currentTimeMillis(), System.currentTimeMillis(), request.getCorrelationId(), getPatientMobile(reqFhirObj), PENDING, request.getWorkflowId(), "1234", "1234", PENDING, getApp(request.getAction(),reqFhirObj) , serializeDocuments, amount, getInsuranceId(reqFhirObj), getPatientName(reqFhirObj));
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
        Map<String, List<String>> documentMap = new HashMap<>();
        Claim claim = getResourceByType("Claim", Claim.class, fhirPayload);
        if (claim != null) {
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
        return documentMap;
    }


    public String getAmount(String fhirPayload) {
        String amount = "0";
        Claim claim = getResourceByType("Claim", Claim.class, fhirPayload);
        if (claim != null && claim.getTotal() != null && claim.getTotal().getValue() != null) {
            amount = String.valueOf(claim.getTotal().getValue());
        }
        return amount;
    }

    public String getInsuranceId(String fhirPayload) {
        String insuranceId = "";
        Coverage coverage = getResourceByType("Coverage", Coverage.class, fhirPayload);
        if (coverage != null && coverage.getSubscriberId() != null) {
            insuranceId = coverage.getSubscriberId();
        }
        return insuranceId;
    }

    public String getPatientName(String fhirPayload) {
        String patientName = "";
        Patient patient = getResourceByType("Patient", Patient.class, fhirPayload);
        if (patient!= null && patient.getName() != null && !CollectionUtils.isEmpty(patient.getName()) && patient.getName().get(0).getTextElement() != null && patient.getName().get(0).getTextElement().getValue() != null) {
            patientName = patient.getName().get(0).getTextElement().getValue();
        }
        return patientName;
    }

    private String getApp(String apiAction, String fhirPayload) {
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        if (apiAction.equalsIgnoreCase("/v0.7/coverageeligibility/check")) {
            CoverageEligibilityRequest ce = parser.parseResource(CoverageEligibilityRequest.class, parser.encodeResourceToString(parsed.getEntry().get(0).getResource()));
            if (ce.getText() != null && ce.getText().getDiv().allText() != null)
                return ce.getText().getDiv().allText();
        } else if (apiAction.equalsIgnoreCase("/v0.7/claim/submit") || apiAction.equalsIgnoreCase("/v0.7/preauth/submit")) {
            Claim claim = parser.parseResource(Claim.class, parser.encodeResourceToString(parsed.getEntry().get(0).getResource()));
            if (claim.getText() != null && claim.getText().getDiv().allText() != null)
                return claim.getText().getDiv().allText();
        }
        return "";
    }

    public String getPatientMobile(String fhirPayload) {
        String patientMobile = "";
        Patient patient = getResourceByType("Patient", Patient.class, fhirPayload);
        if (patient != null && patient.getTelecom() != null && !CollectionUtils.isEmpty(patient.getTelecom())) {
            patientMobile = patient.getTelecom().get(0).getValue();
        }
        return patientMobile;
    }

    public <T extends Resource> T getResourceByType(String type, Class<T> resourceClass, String fhirPayload) {
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        return parsed.getEntry().stream()
                .filter(entry -> StringUtils.equalsIgnoreCase(String.valueOf(entry.getResource().getResourceType()), type))
                .findFirst()
                .map(entry -> parser.parseResource(resourceClass, parser.encodeResourceToString(entry.getResource())))
                .orElse(null);
    }
}

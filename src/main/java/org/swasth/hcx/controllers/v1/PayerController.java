package org.swasth.hcx.controllers.v1;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.hcxprotocol.utils.Operations;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.exception.ClientException;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.service.PostgresService;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.hcx.utils.Constants.*;

@RestController
public class PayerController extends BaseController {

    @Value("${postgres.payerTable}")
    private String table;

    @Autowired
    private PostgresService postgres;

    @PostMapping(value = "/payer/request/list")
    public ResponseEntity<Object> requestList(@RequestBody Map<String, Object> requestBody) {
        try {
            String type = (String) requestBody.getOrDefault("type", "");
            validateProp("type", type);
            List<Object> result = new ArrayList<>();
            String query = "SELECT * FROM " + table + " WHERE action like '%" + type + "%'";
            ResultSet resultSet = postgres.executeQuery(query);
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("request_id", resultSet.getString("request_id"));
                map.put("status", resultSet.getString("status"));
                map.put("additional_info", JSONUtils.deserialize(resultSet.getString("additional_info"), Map.class));
                map.put("payload", resultSet.getString("request_fhir"));
                result.add(map);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put(type, result);
            resp.put("count", result.size());
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(value = "/payer/coverageeligibility/approve")
    public ResponseEntity<Object> coverageEligibilityApprove(@RequestBody Map<String, Object> requestBody) {
        return review(requestBody,"coverageeligibility", APPROVED);
    }

    @PostMapping(value = "/payer/coverageeligibility/reject")
    public ResponseEntity<Object> coverageEligibilityReject(@RequestBody Map<String, Object> requestBody) {
        return review(requestBody,"coverageeligibility", REJECTED);
    }

    @PostMapping(value = "/payer/preauth/approve")
    public ResponseEntity<Object> preauthApprove(@RequestBody Map<String, Object> requestBody) {
        return review(requestBody,"preath", APPROVED);
    }

    @PostMapping(value = "/payer/preauth/reject")
    public ResponseEntity<Object> preauthReject(@RequestBody Map<String, Object> requestBody) {
        return review(requestBody,"preath", REJECTED);
    }

    @PostMapping(value = "/payer/claim/approve")
    public ResponseEntity<Object> claimApprove(@RequestBody Map<String, Object> requestBody) {
        return review(requestBody,"claim", APPROVED);
    }

    @PostMapping(value = "/payer/claim/reject")
    public ResponseEntity<Object> claimReject(@RequestBody Map<String, Object> requestBody) {
        return review(requestBody,"claim", REJECTED);
    }

    public ResponseEntity<Object> review(Map<String, Object> requestBody, String entity, String status){
        try {
            System.out.println("Review: " + status + " :: entity: " + entity + " :: request body: " + requestBody);
            String id = (String) requestBody.getOrDefault("request_id", "");
            validateProp("request_id", id);
            Map<String,Object> output = new HashMap<>();
            if (StringUtils.equals(entity, "coverageeligibility")) {
                String updateQuery = String.format("UPDATE %s SET status='%s',updated_on=%d WHERE request_id='%s' RETURNING %s,%s",
                        table, status, System.currentTimeMillis(), id, "raw_payload", "response_fhir");
                ResultSet resultset = postgres.executeQuery(updateQuery);
                String respfhir = "";
                String actionJwe = "";
                while(resultset.next()){
                    respfhir = resultset.getString("response_fhir");
                    actionJwe = resultset.getString("raw_payload");
                }
                if(status.equals(APPROVED)){
                    onActionCall.sendOnAction(respfhir, Operations.COVERAGE_ELIGIBILITY_ON_CHECK, actionJwe, "response.complete", output);
                } else {
                    IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
                    Bundle newBundle = p.parseResource(Bundle.class, respfhir);
                    for(int i=0; i < newBundle.getEntry().size(); i++){
                        Bundle.BundleEntryComponent par = newBundle.getEntry().get(i);
                        DomainResource dm = (DomainResource) par.getResource();
                        System.out.println("type of dm" + dm);
                        if(dm.getClass() == CoverageEligibilityResponse.class){
                            System.out.println("index " + i);
                            ((CoverageEligibilityResponse) dm).getError().add(new CoverageEligibilityResponse.ErrorsComponent(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/adjudication-error").setCode("a001").setDisplay("Coverage Eligibility Request has been rejected"))));
                        }
                    }
                    String bundleString = p.encodeResourceToString(newBundle);
                    System.out.println("Failure Response bundle: " + bundleString);
                    onActionCall.sendOnAction(bundleString, Operations.COVERAGE_ELIGIBILITY_ON_CHECK, actionJwe, "response.complete", output);
                }
            } else {
                String type = (String) requestBody.getOrDefault("type", "");
                validateProp("type", type);
                if (!Constants.PAYOR_APPROVAL_TYPES.contains(type))
                    throw new ClientException("Invalid type, allowed types are: " + Constants.PAYOR_APPROVAL_TYPES);
                Map<String, Object> info = new HashMap<>();
                info.put("status", status);
                info.put("remarks", requestBody.getOrDefault("remarks", ""));
                if(StringUtils.equals(APPROVED, status))
                    info.put("approved_amount", requestBody.getOrDefault("approved_amount", 0));
                String query = String.format("UPDATE %s SET additional_info = jsonb_set(additional_info::jsonb, '{%s}', '%s'),updated_on = %d WHERE request_id = '%s' RETURNING %s,%s,%s,%s,%s",
                        table, type, JSONUtils.serialize(info), System.currentTimeMillis(), id, "additional_info", "status", "raw_payload", "response_fhir", "action");
                ResultSet resultSet = postgres.executeQuery(query);
                Map<String, Object> addInfo = new HashMap<>();
                String existingStatus = PENDING;
                String respfhir = "";
                String actionJwe = "";
                String action = "";
                while (resultSet.next()) {
                    addInfo.putAll(JSONUtils.deserialize(resultSet.getString("additional_info"), Map.class));
                    existingStatus = resultSet.getString("status");
                    respfhir = resultSet.getString("response_fhir");
                    actionJwe = resultSet.getString("raw_payload");
                    action = resultSet.getString("action");
                }

                if (!addInfo.isEmpty()) {
                    String overallStatus = getStatus(addInfo);
                    if (!StringUtils.equalsIgnoreCase(overallStatus, existingStatus)) {
                        String updateQuery = String.format("UPDATE %s SET status = '%s',updated_on = %d WHERE request_id = '%s'"
                                , table, overallStatus, System.currentTimeMillis(), id);
                        postgres.execute(updateQuery);
                    }

                    if (overallStatus.equals(APPROVED)) {
                        IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
                        Bundle newBundle = p.parseResource(Bundle.class, respfhir);
                        for(int i=0; i < newBundle.getEntry().size(); i++){
                            Bundle.BundleEntryComponent par = newBundle.getEntry().get(i);
                            DomainResource dm = (DomainResource) par.getResource();
                            System.out.println("type of dm" + dm);
                            if(dm.getClass() == ClaimResponse.class){
                                System.out.println("index " + i);
                                if(entity.equals("preauth")){
                                    ((ClaimResponse) dm).setUse(ClaimResponse.Use.PREAUTHORIZATION);
                                }
                                ((ClaimResponse) dm).getTotal().set(0,new ClaimResponse.TotalComponent().setCategory(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/adjudication").setCode("benefit"))).setAmount(new Money().setValue((BigDecimal) requestBody.getOrDefault("approved_amount", 0)).setCurrency("INR")));
                            }
                        }
                        String bundleString = p.encodeResourceToString(newBundle);
                        System.out.println("Success Response bundle: " + bundleString);
                        onActionCall.sendOnAction(bundleString, Enum.valueOf(Operations.class, action), actionJwe, "response.complete", output);
                    } else if (overallStatus.equals(REJECTED)){
                        IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
                        Bundle newBundle = p.parseResource(Bundle.class, respfhir);
                        for(int i=0; i < newBundle.getEntry().size(); i++){
                            Bundle.BundleEntryComponent par = newBundle.getEntry().get(i);
                            DomainResource dm = (DomainResource) par.getResource();
                            System.out.println("type of dm" + dm);
                            if(dm.getClass() == ClaimResponse.class){
                                System.out.println("index " + i);
                                if(entity.equals("preauth")){
                                    ((ClaimResponse) dm).setUse(ClaimResponse.Use.PREAUTHORIZATION);
                                }
                                ((ClaimResponse) dm).getError().add(new ClaimResponse.ErrorComponent(new CodeableConcept(new Coding().setSystem("http://hcxprotocol.io/codes/claim-error-codes").setCode("AUTH-001").setDisplay(StringUtils.capitalize(type) + " adjudication failed"))));
                            }
                        }
                        String bundleString = p.encodeResourceToString(newBundle);
                        System.out.println("Failure Response bundle: " + bundleString);
                        onActionCall.sendOnAction(bundleString, Operations.COVERAGE_ELIGIBILITY_ON_CHECK, actionJwe, "response.complete", output);
                    }
                }
            }
            Map<String,Object> resp = new HashMap<>();
            resp.put("timestamp", System.currentTimeMillis());
            resp.put("status", Constants.SUCCESSFUL);
            resp.put("reason", "");
            System.out.println("Process completed :: request id :" + id);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private void validateProp(String field, String value) throws ClientException {
        if(StringUtils.isEmpty(value))
            throw new ClientException("Missing required field " + field);
    }

    private String getStatus(Map<String,Object> addInfo){
        String status = PENDING;
        for (Map.Entry<String, Object> entry : addInfo.entrySet()) {
            String objStatus = ((Map<String, Object>) entry.getValue()).getOrDefault("status","").toString();
            if(StringUtils.equalsIgnoreCase(REJECTED, objStatus)) {
                status = REJECTED;
                break;
            } else if (StringUtils.equalsIgnoreCase(APPROVED, objStatus)) {
                status = APPROVED;
            }
        }
        return status;
    }

}

package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.utils.Operations;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.XhtmlDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.dto.ResponseError;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.exception.ServerException;
import org.swasth.hcx.exception.ServiceUnavailbleException;
import org.swasth.hcx.fhirexamples.OnActionFhirExamples;
import org.swasth.hcx.utils.HCXFHIRUtils;
import org.swasth.hcx.utils.JSONUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class GenerateOutgoingRequest {

    @Autowired
    private BeneficiaryService beneficiaryService;
    @Autowired
    protected HcxIntegratorService hcxIntegratorService;
    @Autowired
    private PostgresService postgresService;
    @Value("${postgres.table.payerData}")
    private String payorDataTable;
    @Value("${phone.communication-content}")
    private String communicationContent;
    @Value("${beneficiary.protocol-base-path}")
    private String protocolBasePath;
    @Value("${beneficiary.participant-code}")
    private String beneficiaryParticipantCode;
    @Value("${beneficiary.username}")
    private String beneficiaryUserName;
    @Value("${beneficiary.password}")
    private String beneficiaryPassword;
    @Value("${beneficiary.recipient-code}")
    private String mockRecipientCode;
    @Value("${payor.username}")
    private String payorUsername;
    @Value("${payor.password}")
    private String payorPassword;
    IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

    public ResponseEntity<Object> createCoverageEligibilityRequest(Map<String, Object> requestBody, Operations operations) {
        Response response = new Response();
        try {
            String senderCode = (String) requestBody.getOrDefault("senderCode", "");
            getSenderAndRecipientCode(senderCode);
            String recipientCode = (String) requestBody.getOrDefault("recipientCode", "");
            HCXIntegrator hcxIntegrator = hcxIntegratorService.getHCXIntegrator(senderCode);
            CoverageEligibilityRequest ce = OnActionFhirExamples.coverageEligibilityRequestExample();
            System.out.println("requestBody" + requestBody);
            String app = (String) requestBody.get("app");
            ce.setText(new Narrative().setDiv(new XhtmlDocument().setValue(app)).setStatus(Narrative.NarrativeStatus.GENERATED));
            Practitioner practitioner = OnActionFhirExamples.practitionerExample();
            Organization hospital = OnActionFhirExamples.providerOrganizationExample();
            hospital.setName((String) requestBody.getOrDefault("providerName", ""));
            Patient patient = OnActionFhirExamples.patientExample();
            patient.getTelecom().add(new ContactPoint().setValue((String) requestBody.getOrDefault("mobile", "")).setSystem(ContactPoint.ContactPointSystem.PHONE));
            patient.getName().add(new HumanName().setText((String) requestBody.getOrDefault("patientName", "")));
            Organization insurerOrganization = OnActionFhirExamples.insurerOrganizationExample();
            insurerOrganization.setName((String) requestBody.getOrDefault("payor", ""));
            Coverage coverage = OnActionFhirExamples.coverageExample();
            coverage.setSubscriberId((String) requestBody.getOrDefault("insuranceId", ""));
            List<DomainResource> domList = List.of(hospital, insurerOrganization, patient, coverage, practitioner);
            Bundle bundleTest = new Bundle();
            try {
                bundleTest = HCXFHIRUtils.resourceToBundle(ce, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html", hcxIntegrator);
                System.out.println("reosurceToBundle Coverage Eligibility Request \n" + parser.encodeResourceToString(bundleTest));
            } catch (Exception e) {
                System.out.println("Error message " + e.getMessage());
                throw new ClientException(e.getMessage());
            }
            Map<String, Object> output = new HashMap<>();
            String workFlowId = UUID.randomUUID().toString();
            hcxIntegrator.processOutgoingRequest(parser.encodeResourceToString(bundleTest), operations, recipientCode, "", "", workFlowId, new HashMap<>(), output);
            System.out.println("The outgoing request has been successfully generated.");
            Response response1 = new Response(workFlowId);
            return new ResponseEntity<>(response1, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    public ResponseEntity<Object> createClaimRequest(Map<String, Object> requestBody, Operations operations) {
        Response response = new Response();
        try {
            String senderCode = (String) requestBody.getOrDefault("senderCode", "");
            String recipientCode = (String) requestBody.getOrDefault("recipientCode", "");
            HCXIntegrator hcxIntegrator = hcxIntegratorService.getHCXIntegrator(senderCode);
            Claim claim = OnActionFhirExamples.claimExample();
            String billAmount = (String) requestBody.getOrDefault("billAmount", 0);
            claim.setTotal(new Money().setCurrency("INR").setValue(Long.parseLong(billAmount)));
            // To check type is OPD
            String type = (String) requestBody.getOrDefault("type", "");
            claim.setSubType(new CodeableConcept(new Coding().setSystem("https://staging-hcx.swasth.app/hapi-fhir/fhir/CodeSystem/hcx-claim-sub-types").setCode(type)));
            // adding supporting documents (Bill/invoice or prescription)
            if (requestBody.containsKey("supportingDocuments")) {
                ArrayList<Map<String, Object>> supportingDocuments = JSONUtils.convert(requestBody.get("supportingDocuments"), ArrayList.class);
                for (Map<String, Object> document : supportingDocuments) {
                    String documentType = (String) document.getOrDefault("documentType", "");
                    List<String> urls = (List<String>) document.get("urls");
                    if (urls != null && !urls.isEmpty()) {
                        for (String url : urls) {
                            claim.addSupportingInfo(new Claim.SupportingInformationComponent().setSequence(1).setCategory(new CodeableConcept(new Coding().setCode(documentType).setSystem("http://hcxprotocol.io/codes/claim-supporting-info-categories").setDisplay(documentType))).setValue(new Attachment().setUrl(url)));
                        }
                    }
                }
            }
            Practitioner practitioner = OnActionFhirExamples.practitionerExample();
            Organization hospital = OnActionFhirExamples.providerOrganizationExample();
            hospital.setName((String) requestBody.getOrDefault("providerName", ""));
            Patient patient = OnActionFhirExamples.patientExample();
            patient.getTelecom().add(new ContactPoint().setValue((String) requestBody.getOrDefault("mobile", "")).setSystem(ContactPoint.ContactPointSystem.PHONE));
            patient.getName().add(new HumanName().setText((String) requestBody.getOrDefault("patientName", "")));
            Organization insurerOrganization = OnActionFhirExamples.insurerOrganizationExample();
            insurerOrganization.setName((String) requestBody.getOrDefault("payor", ""));
            Coverage coverage = OnActionFhirExamples.coverageExample();
            coverage.setSubscriberId((String) requestBody.getOrDefault("insuranceId", ""));
            List<DomainResource> domList = List.of(hospital, insurerOrganization, patient, coverage, practitioner);
            Bundle bundleTest = new Bundle();
            try {
                bundleTest = HCXFHIRUtils.resourceToBundle(claim, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-Claim.html", hcxIntegrator);
                System.out.println("resource To Bundle claim Request\n" + parser.encodeResourceToString(bundleTest));
            } catch (Exception e) {
                System.out.println("Error message " + e.getMessage());
            }
            Map<String, Object> output = new HashMap<>();
            String workflowId = "";
            if (!requestBody.containsKey("workflowId")) {
                workflowId = UUID.randomUUID().toString();
            } else {
                workflowId = (String) requestBody.getOrDefault("workflowId", "");
            }
            hcxIntegrator.processOutgoingRequest(parser.encodeResourceToString(bundleTest), operations, recipientCode, "", "", workflowId, new HashMap<>(), output);
            System.out.println("The outgoing request has been successfully generated.");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    public ResponseEntity<Object> createCommunicationRequest(Map<String, Object> requestBody, Operations operations) {
        Response response = new Response();
        try {
            String requestId = (String) requestBody.get("request_id");
            validateMap(requestId, requestBody);
            Map<String, Object> payloadMap = beneficiaryService.getPayloadMap(requestId);
            Bundle parsed = parser.parseResource(Bundle.class, (String) payloadMap.get("request_fhir"));
            String correlationId = (String) payloadMap.getOrDefault("correlation_id", "");
            Patient patient1 = parser.parseResource(Patient.class, parser.encodeResourceToString(parsed.getEntry().get(3).getResource()));
            String mobile = patient1.getTelecom().get(0).getValue();
            System.out.println("mobile number of beneficiary: " + mobile);
            String senderCode = (String) payloadMap.get("sender_code");
            String recipientCode = (String) payloadMap.get("recipient_code");
            HCXIntegrator hcxIntegrator = hcxIntegratorService.getHCXIntegrator(recipientCode);
            System.out.println("hcx integrtor -----------recipient code ---------" + hcxIntegrator.getParticipantCode());
            System.out.println("hcx intergrator --------------protocol base path " + hcxIntegrator.getHCXProtocolBasePath());
            System.out.println(" hcx integrator ---------username -------------" + hcxIntegrator.getUsername());
            System.out.println("hcx integrator --- password --------------" + hcxIntegrator.getPassword());
            System.out.println("------hcx integrtor privatekey -----------------" + hcxIntegrator.getPrivateKey());
            CommunicationRequest communicationRequest = OnActionFhirExamples.communicationRequestExample();
            Patient patient = OnActionFhirExamples.patientExample();
            patient.getTelecom().add(new ContactPoint().setValue(mobile).setSystem(ContactPoint.ContactPointSystem.PHONE));
            if (requestBody.getOrDefault("type", "").equals("bank_details")) {
                communicationRequest.getPayload().add(new CommunicationRequest.CommunicationRequestPayloadComponent().setContent(new StringType("Please provide the bank details for claim to be complete.")));
                System.out.println("The Communication request has been sent successfully bank details.");
            } else if (requestBody.getOrDefault("type", "").equals("otp")) {
                communicationRequest.getPayload().add(new CommunicationRequest.CommunicationRequestPayloadComponent().setContent(new StringType("Please verify the OTP sent to your mobile number to proceed.")));
                beneficiaryService.sendOTP(mobile, communicationContent);
                System.out.println("The otp has been sent for the beneficiary mobile to verify cliam.");
            }
            Map<String, Object> output = new HashMap<>();
            String workflowId = (String) payloadMap.getOrDefault("workflow_id", "");
            System.out.println("workflow id -----------------------" + workflowId);
            hcxIntegrator.processOutgoingRequest(parser.encodeResourceToString(communicationRequest), operations, senderCode, "", correlationId, workflowId, new HashMap<>(), output);
            System.out.println("The outgoing request has been successfully generated." + output);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    public ResponseEntity<Object> createCommunicationOnRequest(Map<String, Object> requestBody) throws Exception {
        String requestId = (String) requestBody.getOrDefault("request_id", "");
        if (StringUtils.equalsIgnoreCase((String) requestBody.get("type"), "otp")) {
            ResponseEntity<Object> responseEntity = beneficiaryService.verifyOTP(requestBody);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String query = String.format("UPDATE %s SET otp_verification = '%s' WHERE request_id = '%s'", payorDataTable, "successful", requestId);
                postgresService.execute(query);
                processOutgoingCallbackCommunication("otp", requestId, (String) requestBody.get("otp_code"), "", "");
            } else {
                throw new ClientException(Objects.requireNonNull(responseEntity.getBody()).toString());
            }
            return responseEntity;
        } else if (StringUtils.equalsIgnoreCase((String) requestBody.get("type"), "bank_details")) {
            String accountNumber = (String) requestBody.getOrDefault("account_number", "");
            String ifscCode = (String) requestBody.getOrDefault("ifsc_code", "");
            String query = String.format("UPDATE %s SET account_number ='%s',ifsc_code = '%s' WHERE request_id = '%s'", payorDataTable, accountNumber, ifscCode, requestId);
            postgresService.execute(query);
            System.out.println("The bank details updated successfully to the request id " + requestId);
            processOutgoingCallbackCommunication("bank_details", requestId, "", accountNumber, ifscCode);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return ResponseEntity.badRequest().body("Unable to update the details to database");
    }

    public void processOutgoingCallbackCommunication(String type, String requestId, String otpCode, String accountNumber, String ifscCode) throws Exception {
        Communication communication;
        List<DomainResource> domList = new ArrayList<>();
        if (type.equalsIgnoreCase("otp")) {
            communication = OnActionFhirExamples.communication();
            communication.getPayload().add(new Communication.CommunicationPayloadComponent().setContent(new StringType().setValue(otpCode)));
        } else {
            communication = OnActionFhirExamples.communication();
            communication.getPayload().add(new Communication.CommunicationPayloadComponent().setContent(new StringType().setValue(accountNumber)));
            communication.getPayload().add(new Communication.CommunicationPayloadComponent().setContent(new StringType().setValue(ifscCode)));
        }
        String searchCorrelationIdQuery = String.format("SELECT correlation_id FROM %s WHERE request_id = '%s'", payorDataTable, requestId);
        ResultSet resultSet = postgresService.executeQuery(searchCorrelationIdQuery);
        String correlationId = "";
        String senderCode = "";
        while (resultSet.next()) {
            correlationId = resultSet.getString("correlation_id");
            senderCode = resultSet.getString("sender_code");
        }
        HCXIntegrator hcxIntegrator = hcxIntegratorService.getHCXIntegrator(senderCode);
        String searchActionJweQuery = String.format("SELECT raw_payload from %s where correlation_id = '%s' AND action = 'communication'", payorDataTable, correlationId);
        ResultSet resultSet1 = postgresService.executeQuery(searchActionJweQuery);
        String rawPayload = "";
        while (resultSet1.next()) {
            rawPayload = resultSet1.getString("raw_payload");
        }
        Bundle bundleTest = new Bundle();
        try {
            bundleTest = HCXFHIRUtils.resourceToBundle(communication, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CommunicationBundle.html", hcxIntegrator);
            System.out.println("resource To Bundle communication Request\n" + parser.encodeResourceToString(bundleTest));
        } catch (Exception e) {
            System.out.println("Error message " + e.getMessage());
        }

        Map<String, Object> outputMap = new HashMap<>();
        hcxIntegrator.processOutgoingCallback(parser.encodeResourceToString(bundleTest), Operations.COMMUNICATION_ON_REQUEST, "", rawPayload, "response.complete", new HashMap<>(), outputMap);
    }

    public Map<String, Object> initializingConfigMap() throws IOException {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", protocolBasePath);
        configMap.put("participantCode", beneficiaryParticipantCode);
        configMap.put("username", beneficiaryUserName);
        configMap.put("password", beneficiaryPassword);
        String keyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/private-key.pem";
        String certificate = IOUtils.toString(new URL(keyUrl), StandardCharsets.UTF_8);
        configMap.put("encryptionPrivateKey", certificate);
        configMap.put("signingPrivateKey", certificate);
        return configMap;
    }

    protected ResponseEntity<Object> exceptionHandler(Response response, Exception e) {
        e.printStackTrace();
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

    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e) {
        ResponseError error = new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }

    protected void validateMap(String field, Map<String, Object> value) throws ClientException {
        if (MapUtils.isEmpty(value))
            throw new ClientException("Missing required field " + field);
    }

    public Map<String, Object> initializingConfigMapForPayor() throws IOException {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", protocolBasePath);
        configMap.put("participantCode", mockRecipientCode);
        configMap.put("username", payorUsername);
        configMap.put("password", payorPassword);
        String keyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/private-key.pem";
        String certificate = IOUtils.toString(new URL(keyUrl), StandardCharsets.UTF_8);
        configMap.put("encryptionPrivateKey", certificate);
        configMap.put("signingPrivateKey", certificate);
        return configMap;
    }

    public void getSenderAndRecipientCode(String senderCode) throws ClientException, SQLException {
        String query = String.format("SELECT count(*) count from %s WHERE parent_participant_code = '%s'", payorDataTable, senderCode);
        String childCodeQuery = String.format("SELECT child_participant_code from %s WHERE parent_participant_code = '%s'", payorDataTable, senderCode);
        ResultSet resultSet = postgresService.executeQuery(query);
        while (resultSet.next()) {
            int count = resultSet.getInt("count");
            if (count >= 2) {
                try (ResultSet childCodeResultSet = postgresService.executeQuery(childCodeQuery)) {
                    while (childCodeResultSet.next()) {
                        String childCode = childCodeResultSet.getString("child_participant_code");
                        System.out.println("Child Participant Code: " + childCode);
                    }
                }
            } else {
                throw new RuntimeException("Mock participants not found in the database");
            }
        }
//        String query = String.format("SELECT sender_code,recipient_code FROM %s WHERE request_id = '%s'", payorDataTable, requestId);
//        ResultSet result = postgresService.executeQuery(query);
//        Map<String, Object> senderRecipientDetails = new HashMap<>();
//        while (result.next()) {
//            senderRecipientDetails.put("sender_code", result.getString("sender_code"));
//            senderRecipientDetails.put("recipient_code", result.getString("recipient_code"));
//        }
//        System.out.println("Get sender and recipient Code-------" +  senderRecipientDetails);
//        return senderRecipientDetails;

    }
}

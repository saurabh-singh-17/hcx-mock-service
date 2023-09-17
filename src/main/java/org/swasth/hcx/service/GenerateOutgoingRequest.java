package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.dynamodbv2.xspec.S;
import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.utils.Operations;
import kong.unirest.HttpResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
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
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.HCXFHIRUtils;
import org.swasth.hcx.utils.HttpUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.swasth.hcx.utils.Constants.CREATE_COMMUNICATION_REQUEST;
import static org.swasth.hcx.utils.Constants.MOBILE;

@Service
public class GenerateOutgoingRequest {

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Value("${phone.communication-content}")
    private String communicationContent;
    @Value("${beneficiary.protocol-base-path}")
    private String protocolBasePath;
    @Value("${beneficiary.participant-code}")
    private String participantCode;
    @Value("${beneficiary.username}")
    private String userName;
    @Value("${beneficiary.password}")
    private String password;
    @Value("${beneficiary.recipient-code}")
    private String mockRecipientCode;

  public ResponseEntity<Object> createCoverageEligibilityRequest(Map<String, Object> requestBody, Operations operations) {
        Response response = new Response();
        try {
            HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance(initializingConfigMap());
            IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
            CoverageEligibilityRequest ce = OnActionFhirExamples.coverageEligibilityRequestExample();
            Practitioner practitioner = OnActionFhirExamples.practitionerExample();
            Organization hospital = OnActionFhirExamples.providerOrganizationExample();
            hospital.setName("WeMeanWell Hospital(provider name)");
            Patient patient = OnActionFhirExamples.patientExample();
            patient.getTelecom().add(new ContactPoint().setValue("9008496789 ()patient mobile" ).setSystem(ContactPoint.ContactPointSystem.PHONE));        String date_string = "26-09-1960";
            patient.getName().add(new HumanName().setText("Abhishek(patient name)"));
            Organization insurerOrganization = OnActionFhirExamples.insurerOrganizationExample();
            insurerOrganization.setName("Mock Payor HCX(payor code)");
            Coverage coverage = OnActionFhirExamples.coverageExample();
            List<DomainResource> domList = List.of(hospital, insurerOrganization, patient, coverage, practitioner);
            Bundle bundleTest = new Bundle();
            try {
                bundleTest = HCXFHIRUtils.resourceToBundle(ce, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html",hcxIntegrator);
                System.out.println("reosurceToBundle Coverage Eligibility Request \n" + parser.encodeResourceToString(bundleTest));
            } catch (Exception e) {
                System.out.println("Error message " + e.getMessage());
            }
            Map<String, Object> output = new HashMap<>();
            hcxIntegrator.processOutgoingRequest(parser.encodeResourceToString(bundleTest), operations, mockRecipientCode, "", "", new HashMap<>(), output);
            System.out.println("The outgoing request has been successfully generated.");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    public ResponseEntity<Object> createClaimRequest(Map<String, Object> requestBody, Operations operations) {
        Response response = new Response();
        try {
            HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance(initializingConfigMap());
            IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
            Claim claim = OnActionFhirExamples.claimExample();
            Practitioner practitioner = OnActionFhirExamples.practitionerExample();
            Organization hospital = OnActionFhirExamples.providerOrganizationExample();
            Patient patient = OnActionFhirExamples.patientExample();
            Organization insurerOrganization = OnActionFhirExamples.insurerOrganizationExample();
            Coverage coverage = OnActionFhirExamples.coverageExample();
            List<DomainResource> domList = List.of(hospital, insurerOrganization, patient, coverage, practitioner);
            Bundle bundleTest = new Bundle();
            try {
                bundleTest = HCXFHIRUtils.resourceToBundle(claim, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html", hcxIntegrator);
                System.out.println("Resource To Bundle generated successfully");
            } catch (Exception e) {
                System.out.println("Error message " + e.getMessage());
            }
            Map<String, Object> output = new HashMap<>();
            hcxIntegrator.processOutgoingRequest(parser.encodeResourceToString(bundleTest), operations, mockRecipientCode, "", "", new HashMap<>(), output);
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
            HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance(initializingConfigMap());
            IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
            CommunicationRequest communicationRequest = OnActionFhirExamples.communicationRequestExample();
            Patient patient = OnActionFhirExamples.patientExample();
            List<DomainResource> domList = List.of(patient);
            Bundle bundleTest = new Bundle();
            try {
                bundleTest = HCXFHIRUtils.resourceToBundle(communicationRequest, domList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CommunicationRequest.html", hcxIntegrator);
                System.out.println("Resource To Bundle generated successfully\n" + parser.encodeResourceToString(bundleTest));
            } catch (Exception e) {
                System.out.println("Error message " + e.getMessage());
            }
            Map<String, Object> output = new HashMap<>();
            hcxIntegrator.processOutgoingRequest(parser.encodeResourceToString(bundleTest), operations, mockRecipientCode, "", "", new HashMap<>(), output);
            System.out.println("The outgoing request has been successfully generated.");
            beneficiaryService.sendOTP((String) requestBody.get(MOBILE), communicationContent);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    public Map<String, Object> initializingConfigMap() throws IOException {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", protocolBasePath);
        configMap.put("participantCode", participantCode);
        configMap.put("username", userName);
        configMap.put("password", password);
        String keyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/x509-private-key.pem";
        String certificate = IOUtils.toString(new URL(keyUrl), StandardCharsets.UTF_8);
        configMap.put("encryptionPrivateKey", certificate);
        configMap.put("signingPrivateKey",  certificate);
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


}

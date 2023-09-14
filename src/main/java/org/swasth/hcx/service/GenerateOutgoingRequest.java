package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.dynamodbv2.xspec.S;
import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.utils.Operations;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.swasth.hcx.utils.Constants.CREATE_COMMUNICATION_REQUEST;

@Service
public class GenerateOutgoingRequest {

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

    public GenerateOutgoingRequest() throws IOException {
    }


    public ResponseEntity<Object> processOutgoingRequest(Map<String, Object> requestBody, String apiAction, Operations operations) {
        Response response = new Response();
        try {
            HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance(initializingConfigMap());
            String fhirPayload = createFHIRBundle(apiAction, hcxIntegrator, requestBody);
            System.out.println("---------fhir payload ------" + fhirPayload);
            Map<String, Object> output = new HashMap<>();
            hcxIntegrator.processOutgoingRequest(fhirPayload, operations, mockRecipientCode, "", "", new HashMap<>(), output);
            System.out.println("The outgoing request has been successfully generated.");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    String keyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/main/hcx-apis/src/test/resources/examples/test-keys/private-key.pem";
    String certificate = IOUtils.toString(new URL(keyUrl), StandardCharsets.UTF_8.toString());

    public Map<String, Object> initializingConfigMap() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", protocolBasePath);
        configMap.put("participantCode", participantCode);
        configMap.put("username", userName);
        configMap.put("password", password);
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

    private List<DomainResource> createDomainResourceList(Map<String, Object> requestBody) {
        Practitioner practitioner = OnActionFhirExamples.practitionerExample();
        Organization hospital = OnActionFhirExamples.providerOrganizationExample();
        Patient patient = OnActionFhirExamples.patientExample();
        Organization insurerOrganization = OnActionFhirExamples.insurerOrganizationExample();
        Coverage coverage = OnActionFhirExamples.coverageExample();
        return List.of(hospital, insurerOrganization, patient, coverage, practitioner);
    }

    private String createFHIRBundle(String apiAction, HCXIntegrator hcxIntegrator, Map<String, Object> requestBody) throws Exception {
        IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
        Bundle bundleTest = new Bundle();
        if (StringUtils.equalsIgnoreCase(apiAction, Constants.CREATE_COVERAGEELIGIBILITY_REQUEST)) {
            CoverageEligibilityRequest ce = OnActionFhirExamples.coverageEligibilityRequestExample();
            List<DomainResource> domainList = createDomainResourceList(requestBody);
            bundleTest = HCXFHIRUtils.resourceToBundle(ce, domainList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html", hcxIntegrator);
        } else if (StringUtils.equalsIgnoreCase(apiAction, Constants.CREATE_CLAIM_SUBMIT)) {
            Claim claim = OnActionFhirExamples.claimExample();
            List<DomainResource> domainList = createDomainResourceList(requestBody);
            bundleTest = HCXFHIRUtils.resourceToBundle(claim, domainList, Bundle.BundleType.COLLECTION, "https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-ClaimRequestBundle.html", hcxIntegrator);
        } else if (StringUtils.equalsIgnoreCase(apiAction, CREATE_COMMUNICATION_REQUEST)) {
            CommunicationRequest communication = OnActionFhirExamples.communicationRequestExample();

        }
        return parser.encodeResourceToString(bundleTest);
    }

}

package org.swasth.hcx.controllers.v1;

import com.amazonaws.services.s3.model.ObjectMetadata;
import io.hcxprotocol.utils.Operations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.service.BeneficiaryService;
import org.swasth.hcx.service.CloudStorageClient;
import org.swasth.hcx.service.GenerateOutgoingRequest;
import org.swasth.hcx.utils.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.hcx.utils.Constants.*;

@RestController
@RequestMapping(Constants.VERSION_PREFIX)
public class BeneficiaryController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BeneficiaryController.class);
    @Autowired
    private GenerateOutgoingRequest outgoingRequest;

    @Value("${certificates.bucketName}")
    private String bucketName;
    @Autowired
    private CloudStorageClient cloudStorageClient;
    @Autowired
    private BeneficiaryService beneficiaryService;
    @Value("${phone.beneficiary-register}")
    private String beneficiaryRegisterContent;

    @Value("${phone.communication-content}")
    private String communicationContent;

    @Value("${postgres.table.beneficiary}")
    private String beneficiaryTable;

    @PostMapping(CREATE_COVERAGEELIGIBILITY_REQUEST)
    public ResponseEntity<Object> createCoverageEligibility(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return outgoingRequest.createCoverageEligibilityRequest(requestBody,Operations.COVERAGE_ELIGIBILITY_CHECK);
    }

    @PostMapping(CREATE_CLAIM_SUBMIT)
    public ResponseEntity<Object> createClaimSubmit(@RequestBody Map<String, Object> requestBody) {
        return outgoingRequest.createClaimRequest(requestBody, Operations.CLAIM_SUBMIT);
    }

    @PostMapping(CREATE_PRE_AUTH_SUBMIT)
    public ResponseEntity<Object> createPreAuthSubmit(@RequestBody Map<String, Object> requestBody) {
        return outgoingRequest.createClaimRequest(requestBody, Operations.PRE_AUTH_SUBMIT);
    }

    @PostMapping(CREATE_COMMUNICATION_REQUEST)
    public ResponseEntity<Object> createCommunication(@RequestBody Map<String, Object> requestBody) {
        return outgoingRequest.createCommunicationRequest(requestBody, Operations.COMMUNICATION_REQUEST);
    }

    @PostMapping(CREATE_COMMUNICATION_ON_REQUEST)
    public ResponseEntity<Object> createOnCommunication(@RequestBody Map<String, Object> requestBody) {
        return outgoingRequest.createCommunicationOnRequest(requestBody, Operations.COMMUNICATION_REQUEST);
    }

    @PostMapping(BSP_REQUEST_LIST)
    public ResponseEntity<Object> claimList(@RequestBody Map<String, Object> requestBody) throws Exception {
        return beneficiaryService.getRequestFromDatabase(requestBody);
    }

    @PostMapping(SEND_OTP)
    public ResponseEntity<Object> sendOTP(@RequestBody Map<String, Object> requestBody) {
        try {
            String mobile = (String) requestBody.get(MOBILE);
            beneficiaryService.sendOTP(mobile, beneficiaryRegisterContent);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "mobile", mobile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(VERIFY_OTP)
    public ResponseEntity<Object> verifyOTP(@RequestBody Map<String, Object> requestBody) {
        try {
            return beneficiaryService.verifyOTP(requestBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upload/documents")
    public ResponseEntity<Object> uploadFile(@RequestHeader HttpHeaders headers, @RequestParam("file") MultipartFile file, @RequestParam("folderName") String folderName) throws IOException {
        try {
            String fileName = file.getOriginalFilename();
            cloudStorageClient.putObject(folderName, bucketName);
            String pathToFile = "beneficiary-app" + "/" + folderName + "/" + fileName;
            cloudStorageClient.putObject(bucketName, pathToFile, file);
            Map<String, Object> response = new HashMap<>();
            response.put("url", cloudStorageClient.getUrl(bucketName, pathToFile).toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

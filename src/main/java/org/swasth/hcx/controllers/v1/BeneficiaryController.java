package org.swasth.hcx.controllers.v1;

import io.hcxprotocol.utils.Operations;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.service.BeneficiaryService;
import org.swasth.hcx.service.GenerateOutgoingRequest;
import org.swasth.hcx.utils.Constants;

import java.util.Map;

import static org.swasth.hcx.utils.Constants.*;

@RestController
@RequestMapping(Constants.VERSION_PREFIX)
public class BeneficiaryController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BeneficiaryController.class);
    @Autowired
    private GenerateOutgoingRequest outgoingRequest;

    @Autowired
    private BeneficiaryService beneficiaryService;
    @Value("${phone.content}")
    private String phoneContent;

    @Value("${postgres.table.beneficiary}")
    private String beneficiaryTable;

    @Value("${verification-otp.expiry}")
    private int otpExpiry;

    @PostMapping(CREATE_COVERAGEELIGIBILITY_REQUEST)
    public ResponseEntity<Object> createCoverageEligibility(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Object> requestBody) throws Exception {
        return outgoingRequest.createCoverageEligibilityRequest(requestBody,Operations.COVERAGE_ELIGIBILITY_CHECK);
    }

    @PostMapping(CREATE_CLAIM_SUBMIT)
    public ResponseEntity<Object> createClaimSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        return outgoingRequest.createClaimRequest(requestBody, Operations.CLAIM_SUBMIT);
    }

//    @PostMapping(CREATE_PRE_AUTH_SUBMIT)
//    public ResponseEntity<Object> createPreAuthSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
//        return outgoingRequest.processOutgoingRequest(requestBody, CREATE_PRE_AUTH_SUBMIT, Operations.PRE_AUTH_SUBMIT);
//    }

    @PostMapping(CREATE_COMMUNICATION_REQUEST)
    public ResponseEntity<Object> createCommunication(@RequestBody Map<String, Object> requestBody) throws Exception {
        return outgoingRequest.communicationRequest(requestBody, Operations.COMMUNICATION_REQUEST);
    }

    @PostMapping("/send/otp")
    public ResponseEntity<Object> sendOTP(@RequestBody Map<String, Object> requestBody) {
        try {
            String mobile = (String) requestBody.get(MOBILE);
            beneficiaryService.sendOTP(mobile);
            return ResponseEntity.ok("OTP sent successfully to " + mobile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("verify/otp")
    public ResponseEntity<Object> verifyOTP(@RequestBody Map<String, Object> requestBody) {
        try {
            return beneficiaryService.verifyOTP(requestBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

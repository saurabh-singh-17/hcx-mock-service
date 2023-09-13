package org.swasth.hcx.controllers.v1;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.hcxprotocol.exception.ClientException;
import io.hcxprotocol.utils.Operations;
import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.r4.model.*;
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
import org.swasth.hcx.service.PostgresService;
import org.swasth.hcx.service.SMSService;
import org.swasth.hcx.utils.Constants;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
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
        return outgoingRequest.processOutgoingRequest(requestBody, Constants.COVERAGE_ELIGIBILITY_CHECK , Operations.COVERAGE_ELIGIBILITY_CHECK);
    }

    @PostMapping(CREATE_CLAIM_SUBMIT)
    public ResponseEntity<Object> createClaimSubmit(@RequestBody Map<String,Object> requestBody) throws Exception {
        return outgoingRequest.processOutgoingRequest(requestBody, Constants.CLAIM_SUBMIT, Operations.CLAIM_SUBMIT);
    }

    @PostMapping(CREATE_PRE_AUTH_SUBMIT)
    public ResponseEntity<Object> createPreAuthSubmit(@RequestBody Map<String,Object> requestBody) throws Exception {
        return outgoingRequest.processOutgoingRequest(requestBody, PRE_AUTH_SUBMIT, Operations.PRE_AUTH_SUBMIT);
    }

}

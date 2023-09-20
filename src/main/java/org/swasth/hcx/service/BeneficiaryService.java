package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.utils.Constants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BeneficiaryService {

    @Value("${postgres.table.payerData}")
    private String payorDataTable;
    @Autowired
    private PostgresService postgresService;
    @Autowired
    private SMSService smsService;
    @Value("${postgres.table.beneficiary}")
    private String beneficiaryTable;
    @Value("${otp.expiry}")
    private int otpExpiry;
    @Value("${otp.send-per-minute}")
    private int otpSendPerMinute;
    long lastOTPSendTime = 0;
    int otpSentThisMinute = 0;

    public void sendOTP(String mobile, String phoneContent) throws ClientException, SQLException {
        if (!isRateLimited()) {
            try {
                int otpCode = 100_000 + new Random().nextInt(900_000);
                String query = String.format("SELECT * FROM %s WHERE mobile = '%s'", beneficiaryTable, mobile);
                ResultSet resultSet = postgresService.executeQuery(query);
                if (!resultSet.next()) {
                    String insertQuery = String.format("INSERT INTO %s (mobile, otp_code, mobile_verified, createdon, otp_expiry) VALUES ('%s', %d, false, %d, %d)", beneficiaryTable, mobile, otpCode, System.currentTimeMillis(), System.currentTimeMillis() + otpExpiry);
                    postgresService.execute(insertQuery);
                    smsService.sendSMS(mobile, phoneContent + "\r\n" + otpCode);
                    System.out.println("OTP sent successfully for " + mobile);
                } else {
                    String updateQuery = String.format("UPDATE %s SET otp_code = %d, otp_expiry = %d , mobile_verified = %b WHERE mobile = '%s'", beneficiaryTable, otpCode, System.currentTimeMillis() + otpExpiry, false, mobile);
                    postgresService.execute(updateQuery);
                    smsService.sendSMS(mobile, phoneContent + "\r\n" + otpCode);
                    System.out.println("OTP sent successfully for " + mobile);
                }
                lastOTPSendTime = System.currentTimeMillis();
                otpSentThisMinute++;
            } catch (ClientException e) {
                throw new ClientException(e.getMessage());
            }
        }
    }

    private boolean isRateLimited() throws ClientException {
        if (System.currentTimeMillis() - lastOTPSendTime >= 60000) {
            otpSentThisMinute = 0; // Reset the count if a minute has passed
        }
        if (otpSentThisMinute >= otpSendPerMinute) {
            System.out.println("Rate limit exceeded. Try again later.");
            throw new ClientException(ErrorCodes.ERR_SENDING_OTP, "Rate limit exceeded. Try again later.");
        }
        return false;
    }

    public ResponseEntity<Object> verifyOTP(Map<String, Object> requestBody) {
        try {
            String mobile = (String) requestBody.get(Constants.MOBILE);
            int userEnteredOTP = Integer.parseInt((String) requestBody.get("otp_code"));
            String query = String.format("SELECT * FROM %s WHERE mobile = '%s'", beneficiaryTable, mobile);
            ResultSet resultSet = postgresService.executeQuery(query);
            if (resultSet.next()) {
                boolean isMobileVerified = resultSet.getBoolean("mobile_verified");
                int storedOTP = resultSet.getInt("otp_code");
                long otpExpire = resultSet.getLong("otp_expiry");
                if (isMobileVerified) {
                    return ResponseEntity.badRequest().body(response("Mobile Number already verified", mobile, "failed"));
                }
                if (userEnteredOTP != storedOTP || System.currentTimeMillis() > otpExpire) {
                    throw new io.hcxprotocol.exception.ClientException("Invalid OTP or OTP has expired");
                }
                // Update mobile_verified status
                String updateQuery = String.format("UPDATE %s SET mobile_verified = true WHERE mobile = '%s'", beneficiaryTable, mobile);
                postgresService.execute(updateQuery);
                return ResponseEntity.ok().body(response("verification is successful", mobile, "successful"));
            } else {
                return ResponseEntity.badRequest().body(response("Record does not exist in the database", mobile, "failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public Map<String,Object> response(String message  , String mobile,String verification){
        Map<String,Object> response = new HashMap<>();
        response.put("message",message);
        response.put("mobile",mobile);
        response.put("verification",verification);
        return response;
    }

    public Map<String,Object> getPayloadMap(String requestID) throws ClientException, SQLException {
        String searchQuery = String.format("SELECT * FROM %s WHERE request_id = '%s'", payorDataTable, requestID);
        ResultSet resultSet = postgresService.executeQuery(searchQuery);
        if (!resultSet.next()) {
            throw new ClientException("Request does not exist in the database");
        }
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("request_fhir", resultSet.getString("request_fhir"));
        payloadMap.put("correlation_id", resultSet.getString("correlation_id"));
        return payloadMap;
    }


    public ResponseEntity<Object> getClaimCycles(Map<String, Object> requestBody) throws Exception {
        String mobile = (String) requestBody.getOrDefault("mobile", "");
        List<Object> result = new ArrayList<>();
        String countQuery = String.format("SELECT COUNT(*) AS count FROM payersystem_data WHERE mobile = '%s'", mobile);
        ResultSet resultSet = postgresService.executeQuery(countQuery);
        Map<String, Object> resp = new HashMap<>();
        int count;
        if (resultSet.next()) {
            count = resultSet.getInt("count");
            resp.put("Total count of the mobile number : " + mobile + "  :", count);
        }
        String searchQuery = String.format("SELECT * FROM payersystem_data WHERE mobile = '%s'", mobile);
        ResultSet resultSet1 = postgresService.executeQuery(searchQuery);
        List<Map<String, Object>> coverageEligibility = new ArrayList<>();
        List<Map<String, Object>> claim = new ArrayList<>();
        List<Map<String, Object>> preauth = new ArrayList<>();
        while (resultSet1.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", resultSet1.getString("status"));
            map.put("claimID", resultSet1.getString("request_id"));
            map.put("claimType", "OPD");
            map.put("date", resultSet1.getString("created_on"));
            map.put("insurance_id", getInsuranceId(resultSet1.getString("request_fhir")));
            if (resultSet1.getString("action").equalsIgnoreCase("coverageeligibility")) {
                coverageEligibility.add(map);
            } else if (resultSet1.getString("action").equalsIgnoreCase("claim")) {
                claim.add(map);
            } else {
                preauth.add(map);
            }
            resp.put("coverageEligibility", coverageEligibility);
            resp.put("claim", claim);
            resp.put("preauth", preauth);
        }
        resp.put("request", result);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public String getInsuranceId(String fhirPayload) {
        IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        Coverage coverage = parser.parseResource(Coverage.class, parser.encodeResourceToString(parsed.getEntry().get(4).getResource()));
        System.out.println("------coverage subscriber ID ---------------" + coverage.getSubscriberId());
        return coverage.getSubscriberId();
    }
}

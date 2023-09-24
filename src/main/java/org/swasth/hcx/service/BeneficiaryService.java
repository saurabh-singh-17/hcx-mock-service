package org.swasth.hcx.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.utils.Constants;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class BeneficiaryService {

    @Value("${postgres.table.payerData}")
    private String payorDataTable;
    @Autowired
    private PostgresService postgresService;

    @Value("${certificates.bucketName}")
    private String bucketName;
    @Autowired
    private CloudStorageClient cloudStorageClient;
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
                    String beneficiaryReferenceId = String.valueOf(UUID.randomUUID());
                    String insertQuery = String.format("INSERT INTO %s (mobile, otp_code, mobile_verified, createdon, otp_expiry, bsp_reference_id) VALUES ('%s', %d, false, %d, %d, '%s')", beneficiaryTable, mobile, otpCode, System.currentTimeMillis(), System.currentTimeMillis() + otpExpiry, beneficiaryReferenceId);
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


    public ResponseEntity<Object> getRequestListFromDatabase(Map<String, Object> requestBody) throws Exception {
        String mobile = (String) requestBody.getOrDefault("mobile", "");
        String countQuery = String.format("SELECT COUNT(*) AS count FROM %s WHERE mobile = '%s'", payorDataTable, mobile);
        ResultSet resultSet = postgresService.executeQuery(countQuery);
        Map<String, Object> resp = new HashMap<>();
        int count;
        if (resultSet.next()) {
            count = resultSet.getInt("count");
            resp.put("count", count);
            System.out.println("Total count of the requests : " + count);
        }
        List<Map<String, Object>> entries = new ArrayList<>();
        String searchQuery = String.format("SELECT * FROM %s WHERE mobile = '%s' ORDER BY created_on DESC", payorDataTable, mobile);
        ResultSet searchResultSet = postgresService.executeQuery(searchQuery);
        while (searchResultSet.next()) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("type", getType(searchResultSet.getString("action")));
            responseMap.put("status", searchResultSet.getString("status"));
            responseMap.put("claimID", searchResultSet.getString("request_id"));
            responseMap.put("claimType", "OPD");
            responseMap.put("date", searchResultSet.getString("created_on"));
            responseMap.put("insurance_id", getInsuranceId(searchResultSet.getString("request_fhir")));
            responseMap.put("correlationId", searchResultSet.getString("correlation_id"));
            responseMap.put("sender_code", searchResultSet.getString("sender_code"));
            responseMap.put("recipient_code", searchResultSet.getString("recipient_code"));
            if(!searchResultSet.getString("action").equalsIgnoreCase("coverageeligibility")) {
                responseMap.put("billAmount", getAmount(searchResultSet.getString("request_fhir")));
            }
//            responseMap.put("supportingDocuments", )
            entries.add(responseMap);
        }
        resp.put("entries", entries);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }


    private String getType(String action) {
        if ("coverageeligibility".equalsIgnoreCase(action)) {
            return "coverageEligibility";
        } else if ("claim".equalsIgnoreCase(action)) {
            return "claim";
        } else {
            return "preauth"; // You can add more handling as needed
        }
    }


    public String getInsuranceId(String fhirPayload) {
        IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        Coverage coverage = parser.parseResource(Coverage.class, parser.encodeResourceToString(parsed.getEntry().get(4).getResource()));
        return coverage.getSubscriberId();
    }

    public String getAmount(String fhirPayload) {
        IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
        Claim claim = parser.parseResource(Claim.class, parser.encodeResourceToString(parsed.getEntry().get(0).getResource()));
        return claim.getTotal().getValue().toString();
    }

//    public void getSupportingDocuments(String fhirPayload) {
//        IParser parser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
//        Bundle parsed = parser.parseResource(Bundle.class, fhirPayload);
//    }

    public List<Map<String, Object>> getDocumentUrls(List<MultipartFile> files, String mobile) throws ClientException, SQLException, IOException {
        String query = String.format("SELECT bsp_reference_id FROM %s WHERE mobile = '%s'", beneficiaryTable, mobile);
        ResultSet resultSet = postgresService.executeQuery(query);
        String beneficiaryReferenceId = "";
        while (resultSet.next()) {
            beneficiaryReferenceId = resultSet.getString("bsp_reference_id");
        }
        List<Map<String, Object>> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            cloudStorageClient.putObject(beneficiaryReferenceId, bucketName);
            String pathToFile = "beneficiary-app" + "/" + beneficiaryReferenceId + "/" + fileName;
            cloudStorageClient.putObject(bucketName, pathToFile, file);
            Map<String, Object> response = new HashMap<>();
            response.put("url", cloudStorageClient.getUrl(bucketName, pathToFile).toString());
            response.put("reference_id", beneficiaryReferenceId);
            responses.add(response);
        }
        return responses;
    }

    public boolean checkCommunicationRequest(String requestId) throws ClientException, SQLException {
        String query = String.format("SELECT otp_verification FROM %s WHERE request_id = '%s'", payorDataTable, requestId);
        ResultSet resultSet = postgresService.executeQuery(query);
        String status;
        if (resultSet.next()) {
            status = resultSet.getString("otp_verification");
            return status.equalsIgnoreCase("initiated");
        } else {
            return false;
        }
    }


}

package org.swasth.hcx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.utils.Constants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

@Service
public class BeneficiaryService {

    @Autowired
    private PostgresService postgresService;
    @Autowired
    private SMSService smsService;
    @Value("${postgres.table.beneficiary}")
    private String beneficiaryTable;
    @Value("${verification-otp.expiry}")
    private int otpExpiry;

    public void sendOTP(String mobile, String phoneContent) throws ClientException, SQLException {
        try {
            String query = String.format("SELECT * FROM %s WHERE mobile = '%s'", beneficiaryTable, mobile);
            ResultSet resultSet = postgresService.executeQuery(query);
            if (!resultSet.next()) {
                createUserAndSendOTP(mobile, phoneContent);
            } else {
                sendOTPToExistingUser(mobile, phoneContent);
            }
        } catch (ClientException e) {
            throw new ClientException(e.getMessage());
        }
    }

    private void sendOTPToExistingUser(String mobile, String phoneContent) throws ClientException {
        int otpCode = 100_000 + new Random().nextInt(900_000);
        String query = String.format("UPDATE %s SET otp_code = %d, otp_expiry = %d WHERE mobile = '%s'", beneficiaryTable, otpCode, System.currentTimeMillis() + otpExpiry, mobile);
        try {
            postgresService.execute(query);
            smsService.sendSMS(mobile, phoneContent + "\r\n" + otpCode);
            System.out.println("OTP sent successfully for " + mobile);
        } catch (Exception e) {
            throw new ClientException(e.getMessage());
        }
    }

    private void createUserAndSendOTP(String mobile,String phoneContent) throws ClientException {
        int otpCode = 100_000 + new Random().nextInt(900_000);
        String query = String.format("INSERT INTO %s (mobile, otp_code, mobile_verified, createdon, otp_expiry) VALUES ('%s', %d, false, %d, %d)", beneficiaryTable, mobile, otpCode, System.currentTimeMillis(), System.currentTimeMillis() + otpExpiry);
        try {
            postgresService.execute(query);
            smsService.sendSMS(mobile, phoneContent + "\r\n" + otpCode);
            System.out.println("OTP sent successfully for " + mobile);
        } catch (Exception e) {
            throw new ClientException(e.getMessage());
        }
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
                    return ResponseEntity.badRequest().body(Map.of("message", "Mobile Number already verified", "verification", "failed", "mobile", mobile));
                }
                if (userEnteredOTP != storedOTP || System.currentTimeMillis() > otpExpire) {
                    throw new io.hcxprotocol.exception.ClientException("Invalid OTP or OTP has expired");
                }
                // Update mobile_verified status
                String updateQuery = String.format("UPDATE %s SET mobile_verified = true WHERE mobile = '%s'", beneficiaryTable, mobile);
                postgresService.execute(updateQuery);
                return ResponseEntity.ok().body(Map.of("mobile", mobile, "verification", "successful"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Record does not exist in the database", "verification", "failed", "mobile", mobile));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

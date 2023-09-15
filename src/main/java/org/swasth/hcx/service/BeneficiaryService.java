package org.swasth.hcx.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.utils.Constants;

import java.sql.ResultSet;
import java.util.Map;

@Service
public class BeneficiaryService {

    @Autowired
    private PostgresService postgresService;
    @Autowired
    private SMSService smsService;
    @Value("${phone.content}")
    private String phoneContent;
    @Value("${postgres.table.beneficiary}")
    private String beneficiaryTable;
    @Value("${verification-otp.expiry}")
    private int otpExpiry;

    public void sendOTP(String mobile) throws ClientException {
        Integer otpCode = Integer.valueOf(RandomStringUtils.randomNumeric(6));
        String query = String.format("INSERT INTO %s (mobile, otp_code, mobile_verified, createdon, otp_expiry) VALUES ('%s', %d, false, %d, %d)", beneficiaryTable, mobile, otpCode, System.currentTimeMillis(), System.currentTimeMillis() + otpExpiry);
        postgresService.execute(query);
        smsService.sendSMS(mobile, phoneContent + "\r\n" + otpCode);
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
                    return ResponseEntity.badRequest().body("Mobile Number already verified");
                }
                if (userEnteredOTP != storedOTP || System.currentTimeMillis() > otpExpire) {
                    throw new io.hcxprotocol.exception.ClientException("Invalid OTP or OTP has expired");
                }
                // Update mobile_verified status
                String updateQuery = String.format("UPDATE %s SET mobile_verified = true WHERE mobile = '%s'", beneficiaryTable, mobile);
                postgresService.execute(updateQuery);
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            } else {
                return ResponseEntity.badRequest().body("Record does not exist in the database");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

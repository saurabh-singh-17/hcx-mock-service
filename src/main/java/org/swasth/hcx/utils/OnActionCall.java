package org.swasth.hcx.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.swasth.hcx.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;

public class OnActionCall {

    public static void sendOnAction(String hcxBasePath, String onApiCall, Map<String, ?> encryptedPayload) throws Exception{
        System.out.println("Timestamp before on_action call: "+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        HttpResponse<String> response = Unirest.post("http://a9dd63de91ee94d59847a1225da8b111-273954130.ap-south-1.elb.amazonaws.com:8080/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token")
                .header("content-type", "application/x-www-form-urlencoded")
                .field("client_id", "registry-frontend")
                .field("username", "swasth_mock_payer@swasthapp.org")
                .field("password", "Opensaber@123")
                .field("grant_type", "password")
                .asString();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseBody = mapper.readValue(response.getBody(), Map.class);

        HttpResponse<String> onActionResponse = Unirest.post(hcxBasePath + onApiCall)
                .header("Authorization", "Bearer " + responseBody.get("access_token").toString())
                .header("Content-Type", "application/json")
                .body(encryptedPayload)
                .asString();

        Map<String, String> res = mapper.readValue(onActionResponse.getBody(), Map.class);
        System.out.println("response "+ res);
    }
}

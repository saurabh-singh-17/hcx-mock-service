package org.swasth.hcx.controllers.v1;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.swasth.hcx.utils.Constants.*;

@RestController()
@RequestMapping(value = "/"+ "${hcx_application.api_version}")
public class OnboardController extends BaseController {

    @PostMapping(value = "/applicant/verify")
    public ResponseEntity<Object> applicantVerify(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            Map<String,Object> resp = new HashMap<>();
            resp.put("timestamp", System.currentTimeMillis());
            resp.put(APPLICANT_CODE, requestBody.getOrDefault(APPLICANT_CODE, ""));
            resp.put(VERIFIER_CODE, requestBody.getOrDefault(VERIFIER_CODE, ""));
            resp.put(RESULT, ACCEPTED);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(value = "/applicant/getinfo")
    public ResponseEntity<Object> getInfo(@RequestHeader HttpHeaders header, @RequestBody Map<String, Object> requestBody) {
        try {
            String verifierCode = null;
            String applicantCode = null;
            if (requestBody.containsKey(VERIFICATION_TOKEN)) {
                String token = (String) requestBody.getOrDefault(VERIFICATION_TOKEN, "");
                Map<String, Object> jwtPayload = JSONUtils.decodeBase64String(token.split("\\.")[1], Map.class);
                verifierCode = (String) jwtPayload.get(ISS);
                applicantCode = (String) jwtPayload.get(SUB);
            } else if (requestBody.containsKey(MOBILE)) {
                verifierCode = (String) requestBody.get(VERIFIER_CODE);
            } else {
                applicantCode = (String) requestBody.get(APPLICANT_CODE);
                verifierCode = (String) requestBody.get(VERIFIER_CODE);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put(APPLICANT_CODE, applicantCode);
            resp.put(VERIFIER_CODE, verifierCode);
            resp.put("email", applicantCode + "@yopmail.com");
            resp.put(ADDITIONAL_VERIFICATION, new ArrayList<>());
            
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

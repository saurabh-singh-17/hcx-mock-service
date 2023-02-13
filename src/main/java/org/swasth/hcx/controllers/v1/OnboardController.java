package org.swasth.hcx.controllers.v1;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;

import java.util.HashMap;
import java.util.Map;

import static org.swasth.hcx.utils.Constants.*;

@RestController()
@RequestMapping(value = "/"+ "${hcx_application.api_version}")
public class OnboardController extends BaseController {

    @RequestMapping(value = "/applicant/verify", method = RequestMethod.POST)
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
}

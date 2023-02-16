package org.swasth.hcx.controllers.v1;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.common.exception.ClientException;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PayerController extends BaseController {

    @PostMapping(value = "/payer/request/list")
    public ResponseEntity<Object> requestList(@RequestBody Map<String, Object> requestBody) {
        try {
            String type = (String) requestBody.getOrDefault("type","");
            List<Object> result = new ArrayList<>();
            if (!StringUtils.isEmpty(type))
                result.addAll((List<Object>) JSONUtils.convertJson(getFileAsIOStream("fhir-objects/" + type + "-requests.json"), List.class));
            else
                throw new ClientException("Type field is missing");
            Map<String,Object> resp = new HashMap<>();
            resp.put(type, result);
            resp.put("count", result.size());
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(value = "/payer/request/approve")
    public ResponseEntity<Object> approve(@RequestBody Map<String, Object> requestBody) {
        try {
            Map<String,Object> resp = new HashMap<>();
            resp.put("timestamp", System.currentTimeMillis());
            resp.put("status", "successful");
            resp.put("reason", "");
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(value = "/payer/request/reject")
    public ResponseEntity<Object> reject(@RequestBody Map<String, Object> requestBody) {
        try {
            Map<String,Object> resp = new HashMap<>();
            resp.put("timestamp", System.currentTimeMillis());
            resp.put("status", "successful");
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

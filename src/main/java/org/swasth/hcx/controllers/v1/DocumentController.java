package org.swasth.hcx.controllers.v1;

import io.hcxprotocol.exception.ClientException;
import io.hcxprotocol.jwe.JweRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DocumentController extends BaseController {

    @PostMapping("/document/analyse/on_submit")
    public ResponseEntity<Object> analyseOnSubmit(@RequestBody List<Map<String, Object>> request) {
        try {
            if (request.isEmpty()) {
                throw new ClientException("Request body is empty");
            }

            String requestId = request.get(0).getOrDefault("request_id", "").toString();
            Map<String,Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            response.put("request_id", requestId);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

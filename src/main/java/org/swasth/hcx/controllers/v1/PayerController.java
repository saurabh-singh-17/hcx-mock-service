//package org.swasth.hcx.controllers.v1;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import org.swasth.common.exception.ClientException;
//import org.swasth.hcx.controllers.BaseController;
//import org.swasth.hcx.dto.Response;
//import org.swasth.hcx.utils.JSONUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//public class PayerController extends BaseController {
//
//    @PostMapping(value = "/payer/request/list")
//    public ResponseEntity<Object> requestList(@RequestBody Map<String, Object> requestBody) {
//        try {
//            String type = (String) requestBody.getOrDefault("type","");
//            List<Object> result = new ArrayList<>();
//            if (!StringUtils.isEmpty(type))
//                result.addAll((List<Object>) JSONUtils.convertJson(getFileAsIOStream("fhir-objects/" + type + "-requests.json"), List.class));
//            else
//                throw new ClientException("Type field is missing");
//            Map<String,Object> resp = new HashMap<>();
//            resp.put(type, result);
//            resp.put("count", result.size());
//            return new ResponseEntity<>(resp, HttpStatus.OK);
//        } catch (Exception e) {
//            return exceptionHandler(new Response(), e);
//        }
//    }
//
//    @PostMapping(value = "/payer/coverageeligibility/approve")
//    public ResponseEntity<Object> coverageEligibilityApprove(@RequestBody Map<String, Object> requestBody) {
//        return approve();
//    }
//
//    @PostMapping(value = "/payer/coverageeligibility/reject")
//    public ResponseEntity<Object> coverageEligibilityReject(@RequestBody Map<String, Object> requestBody) {
//        return reject();
//    }
//
//    @PostMapping(value = "/payer/preauth/approve")
//    public ResponseEntity<Object> preauthApprove(@RequestBody Map<String, Object> requestBody) {
//        return approve();
//    }
//
//    @PostMapping(value = "/payer/preauth/reject")
//    public ResponseEntity<Object> preauthReject(@RequestBody Map<String, Object> requestBody) {
//        return reject();
//    }
//
//    @PostMapping(value = "/payer/claim/approve")
//    public ResponseEntity<Object> claimApprove(@RequestBody Map<String, Object> requestBody) {
//        return approve();
//    }
//
//    @PostMapping(value = "/payer/claim/reject")
//    public ResponseEntity<Object> claimReject(@RequestBody Map<String, Object> requestBody) {
//        return reject();
//    }
//
//    public ResponseEntity<Object> approve(){
//        try {
//            Map<String,Object> resp = new HashMap<>();
//            resp.put("timestamp", System.currentTimeMillis());
//            resp.put("status", "successful");
//            resp.put("reason", "");
//            return new ResponseEntity<>(resp, HttpStatus.OK);
//        } catch (Exception e) {
//            return exceptionHandler(new Response(), e);
//        }
//    }
//
//    public ResponseEntity<Object> reject(){
//        try {
//            Map<String,Object> resp = new HashMap<>();
//            resp.put("timestamp", System.currentTimeMillis());
//            resp.put("status", "successful");
//            return new ResponseEntity<>(resp, HttpStatus.OK);
//        } catch (Exception e) {
//            return exceptionHandler(new Response(), e);
//        }
//    }
//
//}

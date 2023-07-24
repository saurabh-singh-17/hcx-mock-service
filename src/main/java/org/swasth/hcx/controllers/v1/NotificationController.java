package org.swasth.hcx.controllers.v1;

import com.google.j2objc.annotations.J2ObjCIncompatible;
import io.hcxprotocol.init.HCXIntegrator;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.service.HcxIntegratorService;
import org.swasth.hcx.service.NotificationService;
import org.swasth.hcx.service.RedisService;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;

import javax.websocket.server.PathParam;

import static org.swasth.hcx.utils.Constants.GET_NOTIFICATION;

@RestController()
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisService redisService;

    @Value("${redis.expires}")
    private int redisExpires;

    @PostMapping(value = "/" + "${hcx_application.api_version}" + Constants.NOTIFICATION_SUBSCRIBE)
    public ResponseEntity<Object> notificationSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody, Constants.NOTIFICATION_SUBSCRIBE);
        Response response = new Response();
        try {
            notificationService.processSubscription(request, response);
            System.out.println("Subscribe response sent");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            System.out.println("Subscribe failed response sent");
            return exceptionHandler(response, e);
        }
    }


    @PostMapping(value = "/" + "${hcx_application.api_version}" + Constants.NOTIFICATION_NOTIFY)
    public ResponseEntity<Object> notificationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        Response response = new Response(System.currentTimeMillis());
        Request request = new Request(requestBody);
        try {
            Map<String, Object> output = new HashMap<>();
            String senderCode = (String) request.getNotificationHeaders().get("sender_code");
            String topicCode = request.getTopicCode();
            HCXIntegrator hcxIntegrator = hcxIntegratorService.getHCXIntegrator(senderCode);
            hcxIntegrator.receiveNotification(JSONUtils.serialize(requestBody), output);
            String key = senderCode + "-" + topicCode;
            redisService.set(key, notificationService.notificationResponse(output), redisExpires);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @GetMapping(value = "/" + "${hcx_application.api_version}" + GET_NOTIFICATION)
    public ResponseEntity<Object> getNotification(@PathVariable("participantCode") String code) throws Exception {
        try {
            List<Map<String,Object>> details = redisService.get(code);
            Map<String, Object> output = new HashMap<>();
            output.put("notification", details);
            return new ResponseEntity<>(output, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}

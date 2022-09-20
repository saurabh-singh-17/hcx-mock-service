package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.service.NotificationService;
import org.swasth.hcx.utils.Constants;

public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping(value = "/" + "${hcx_application.api_version}" + Constants.NOTIFICATION_SUBSCRIBE)
    public ResponseEntity<Object> notificationSubscribe(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody);
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


    @PostMapping("/" + "${hcx_application.api_version}" + Constants.NOTIFICATION_NOTIFY)
    public ResponseEntity<Object> notificationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        Request request = new Request(requestBody);
        Response response = new Response();
        try {
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }
}

package org.swasth.hcx.controllers.v1;

import io.hcxprotocol.init.HCXIntegrator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.service.NotificationService;
import org.swasth.hcx.service.RedisService;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;

@RequestMapping(value = "/"+ "${hcx_application.api_version}")
@RestController()
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisService redisService;
    @Value("${redis.expires}")
    private int redisExpires;

    @Value("${kafka.topic.coverageeligibility}")
    private String kafkaTopic;

    @RequestMapping(value = Constants.NOTIFICATION_NOTIFY, method = RequestMethod.POST)
    public ResponseEntity<Object> claimSubmit(@RequestBody Map<String, Object> requestBody) throws Exception {
        System.out.println("----Notification notify API -----");
        System.out.println("Request Body ------" + requestBody);
        return processRequest(requestBody, Constants.NOTIFICATION_NOTIFY, Constants.NOTIFICATION_NOTIFY, kafkaTopic);
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
            String key = senderCode + ":" + topicCode;
            redisService.set(key, notificationService.notificationResponse(output), redisExpires);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }

    @PostMapping(value = "/notification/list")
    public ResponseEntity<Object> getNotification(@RequestBody Map<String, Object> requestBody) throws Exception {
        try {
            System.out.println("Getting the values for participant code :" + requestBody);
            List<Map<String, Object>> detailsParticipantRole = new ArrayList<>();
            System.out.println(requestBody.containsKey("participant_role"));
            if (requestBody.containsKey("participant_role") && !StringUtils.isEmpty("participant_role")) {
                System.out.println("Participant role ---" + requestBody.get("participant_role"));
                detailsParticipantRole = redisService.get((String) requestBody.get("participant_role"));
                System.out.println("-- Details participant Roles---- " + detailsParticipantRole);
            }
//            } else if (requestBody.containsKey("participant_code")) {
//                detailsParticipantCode = redisService.get((String) requestBody.get("participant_code"));
//            }
//            List<Map<String, Object>> combinedDetails = new ArrayList<>(detailsParticipantRole);
//            combinedDetails.addAll(detailsParticipantCode);
            System.out.println("detailsParticipantRole ---" + detailsParticipantRole);
            Map<String, Object> output = new HashMap<>();
            output.put("result", detailsParticipantRole);

            return new ResponseEntity<>(output, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}

package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;
import org.swasth.hcx.utils.OnActionCall;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private OnActionCall onActionCall;

    @Autowired
    Environment env;

    @Async(value = "asyncExecutor")
    public void processSubscription(Request request, Response response) throws Exception {
        String subscription_id = request.getSubscriptionId();
        //making body for on subscribe call
        HashMap<String, String> reqbody = new HashMap<>();
        reqbody.put("subscription_id",subscription_id);
        reqbody.put("subscription_status","Active");
        //onActionCall.sendOnAction("/" + env.getProperty("hcx_application.api_version")  + Constants.NOTIFICATION_ON_SUBSCRIBE,reqbody);
    }

    /**
     * validates and process the notify request
     */

  public void notify(Request request , String entity, String status) throws Exception {
        HashMap<String, Object> notification_header = new HashMap<>();
        //getting the sendor code from the registry
        String osid = onActionCall.searchRegistry(env.getProperty("mock_payer.username"));
        notification_header.put("sender_code",osid);
        notification_header.put("timestamp", String.valueOf(System.currentTimeMillis()));
        ArrayList<String> recipient_type = new ArrayList<>();
        recipient_type.add("participant_role");
        notification_header.put("recipient_type", recipient_type);
        notification_header.put("recipients","payor");
        HashMap<String,Object> resBody =  new HashMap<>();
        resBody.put("alg","RS256");
        resBody.put("x-hcx-notification_headers",notification_header);
        HashMap<String, String> payload = new HashMap<>();
        payload.put("topic_code","notif-workflow-update");
        String template = "%s has updated a %s request with correlation id: %s status to %s.";
        payload.put("message", String.format(template, osid, entity, request.getCorrelationId(),status));
        resBody.put("paylaod",payload);
        System.out.println("Notification notify body "+resBody);
        //JWS encryption
        String privateKeyPath =  "key/x509-private-key.pem";
        Map<String,String> result = onActionCall.jwsEncryptPayload(privateKeyPath,resBody);
        //onActionCall.sendOnAction("/" + env.getProperty("hcx_application.api_version") + Constants.NOTIFICATION_NOTIFY,result);
    }

    public String notificationResponse(Map<String, Object> notification) throws JsonProcessingException {
        Map<String, Object> notificationHeaders = (Map<String, Object>) ((Map<String, Object>) notification.get(Constants.HEADERS)).get("x-hcx-notification_headers");
        Map<String, Object> payload = (Map<String, Object>) notification.get(Constants.PAYLOAD);
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("timestamp", notificationHeaders.get("timestamp"));
        notificationMap.put("sender_code", notificationHeaders.get("sender_code"));
        notificationMap.put("topic_code", payload.get("topic_code"));
        notificationMap.put(Constants.MESSAGE, payload.get(Constants.MESSAGE));
        return JSONUtils.serialize(notificationMap);
    }


}

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

package org.swasth.hcx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.OnActionCall;

import java.util.HashMap;

public class NotificationService {

    @Autowired
    private OnActionCall onActionCall;

    @Async(value = "asyncExecutor")
    public void processSubscription(Request request, Response response) throws Exception {
        String subscription_id = request.getSubscriptionId();
        //making body for on subscribe call
        HashMap<String, String> reqbody = new HashMap<>();
        reqbody.put("subscription_id",subscription_id);
        reqbody.put("subscription_status","Active");
        onActionCall.sendOnAction(Constants.NOTIFICATION_ON_SUBSCRIBE,reqbody);
    }
}

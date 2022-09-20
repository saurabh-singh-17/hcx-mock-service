package org.swasth.hcx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.OnActionCall;

import java.util.HashMap;

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
        onActionCall.sendOnAction(Constants.NOTIFICATION_ON_SUBSCRIBE,reqbody);
    }

    /**
     * validates and process the notify request
     */
    public void notify(Request request) throws Exception {
        HashMap<String, String> resHeader = new HashMap<>();
        //getting the sendor code from the registry
        String osid = onActionCall.searchRegistry(env.getProperty("hcx_application.user"));
        resHeader.put(Constants.SENDER_CODE,osid);
        HashMap<String,?> resBody =  new HashMap<>();
    }

}

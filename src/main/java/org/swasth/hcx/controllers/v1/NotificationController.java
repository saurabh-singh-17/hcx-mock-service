package org.swasth.hcx.controllers.v1;

import io.hcxprotocol.init.HCXIntegrator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.swasth.hcx.dto.Notification;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.service.NotificationService;
import org.swasth.hcx.service.PostgresService;
import org.swasth.hcx.service.RedisService;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;


@RestController()
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;
    @Value("${postgres.table.notificationData}")
    private String table;
    @Autowired
    private PostgresService postgres;
    @Autowired
    private RedisService redisService;
    @Value("${redis.expires}")
    private int redisExpires;
    @Value("${request_list.default_limit}")
    private int listLimit;

    @Value("${request_list.default_latest_data_by_day}")
    private int dayLimit;

    @Value("${kafka.topic.coverageeligibility}")
    private String kafkaTopic;

    private void addToQuery(StringBuilder query, String code, String field){
        if(!StringUtils.isEmpty(code)) {
            query.append(" AND " + field + " = '" + code + "'");
        }
    }
    @PostMapping(value = "/"+ "${hcx_application.api_version}" + Constants.NOTIFICATION_NOTIFY)
    public ResponseEntity<Object> notificationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        System.out.println("----Notification notify API -----");
        System.out.println("Request Body ------" + requestBody);
        Response response = new Response(System.currentTimeMillis());
        Request request = new Request(requestBody);
        System.out.println("Request Body ------" + request);
        try {
            Map<String, Object> output = new HashMap<>();
            String senderCode = (String) request.getNotificationHeaders().get("sender_code");
            String topicCode = request.getTopicCode();
            String topicMessage = request.getTopicMessage();
            ArrayList<String> recipientCode = (ArrayList<String>) request.getNotificationHeaders().get("recipients");
            HCXIntegrator hcxIntegrator = hcxIntegratorService.getHCXIntegrator(recipientCode.get(0));
            hcxIntegrator.getParticipantCode();
            hcxIntegrator.receiveNotification(JSONUtils.serialize(requestBody), output);
            System.out.println("Notification details " + topicCode +  " " +topicMessage);
            System.out.println("Notification is here" + output);
            String request_id = UUID.randomUUID().toString();
            notificationService.sendNotification(request_id,senderCode,hcxIntegrator.getParticipantCode(),topicMessage,topicCode,"false", String.valueOf(System.currentTimeMillis()));
            String query = String.format("INSERT INTO %s (request_id,sender_code,recipient_code,topic_code,message,read,created_on) VALUES ('%s','%s','%s','%s','%s','%s',%d);",
                    table, request_id, senderCode, hcxIntegrator.getParticipantCode(), topicCode, topicMessage, "false", System.currentTimeMillis());
            postgres.execute(query);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(response, e);
        }
    }


    @PostMapping(value = "/payer/notification/request/list")
    public ResponseEntity<Object> requestList(@RequestBody Map<String, Object> requestBody) {
        try {
            String type = (String) requestBody.getOrDefault("type", "");
            long currentDay = System.currentTimeMillis();
            long tenDaysInMillis = TimeUnit.DAYS.toMillis((int) requestBody.getOrDefault("days", listLimit));
            long days = currentDay-tenDaysInMillis;
            int limit = (int) requestBody.getOrDefault("limit", listLimit);
            int offset = (int) requestBody.getOrDefault("offset", 0);
            String senderCode = (String) requestBody.getOrDefault("sender_code", "");
            String recipientCode = (String) requestBody.getOrDefault("recipient_code", "");
            List<Object> result = new ArrayList<>();
            StringBuilder countQuery = new StringBuilder("SELECT count(*) FROM " + table + " WHERE created_on > " + days);
            addToQuery(countQuery, recipientCode, "recipient_code");
            ResultSet resultSet1 = postgres.executeQuery(countQuery.toString());
            Map<String, Object> resp = new HashMap<>();
            while (resultSet1.next()) {
                Map<String, Object> map = new HashMap<>();
                resp.put("count", resultSet1.getInt("count"));
            }
            String query = countQuery.toString().replace("count(*)", "count(*) over(),*") + " ORDER BY created_on DESC LIMIT " + limit + " OFFSET " + offset;
            System.out.println("Notification list query " + query);
            ResultSet resultSet = postgres.executeQuery(query);
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("sender_code", resultSet.getString("sender_code"));
                map.put("recipient_code", resultSet.getString("recipient_code"));
                map.put("request_id", resultSet.getString("request_id"));
                map.put("topic_code", resultSet.getString("topic_code"));
                map.put("message", resultSet.getString("message"));
                map.put("read", resultSet.getString("read"));
                map.put("created_on", resultSet.getString("created_on"));
                result.add(map);
            }
            resp.put("notification", result);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

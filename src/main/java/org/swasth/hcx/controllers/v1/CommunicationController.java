package org.swasth.hcx.controllers.v1;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.service.PostgresService;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController()
public class CommunicationController extends BaseController {

    @Value("${postgres.table.communicationData}")
    private String table;
    @Autowired
    private PostgresService postgres;

    @Value("${request_list.default_limit}")
    private int listLimit;

    @Value("${request_list.default_latest_data_by_day}")
    private int dayLimit;

    @Value("${kafka.topic.communication}")
    private String kafkaTopic;

    private void addToQuery(StringBuilder query, String code, String field){
        if(!StringUtils.isEmpty(code)) {
            query.append(" AND " + field + " = '" + code + "'");
        }
    }

    @RequestMapping(value = "/"+ "${hcx_application.api_version}"+ "/communication/request", method = RequestMethod.POST)
    public ResponseEntity<Object> communicationRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processRequest(requestBody, Constants.COMMUNICATION_REQUEST, Constants.COMMUNICATION_ONREQUEST, kafkaTopic);
    }

    @RequestMapping(value = "/"+ "${hcx_application.api_version}"+ "/communication/on_request", method = RequestMethod.POST)
    public ResponseEntity<Object> communicationOnRequest(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processAndValidateRequest(requestBody, Constants.COMMUNICATION_REQUEST,Constants.COMMUNICATION_ONREQUEST, kafkaTopic);
    }

    @PostMapping(value = "/payer/communication/request/list")
    public ResponseEntity<Object> requestList(@RequestBody Map<String, Object> requestBody) {
        try {
            String correlationId = (String) requestBody.getOrDefault("correlation_id", "");
            List<Object> result = new ArrayList<>();
            StringBuilder countQuery = new StringBuilder("SELECT count(*) FROM " + table + " WHERE correlation_id = " + "'" + correlationId + "'");
            ResultSet resultSet1 = postgres.executeQuery(countQuery.toString());
            Map<String, Object> resp = new HashMap<>();
            while (resultSet1.next()) {
                Map<String, Object> map = new HashMap<>();
                resp.put("count", resultSet1.getInt("count"));
            }
            String query = countQuery.toString().replace("count(*)", "count(*) over(),*") + " ORDER BY created_on DESC";
            System.out.println("Notification list query " + query);
            ResultSet resultSet = postgres.executeQuery(query);
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("sender_code", resultSet.getString("sender_code"));
                map.put("recipient_code", resultSet.getString("recipient_code"));
                map.put("request_id", resultSet.getString("request_id"));
                map.put("correlation_id", resultSet.getString("correlation_id"));
                map.put("message", resultSet.getString("message"));
                map.put("created_on", resultSet.getString("created_on"));
                result.add(map);
            }
            resp.put("communication", result);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

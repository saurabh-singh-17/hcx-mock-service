package org.swasth.hcx.controllers.v1;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.service.PostgresService;
import org.swasth.hcx.utils.JSONUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class PatientController extends BaseController {

    @Value("${postgres.table.patientData}")
    private String table;

    @Autowired
    private PostgresService postgres;

    @Value("${request_list.default_limit}")
    private int listLimit;

    @Value("${request_list.default_latest_data_by_day}")
    private int dayLimit;

    IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

    private void addToQuery(StringBuilder query, String code, String field){
        if(!StringUtils.isEmpty(code)) {
            query.append(" AND " + field + " = '" + code + "'");
        }
    }

    @PostMapping(value = "/payer/beneficiary/request/list")
    @CrossOrigin(origins = "http://127.0.0.1:5173")
    public ResponseEntity<Object> requestList(@RequestBody Map<String, Object> requestBody) {
        try {
            long days = System.currentTimeMillis()-(int) requestBody.getOrDefault("days", dayLimit)*24*60*60*1000;
            int limit = (int) requestBody.getOrDefault("limit", listLimit);
            int offset = (int) requestBody.getOrDefault("offset", 0);
            String senderCode = (String) requestBody.getOrDefault("sender_code", "");
            String recipientCode = (String) requestBody.getOrDefault("recipient_code", "");
            String patientName = (String) requestBody.getOrDefault("patient_name", "");
            String phoneNumber = (String) requestBody.getOrDefault("phone_number", "");
            String insuranceId = (String) requestBody.getOrDefault("insurance_id", "");
            List<Object> result = new ArrayList<>();
            StringBuilder countQuery;
            if(patientName != ""){
                countQuery = new StringBuilder("SELECT count(*) FROM " + table + " WHERE patient_name ILIKE '%" + patientName  + "%' ");
            } else if (phoneNumber != "") {
                countQuery = new StringBuilder("SELECT count(*) FROM " + table + " WHERE phone_number = '" + phoneNumber+ "' ");
            } else if (insuranceId != "") {
                countQuery = new StringBuilder("SELECT count(*) FROM " + table + " WHERE insurance_id ILIKE '%" + insuranceId+ "%' ");
            }else{
                throw new ClientException("Atlease one of patient name, phone number or insurance id should be present");
            }
            System.out.println("query" +  countQuery);
            addToQuery(countQuery, senderCode, "sender_code");
            addToQuery(countQuery, recipientCode, "recipient_code");
            ResultSet resultSet1 = postgres.executeQuery(countQuery.toString());
            Map<String, Object> resp = new HashMap<>();
            while (resultSet1.next()) {
                resp.put("count", resultSet1.getInt("count"));
            }
            String query = countQuery.toString().replace("count(*)", "count(*) over(),*") + " ORDER BY created_on DESC LIMIT " + limit + " OFFSET " + offset;
            ResultSet resultSet = postgres.executeQuery(query);
            while (resultSet.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("sender_code", resultSet.getString("sender_code"));
                map.put("recipient_code", resultSet.getString("recipient_code"));
                map.put("request_id", resultSet.getString("request_id"));
                map.put("patient_name", resultSet.getString("patient_name"));
                map.put("phone_number", resultSet.getString("phone_number"));
                map.put("insurance_id", resultSet.getString("insurance_id"));
                map.put("action", resultSet.getString("action"));
                map.put("created_on", resultSet.getString("created_on"));
                result.add(map);
            }
            resp.put("payload", result);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }
}

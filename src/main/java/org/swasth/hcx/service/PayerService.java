package org.swasth.hcx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.hcx.dto.Request;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.utils.JSONUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.swasth.hcx.utils.Constants.PENDING;

@Service
public class PayerService {

    @Value("${postgres.payerTable}")
    private String table;

    @Autowired
    private PostgresService postgres;


    public void process(Request request,Map<String,Object> reqFhirObj, Map<String,Object> respFhirObj) throws ClientException, JsonProcessingException {
        Map<String,Object> info = new HashMap<>();
        if(!request.getAction().contains("coverageeligibility")) {
            info.put("medical", Collections.singletonMap("status", PENDING));
            info.put("financial", Collections.singletonMap("status", PENDING));
        }
        String query = String.format("INSERT INTO %s (request_id,action,raw_payload,request_fhir,response_fhir,status,additional_info,created_on,updated_on) VALUES ('%s','%s','%s','%s','%s','%s','%s',%d,%d);",
                table, reqFhirObj.get("id"), request.getAction(), request.getPayload(), reqFhirObj, respFhirObj, PENDING, JSONUtils.serialize(info), System.currentTimeMillis(), System.currentTimeMillis());
        postgres.execute(query);
    }
}

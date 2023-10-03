package org.swasth.hcx.controllers.v1;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.utils.Constants;

import java.util.Map;
import java.util.UUID;

@RestController
public class ProviderAppController {

    @Value("${postgres.table.consultation-info}")
    private String consultationInfoTable;


    @PostMapping("/consultation/add")
    public void addConsultationInfo(@RequestBody Map<String,Object> requestBody) throws ClientException {
        UUID workflowId = (UUID) requestBody.getOrDefault("workflow_id","");
        if(!requestBody.containsKey("workflow_id") && workflowId.toString().isEmpty()) {
            throw new ClientException("Work flow id cannot be empty");
        }
    }

}

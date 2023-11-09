package org.swasth.hcx.controllers.v1;

import io.hcxprotocol.exception.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.service.PostgresService;
import org.swasth.hcx.utils.JSONUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
public class DocumentController extends BaseController {

    @Autowired
    private PostgresService postgresService;

    @Value("${postgres.table.document-analysis-response}")
    private String documentAnalyseResponse;

    @PostMapping("/document/analyse/on_submit")
    public ResponseEntity<Object> analyseOnSubmit(@RequestBody List<Map<String, Object>> request) {
        try {
            if (request.isEmpty()) {
                throw new ClientException("Request body is empty");
            }
            String requestId = request.get(0).getOrDefault("request_id", "").toString();
            System.out.println("Request will be ---------" + request);
            System.out.println(JSONUtils.serialize(request));
            String query = String.format("INSERT INTO %s (request_id, payload) VALUES ('%s', '%s');",
                    documentAnalyseResponse, requestId, JSONUtils.serialize(request));
                System.out.println("---query -----" + query);
                postgresService.execute(query);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            response.put("request_id", requestId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @GetMapping("/document/analysis/{request_id}")
    public ResponseEntity<Object> getDocumentAnalysisResponse(@PathVariable("request_id") String requestId) {
        try {
            List<Map<String, Object>> documentAnalysisResponse = getInfoByRequestId(requestId);
            if (documentAnalysisResponse != null) {
                return ResponseEntity.ok(documentAnalysisResponse);
            } else {
                return ResponseEntity.status(kong.unirest.HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Document Analysis Response not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(kong.unirest.HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Unable to fetch the details" + e.getMessage()));
        }
    }

    public ArrayList<Map<String, Object>> getInfoByRequestId(String requestId) throws org.swasth.hcx.exception.ClientException, SQLException {
        String searchQuery = String.format("SELECT * FROM %s WHERE request_id = '%s'", documentAnalyseResponse, requestId);
        ResultSet resultSet = postgresService.executeQuery(searchQuery);
        ArrayList<Map<String, Object>> documentAnalyseResponse;
        if (resultSet.next()) {
            documentAnalyseResponse = resultSet.getObject("payload", ArrayList.class);
        } else {
            throw new org.swasth.hcx.exception.ClientException("The Record does not exit for request id  : " + requestId);
        }
        return documentAnalyseResponse;
    }
}

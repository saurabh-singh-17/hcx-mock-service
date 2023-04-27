package org.swasth.hcx.controllers.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.utils.Constants;
import org.swasth.hcx.controllers.BaseController;

import java.util.Map;

@RestController()
@RequestMapping(value = "/"+ "${hcx_application.api_version}" + "/predetermination")
public class PredeterminationController extends BaseController {

    @Value("${kafka.topic.predetermination}")
    private String kafkaTopic;

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public ResponseEntity<Object> submitPredetermination(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processRequest(requestBody, Constants.PREDETERMINATION_SUBMIT,Constants.PREDETERMINATION_ONSUBMIT, kafkaTopic);
    }

    @RequestMapping(value = "/on_submit", method = RequestMethod.POST)
    public ResponseEntity<Object> onSubmitPredetermination(@RequestBody Map<String, Object> requestBody) throws Exception {
        return processRequest(requestBody, Constants.PREDETERMINATION_ONSUBMIT,Constants.PREDETERMINATION_ONSUBMIT, kafkaTopic);
    }
}

package org.swasth.hcx.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.utils.Constants;

@RestController
public class HealthController extends BaseController {

    @RequestMapping(value = "/service/health", method = RequestMethod.GET)
    public ResponseEntity<Object> serviceHealth() {
        Response response = new Response(Constants.HEALTHY, true);
        System.out.println("-----------------------------------");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

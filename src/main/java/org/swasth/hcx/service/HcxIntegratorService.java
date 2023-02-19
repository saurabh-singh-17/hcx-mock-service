package org.swasth.hcx.service;

import io.hcxprotocol.init.HCXIntegrator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Service
public class HcxIntegratorService {

    @Autowired
    Environment env;

    public HCXIntegrator initialiseHcxIntegrator() throws Exception {
        /**
         * Initializing hcx_sdk to use helper functions and FHIR validator
         * Documentation is available at https://github.com/Swasth-Digital-Health-Foundation/hcx-platform/releases/tag/hcx-integrator-sdk-1.0.0
         */

        System.out.println("we are intiliazing the integrator SDK" + env.getProperty("hcx_application.user"));
        String keyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-29/demo-app/server/resources/keys/x509-private-key.pem";
        String certificate = IOUtils.toString(new URL(keyUrl), StandardCharsets.UTF_8.toString());

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", "http://staging-hcx.swasth.app/api/v0.7");
        configMap.put("participantCode", "1-521eaec7-8cb9-4b6c-8b4e-4dba300af6f4");
        configMap.put("authBasePath", "http://a9dd63de91ee94d59847a1225da8b111-273954130.ap-south-1.elb.amazonaws.com:8080/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token");
        configMap.put("username", env.getProperty("hcx_application.user"));
        configMap.put("password", env.getProperty("hcx_application.password"));
        configMap.put("encryptionPrivateKey", certificate);
        configMap.put("igUrl", "https://ig.hcxprotocol.io/v0.7.1");
        HCXIntegrator.init(configMap);
        return HCXIntegrator.getInstance();
    }
}

package org.swasth.hcx.service;

import io.hcxprotocol.init.HCXIntegrator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Service
public class HcxIntegratorService {

    @Autowired
    Environment env;

    private HCXIntegrator integrator;


    public HCXIntegrator initialiseHcxIntegrator() throws Exception {
        /**
         * Initializing hcx_sdk to use helper functions and FHIR validator
         * Documentation is available at https://github.com/Swasth-Digital-Health-Foundation/hcx-platform/releases/tag/hcx-integrator-sdk-1.0.0
         */

        if (integrator == null) {
            System.out.println("We are intiliazing the integrator SDK: " + env.getProperty("hcx_application.user"));
            String keyUrl = "https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-platform/sprint-29/demo-app/server/resources/keys/x509-private-key.pem";
            String certificate = IOUtils.toString(new URL(keyUrl), StandardCharsets.UTF_8.toString());

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("protocolBasePath", "https://staging-hcx.swasth.app/api/v0.7");
            configMap.put("participantCode", "1-29482df3-e875-45ef-a4e9-592b6f565782");
            configMap.put("authBasePath", "https://staging-hcx.swasth.app/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token");
            configMap.put("username", env.getProperty("hcx_application.user"));
            configMap.put("password", env.getProperty("hcx_application.password"));
            configMap.put("encryptionPrivateKey", certificate);
            configMap.put("igUrl", "https://ig.hcxprotocol.io/v0.7.1");
            integrator = HCXIntegrator.getInstance(configMap);
        } else {
            System.out.println("The Integrator SDK initialized already: " + env.getProperty("hcx_application.user"));
        }

        return integrator;
    }
}

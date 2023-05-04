package org.swasth.hcx.service;

import io.hcxprotocol.init.HCXIntegrator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.swasth.hcx.exception.ClientException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@Service
public class HcxIntegratorService {

    @Autowired
    Environment env;

    @Autowired
    private PostgresService postgres;

    private Map<String,Object> configCache = new HashMap<>();


    public HCXIntegrator getHCXIntegrator(String participantCode) throws Exception {
        /**
         * Initializing hcx_sdk to use helper functions and FHIR validator
         * Documentation is available at https://github.com/Swasth-Digital-Health-Foundation/hcx-platform/releases/tag/hcx-integrator-sdk-1.0.0
         */
        HCXIntegrator hcxIntegrator = null;
        if(!configCache.containsKey(participantCode)) {
            hcxIntegrator = HCXIntegrator.getInstance(getParticipantConfig(participantCode));
            configCache.put(hcxIntegrator.getParticipantCode(), hcxIntegrator);
        } else {
            hcxIntegrator = (HCXIntegrator) configCache.get(participantCode);
        }
        System.out.println("Config map cache: " + configCache);
        System.out.println("We are intiliazing the integrator SDK: " + hcxIntegrator.getParticipantCode() + "private key " + hcxIntegrator.getPrivateKey());
        return hcxIntegrator;
    }

    public Map<String,Object> getParticipantConfig(String participantCode) throws ClientException, SQLException, IOException {
        String query = String.format("SELECT * FROM %s WHERE child_participant_code='%s'", env.getProperty("postgres.table.mockParticipant"), participantCode);
        System.out.println("config fetch query" + query);
        ResultSet resultSet = postgres.executeQuery(query);
        if(resultSet.next()){
            System.out.println("inside result set " + resultSet.getString("private_key"));
            return getConfig(participantCode, resultSet.getString("primary_email"), resultSet.getString("password"),  resultSet.getString("private_key"));
        } else {
            return getConfig(env.getProperty("mock_payer.participant_code"), env.getProperty("mock_payer.username"), env.getProperty("mock_payer.password"),env.getProperty("mock_payer.private_key"));
        }
    }

    public Map<String,Object> getConfig(String code, String username, String password, String privateKey) throws IOException {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", env.getProperty("hcx_application.url") + "/api/" + env.getProperty("hcx_application.api_version"));
        configMap.put("participantCode", code);
        configMap.put("authBasePath", env.getProperty("hcx_application.token_url"));
        configMap.put("username", username);
        configMap.put("password", password);
        configMap.put("encryptionPrivateKey", "-----BEGIN PRIVATE KEY-----\n MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDIRf2KSLXOFi19 sqc6zRE2LlD3ceJivwhZyFUt5oDKiYEUYycznVLExkkRMPl9P0JvTbOS02xIedDy oZoFQMT1tMDh71Q64Ck+dOD4JDjaeyGuaHUgDITHNsgRrCI++49nTcB466dHnvuL QXDYDbuNSfMsf2977boriE+ub8ILGhHDfF3Myw7oNhGAE0+dvMyUJHLAms3Ag90d YiqXl15qj4h1Il5XP3R+QMC91+YeeL3osmHrGipYUbgY2LNaIuWTD57fwvOzqkrQ zhuM//dPCcdR6eM6YBVVLzjI/xrQNYcd3sPKrW6QdQGEYBzuji9d9BkrDTq13tNw UGBqtrvRAgMBAAECggEAA6D43H4k56zGkse3e0j1usYSnDTaE7zsrP+fPf1mcuJf EQWl00vykODQzaeV/1vzo2ZHNpX0vm3bywKsjT5F7K0OKTsxEF7z4Qck1VAxihwS LBmlz3s/QRK8DoMVwi9Zvcb2kbuqMzcKmCSgWPTWPl5ojONLUQW5omdrfgXVwHCr 4iWd9+b2qFT+68Xht86VhjWTpOGmKVGXqF6Poh1/tGSz/DHjMeYk61Fbz+y6eo/m CtKN6xX9c8wlx7I0hWOxSEIf810V7oqLhtBez5D2hzhcC8cKiqSfL62+ibHfM527 9ZojqFg5kp/zUkdgqglLwvnywiz6tll9CETkCTx6YQKBgQDkdqDIH1y8fnXFIGnB k3TQejIfKdCvTH/lCmxcYNPql6RQVfsvdqeaCqEvwJ0/+rLIzcHmajvIbcUuQ9cZ Ng7p+j+tAHXG3wncCllarEIGdk7lxKkROXSXphZbcRIACEi51E4jDi+6NmveOSCB lQuG0lN/3I65xV4t1lseJJJ4oQKBgQDgaYsrf7J6ZNThn+9eolh6MJ+2rkY1gm+v ROx41irb00xLJ8gIFkbJ2wCwlln4Pt1EbGeCMApZP7bMYctAO0UhTZXANbu7eV0k j1DoinMofGCDMh+umpPCNd6IxG7jyHJEayuLX+awSuLQ79NPrkLN2rkxKG2vd0Ln RYwK6MKFMQKBgQCuS0pSIosPSrLyVs4X3jUKL485cx8//W68//y3ZS+HE3VJzK67 ARzQSje6RIozG0F2LHM3I+jD+eqqZxscor8yBba6d1VjCRlug36zzCL65xs985DA EUM/xnfb/yEqYSeKjcumttX2jDoy6QZBlcGbc648J7nDRC2bbqNJFcGgwQKBgCJI fXZbv8xq8wq1AECY1KN2IjAqV5d9IcrI4MdlEI8crJ8eqLRzksTrrCNOFNiT4gG9 /eIprKsNzMNUfVSGuXzo+WOv0cpGNDCNpHKiFA2vUp84XRazjrcwu5pxR++Mg8ho BvL356ovBo+c089sc9A3Beu4nWkc8x+ZxFc2T+FBAoGBANjFrtd4IPZhN8FUU8VX GQ/2oXszIc1eXXD4iqCe6cN5GhCF/odO5llZMavF+kOwABLhjse8iiqhg6OLjni/ KXCxnuyjW8zK7dtyEVA8c602yIhDTFowNyNiSJz6niC6YuYWlooa0aHJa839/Mx1 32OkoRO2uG3Urje9xiltlM0e \n-----END PRIVATE KEY-----");

        return configMap;
    }
}

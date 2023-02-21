package org.swasth.hcx.controllers.support;

import io.hcxprotocol.jwe.JweRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swasth.hcx.controllers.BaseController;
import org.swasth.hcx.dto.Response;

import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EnDeController extends BaseController {

    @PostMapping(value = "/payer/request/encrypt")
    public ResponseEntity<Object> encrypt(@RequestBody Map<String, Object> request) throws Exception {
        try {
            Map<String,Object> payload = (Map<String, Object>) request.getOrDefault("payload", new HashMap<>());
            String publicKeyPath = (String) request.getOrDefault("publicKeyPath", "");
            validateMap("payload", payload);
            validateStr("publicKeyPath", publicKeyPath);
            JweRequest jweRequest = new JweRequest(new HashMap<>(), payload);
            jweRequest.encryptRequest(getPublicKey(publicKeyPath));
            return new ResponseEntity<>(Collections.singletonMap("encryptedPayload", jweRequest.getEncryptedObject()), HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    @PostMapping(value = "/payer/request/decrypt")
    public ResponseEntity<Object> decrypt(@RequestBody Map<String, Object> request) throws Exception {
        try {
            String payload = (String) request.getOrDefault("payload", "");
            String privateKeyPath = (String) request.getOrDefault("privateKeyPath", "");
            validateStr("payload", payload);
            validateStr("privateKeyPath", privateKeyPath);
            JweRequest jweRequest = new JweRequest(new HashMap<>() {{
                put("payload", payload);
            }});
            jweRequest.decryptRequest(getPrivateKey(privateKeyPath));
            return new ResponseEntity<>(Collections.singletonMap("decryptedPayload", jweRequest.getEncryptedObject()), HttpStatus.OK);
        } catch (Exception e) {
            return exceptionHandler(new Response(), e);
        }
    }

    private RSAPublicKey getPublicKey(String keyPath) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(new URL(keyPath).openStream());
        return (RSAPublicKey) x509Certificate.getPublicKey();
    }

    private RSAPrivateKey getPrivateKey(String keyPath) throws Exception {
        PemReader pemReader = new PemReader(new InputStreamReader(new URL(keyPath).openStream()));
        PemObject pemObject = pemReader.readPemObject();
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(privateKeySpec);
    }


}

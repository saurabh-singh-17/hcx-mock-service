package org.swasth.hcx.service;

import io.hcxprotocol.init.HCXIntegrator;
import io.hcxprotocol.utils.Operations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.dto.ResponseError;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.exception.ServerException;
import org.swasth.hcx.exception.ServiceUnavailbleException;

import java.util.HashMap;
import java.util.Map;

@Service
public class GenerateOutgoingRequest {

    @Value("${beneficiary.protocol-base-path}")
    private String protocolBasePath;
    @Value("${beneficiary.participant-code}")
    private String participantCode;
    @Value("${beneficiary.username}")
    private String userName;
    @Value("${beneficiary.password}")
    private String password;
    @Value("${beneficiary.recipient-code}")
    private String mockRecipientCode;

    public ResponseEntity<Object> processOutgoingRequest(Map<String, Object> requestBody, String apiAction, Operations operations) throws Exception {
        Response response = new Response();
        try {
            Map<String, Object> output = new HashMap<>();
            processAndValidateOutgoingRequest(operations, output);
            System.out.println("The outgoing request has been successfully generated.");
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error   " + e);
            return exceptionHandler(response, e);
        }
    }

    public void processAndValidateOutgoingRequest(Operations operations, Map<String, Object> output) throws Exception {
        String fhirPayload = "{ \"resourceType\": \"Bundle\", \"id\": \"802698e0-17c6-4b24-935f-0bc33cc951f6\", \"meta\": { \"lastUpdated\": \"2023-09-13T12:45:01.004+05:30\", \"profile\": [ \"https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequestBundle.html\" ] }, \"identifier\": { \"system\": \"https://www.tmh.in/bundle\", \"value\": \"52b73d6a-9e6d-4b60-a628-2bfdc2b8c975\" }, \"type\": \"collection\", \"timestamp\": \"2023-09-13T12:45:01.006+05:30\", \"entry\": [ { \"fullUrl\": \"CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6\", \"resource\": { \"resourceType\": \"CoverageEligibilityRequest\", \"id\": \"dc82673b-8c71-48c2-8a17-16dcb3b035f6\", \"meta\": { \"profile\": [ \"https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityRequest.html\" ] }, \"identifier\": [ { \"value\": \"req_70e02576-f5f5-424f-b115-b5f1029704d4\" } ], \"status\": \"active\", \"priority\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/processpriority\", \"code\": \"normal\" } ] }, \"purpose\": [ \"benefits\" ], \"patient\": { \"reference\": \"Patient/RVH1003\" }, \"servicedDate\": \"0026-12-13\", \"created\": \"2023-09-13T12:44:58+05:30\", \"enterer\": { \"reference\": \"Practitioner/PractitionerViswasKar\" }, \"provider\": { \"reference\": \"Organization/GICOFINDIA\" }, \"insurer\": { \"reference\": \"Organization/WeMeanWell01\" }, \"facility\": { \"reference\": \"http://sgh.com.sa/Location/4461281\" }, \"insurance\": [ { \"coverage\": { \"reference\": \"Coverage/COVERAGE1\" } } ], \"item\": [ { \"productOrService\": { \"coding\": [ { \"system\": \"https://irdai.gov.in/package-code\", \"code\": \"E101021\", \"display\": \"Twin Sharing Ac\" } ], \"text\": \" twin sharing basis room package\" }, \"diagnosis\": [ { \"diagnosisCodeableConcept\": { \"coding\": [ { \"system\": \"https://irdai.gov.in/package-code\", \"code\": \"E906184\", \"display\": \"SINGLE INCISION LAPAROSCOPIC APPENDECTOMY\" } ], \"text\": \"SINGLE INCISION LAPAROSCOPIC APPENDECTOMY\" } } ] } ] } }, { \"fullUrl\": \"Organization/WeMeanWell01\", \"resource\": { \"resourceType\": \"Organization\", \"id\": \"WeMeanWell01\", \"meta\": { \"profile\": [ \"https://nrces.in/ndhm/fhir/r4/StructureDefinition/Organization\" ] }, \"identifier\": [ { \"type\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\", \"code\": \"AC\", \"display\": \"Narayana\" } ] }, \"system\": \"http://abdm.gov.in/facilities\", \"value\": \"HFR-ID-FOR-TMH\" } ], \"name\": \"WeMeanWell Hospital\", \"address\": [ { \"text\": \" Bannerghatta Road, Bengaluru \", \"city\": \"Bengaluru\", \"country\": \"India\" } ] } }, { \"fullUrl\": \"Organization/GICOFINDIA\", \"resource\": { \"resourceType\": \"Organization\", \"id\": \"GICOFINDIA\", \"meta\": { \"profile\": [ \"https://nrces.in/ndhm/fhir/r4/StructureDefinition/Organization\" ] }, \"identifier\": [ { \"type\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\", \"code\": \"AC\", \"display\": \"GOVOFINDIA\" } ] }, \"system\": \"http://irdai.gov.in/insurers\", \"value\": \"GICOFINDIA\" } ], \"name\": \"GICOFINDIA\" } }, { \"fullUrl\": \"Patient/RVH1003\", \"resource\": { \"resourceType\": \"Patient\", \"id\": \"RVH1003\", \"meta\": { \"profile\": [ \"https://nrces.in/ndhm/fhir/r4/StructureDefinition/Patient\" ] }, \"identifier\": [ { \"type\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\", \"code\": \"SN\", \"display\": \"Subscriber Number\" } ] }, \"system\": \"http://gicofIndia.com/beneficiaries\", \"value\": \"BEN-101\" } ], \"name\": [ { \"text\": \"Prasidh Dixit\" } ], \"gender\": \"male\", \"birthDate\": \"1960-09-26\", \"address\": [ { \"text\": \"#39 Kalena Agrahara, Kamanahalli, Bengaluru - 560056\", \"city\": \"Bengaluru\", \"state\": \"Karnataka\", \"postalCode\": \"560056\", \"country\": \"India\" } ] } }, { \"fullUrl\": \"Coverage/COVERAGE1\", \"resource\": { \"resourceType\": \"Coverage\", \"id\": \"COVERAGE1\", \"meta\": { \"profile\": [ \"https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-Coverage.html\" ] }, \"identifier\": [ { \"system\": \"https://www.gicofIndia.in/policies\", \"value\": \"policy-RVH1003\" } ], \"status\": \"active\", \"subscriber\": { \"reference\": \"Patient/RVH1003\" }, \"subscriberId\": \"2XX8971\", \"beneficiary\": { \"reference\": \"Patient/RVH1003\" }, \"relationship\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/subscriber-relationship\", \"code\": \"self\" } ] }, \"payor\": [ { \"reference\": \"Organization/GICOFINDIA\" } ] } }, { \"fullUrl\": \"Practitioner/PractitionerViswasKar\", \"resource\": { \"resourceType\": \"Practitioner\", \"id\": \"PractitionerViswasKar\", \"meta\": { \"profile\": [ \"https://nrces.in/ndhm/fhir/r4/StructureDefinition/Practitioner\" ] }, \"identifier\": [ { \"type\": { \"coding\": [ { \"system\": \"http://terminology.hl7.org/CodeSystem/v2-0203\", \"code\": \"MD\", \"display\": \"Medical License number\" } ] }, \"system\": \"http://abdm.gov.in/facilities\", \"value\": \"DOC-123/456\" } ], \"name\": [ { \"text\": \"Dr Viswas kar\" } ] } } ] }";
        HCXIntegrator hcxIntegrator = HCXIntegrator.getInstance(initializingConfigMap());
        hcxIntegrator.processOutgoingRequest(fhirPayload, operations, mockRecipientCode, "", "", new HashMap<>(), output);
    }

    public Map<String, Object> initializingConfigMap() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("protocolBasePath", protocolBasePath);
        configMap.put("participantCode", participantCode);
        configMap.put("username", userName);
        configMap.put("password", password);
        configMap.put("encryptionPrivateKey", "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCG+XLPYiCxrZq71IX+w7uoDGxGI7qy7XaDbL3BJE33ju7rjdrP7wsAOWRvM8BIyWuRZZhl9xG+u7l/7OsZAzGoqI7p+32x+r9IJVzboLDajk6tp/NPg1csc7f2M5Bu6rkLEvrKLz3dgy3Q928rMsD3rSmzBLelfKTo+aDXvCOiw1dMWsZZdkEpCTJxH39Nb2K4S59kO/R2GtSU/QMLq65m34XcMZpDtatA1u1S8JdZNNeMCO+NuFKBzIfvXUCQ8jkf7h612+UP1AYhoyCMFpzUZ9b7liQF9TYpX1Myr/tT75WKuRlkFlcALUrtVskL8KA0w6sA0nX5fORVsuVehVeDAgMBAAECggEAX1n1y5/M7PhxqWO3zYTFGzC7hMlU6XZsFOhLHRjio5KsImgyPlbm9J+W3iA3JLR2c17MTKxAMvg3UbIzW5YwDLAXViC+aW90like8mEQzzVdS7ysXG2ytcqCGUHQNStI0hP0a8T39XbodQl31ZKjU9VW8grRGe12Kse+4ukcW6yRVES+CkyO5BQB+vs3voZavodRGsk/YSt00PtIrFPJgkDuyzzcybKJD9zeJk5W3OGVK1z0on+NXKekRti5FBx/uEkT3+knkz7ZlTDNcyexyeiv7zSL/L6tcszV0Fe0g9vJktqnenEyh4BgbqABPzQR++DaCgW5zsFiQuD0hMadoQKBgQC+rekgpBHsPnbjQ2Ptog9cFzGY6LRGXxVcY7hKBtAZOKAKus5RmMi7Uv7aYJgtX2jt6QJMuE90JLEgdO2vxYG5V7H6Tx+HqH7ftCGZq70A9jFBaba04QAp0r4TnD6v/LM+PGVT8FKtggp+o7gZqXYlSVFm6YzI37G08w43t2j2aQKBgQC1Nluxop8w6pmHxabaFXYomNckziBNMML5GjXW6b0xrzlnZo0p0lTuDtUy2xjaRWRYxb/1lu//LIrWqSGtzu+1mdmV2RbOd26PArKw0pYpXhKFu/W7r6n64/iCisoMJGWSRJVK9X3D4AjPaWOtE+jUTBLOk0lqPJP8K6yiCA6ZCwKBgDLtgDaXm7HdfSN1/Fqbzj5qc3TDsmKZQrtKZw5eg3Y5CYXUHwbsJ7DgmfD5m6uCsCPa+CJFl/MNWcGxeUpZFizKn16bg3BYMIrPMao5lGGNX9p4wbPN5J1HDD1wnc2jULxupSGmLm7pLKRmVeWEvWl4C6XQ+ykrlesef82hzwcBAoGBAKGY3v4y4jlSDCXaqadzWhJr8ffdZUrQwB46NGb5vADxnIRMHHh+G8TLL26RmcET/p93gW518oGg7BLvcpw3nOZaU4HgvQjT0qDvrAApW0V6oZPnAQUlarTU1Uk8kV9wma9tP6E/+K5TPCgSeJPg3FFtoZvcFq0JZoKLRACepL3vAoGAMAUHmNHvDI+v0eyQjQxlmeAscuW0KVAQQR3OdwEwTwdFhp9Il7/mslN1DLBddhj6WtVKLXu85RIGY8I2NhMXLFMgl+q+mvKMFmcTLSJb5bJHyMz/foenGA/3Yl50h9dJRFItApGuEJo/30cG+VmYo2rjtEifktX4mDfbgLsNwsI=\n-----END PRIVATE KEY-----");
        configMap.put("signingPrivateKey", "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCG+XLPYiCxrZq71IX+w7uoDGxGI7qy7XaDbL3BJE33ju7rjdrP7wsAOWRvM8BIyWuRZZhl9xG+u7l/7OsZAzGoqI7p+32x+r9IJVzboLDajk6tp/NPg1csc7f2M5Bu6rkLEvrKLz3dgy3Q928rMsD3rSmzBLelfKTo+aDXvCOiw1dMWsZZdkEpCTJxH39Nb2K4S59kO/R2GtSU/QMLq65m34XcMZpDtatA1u1S8JdZNNeMCO+NuFKBzIfvXUCQ8jkf7h612+UP1AYhoyCMFpzUZ9b7liQF9TYpX1Myr/tT75WKuRlkFlcALUrtVskL8KA0w6sA0nX5fORVsuVehVeDAgMBAAECggEAX1n1y5/M7PhxqWO3zYTFGzC7hMlU6XZsFOhLHRjio5KsImgyPlbm9J+W3iA3JLR2c17MTKxAMvg3UbIzW5YwDLAXViC+aW90like8mEQzzVdS7ysXG2ytcqCGUHQNStI0hP0a8T39XbodQl31ZKjU9VW8grRGe12Kse+4ukcW6yRVES+CkyO5BQB+vs3voZavodRGsk/YSt00PtIrFPJgkDuyzzcybKJD9zeJk5W3OGVK1z0on+NXKekRti5FBx/uEkT3+knkz7ZlTDNcyexyeiv7zSL/L6tcszV0Fe0g9vJktqnenEyh4BgbqABPzQR++DaCgW5zsFiQuD0hMadoQKBgQC+rekgpBHsPnbjQ2Ptog9cFzGY6LRGXxVcY7hKBtAZOKAKus5RmMi7Uv7aYJgtX2jt6QJMuE90JLEgdO2vxYG5V7H6Tx+HqH7ftCGZq70A9jFBaba04QAp0r4TnD6v/LM+PGVT8FKtggp+o7gZqXYlSVFm6YzI37G08w43t2j2aQKBgQC1Nluxop8w6pmHxabaFXYomNckziBNMML5GjXW6b0xrzlnZo0p0lTuDtUy2xjaRWRYxb/1lu//LIrWqSGtzu+1mdmV2RbOd26PArKw0pYpXhKFu/W7r6n64/iCisoMJGWSRJVK9X3D4AjPaWOtE+jUTBLOk0lqPJP8K6yiCA6ZCwKBgDLtgDaXm7HdfSN1/Fqbzj5qc3TDsmKZQrtKZw5eg3Y5CYXUHwbsJ7DgmfD5m6uCsCPa+CJFl/MNWcGxeUpZFizKn16bg3BYMIrPMao5lGGNX9p4wbPN5J1HDD1wnc2jULxupSGmLm7pLKRmVeWEvWl4C6XQ+ykrlesef82hzwcBAoGBAKGY3v4y4jlSDCXaqadzWhJr8ffdZUrQwB46NGb5vADxnIRMHHh+G8TLL26RmcET/p93gW518oGg7BLvcpw3nOZaU4HgvQjT0qDvrAApW0V6oZPnAQUlarTU1Uk8kV9wma9tP6E/+K5TPCgSeJPg3FFtoZvcFq0JZoKLRACepL3vAoGAMAUHmNHvDI+v0eyQjQxlmeAscuW0KVAQQR3OdwEwTwdFhp9Il7/mslN1DLBddhj6WtVKLXu85RIGY8I2NhMXLFMgl+q+mvKMFmcTLSJb5bJHyMz/foenGA/3Yl50h9dJRFItApGuEJo/30cG+VmYo2rjtEifktX4mDfbgLsNwsI=\n-----END PRIVATE KEY-----");
        return configMap;
    }

    protected ResponseEntity<Object> exceptionHandler(Response response, Exception e) {
        e.printStackTrace();
        if (e instanceof ClientException) {
            return new ResponseEntity<>(errorResponse(response, ((ClientException) e).getErrCode(), e), HttpStatus.BAD_REQUEST);
        } else if (e instanceof ServiceUnavailbleException) {
            return new ResponseEntity<>(errorResponse(response, ((ServiceUnavailbleException) e).getErrCode(), e), HttpStatus.SERVICE_UNAVAILABLE);
        } else if (e instanceof ServerException) {
            return new ResponseEntity<>(errorResponse(response, ((ServerException) e).getErrCode(), e), HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>(errorResponse(response, ErrorCodes.INTERNAL_SERVER_ERROR, e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected Response errorResponse(Response response, ErrorCodes code, java.lang.Exception e) {
        ResponseError error = new ResponseError(code, e.getMessage(), e.getCause());
        response.setError(error);
        return response;
    }


}

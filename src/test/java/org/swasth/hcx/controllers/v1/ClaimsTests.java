package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class ClaimsTests extends BaseSpec {

    @Value("${hcx_application.api_version}")
    private String api_version;
  
  @Test
  public void check_claim_submit_success_scenario() throws Exception {
      postgresService.execute("DROP TABLE IF EXISTS mock_participant");
      postgresService.execute("CREATE TABLE mock_participant (" +
              "parent_participant_code character varying," +
              "child_participant_code character varying NOT NULL PRIMARY KEY," +
              "primary_email character varying," +
              "password character varying," +
              "private_key character varying" +
              ");");
      postgresService.execute("INSERT INTO mock_participant (parent_participant_code, child_participant_code, primary_email, password, private_key) " +
              "VALUES ('hosp_hcx_955522@swasth-hcx-dev', 'payr_hcxmoc_903588@swasth-hcx-dev', 'lagertha+mock_payor@yopmail.com', 'Opensaber@123', '-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDAQLG7dTHuSE1F 8L46wS84SCU4Pf+ofSB8/h8tlyUHCw2SmY3CRZ3Uz1Jtw87UCdEvaSqs4YvkdKIh 1Nt04xpunUufUYNx6wBx/S/nuG7zcUu+bQeO1SpeEAA6+4HvnAyVBc+YCctnyREr Pgigs8MJG6dMA2rEap1na8VTrVN6JNNhcJxqnCyMoZ/aJIrmRxMj0Eh55LC20AZh jkxt5q/12qarpc/0ItQ2nv11fucojVVQyQGBaeqULVM9fGXbtquLXnRLJWzTLuSN Zk3Z+NpZZ5W7gxyBsxGTc7ccWp5edOxMXK3wELxsqirT0EOIz2ZMEYVbWrCyxfGC WYGUGRkPAgMBAAECggEACRAjsMweDfcfmW/3b8qioLsZ8hROJJG9gdby/lG1P6dA PQx7F9DzV1BsuNr1uZ4628rP5bV+REdRS1n+/HwmR1en9HtBwJGNBFKkuv5BUL2j DfeS1yNqa2PCtwUbQ4HuP+o1tpujmKFsa0bx3i2LQLcqhFVW56qFABafB0pQ+J2F 5MIgoS3e84Q/4oHYhxcRCgINQRoWKkqLGdAMg83NGe/G97PHGW9PPG9AQnfVhqey adov6rO/VOhbEYoKIwBq4G2qFnq6GwxDsx+wKl7SMVIwJzk8lRd5IaCGNscGEirU f/1PKxRT5WR735wyFyqLtSATFlrvH+sIiKsYR0PRlQKBgQDewV/umGidbm4jcmYR PFW53Ipr05lz4VNUOxdI/NIQ5jNfwjTE7Ep4R2xWycgzFwtoYYzWOhgHfSEcQ7KH 4z7w5Oe5CO+NB9viTxsrRx/kc6KU5cr3iqYCQzu2IaaVL2n6a0dDWR1ZXSGckKCf RWENuPkU9BAMn5LqEbcxVkSp6wKBgQDc8e4tMef6jpX5i8UEtd3y0HE302I1opfM 7Sdl5KVYsTOcTVobYM3niRKGtaY26EP7JCI2D0rCGS/DpDmsf7v5uHD4pZ4whRGR uhj64NIzveG3goki/ZerFZTc83mBxcaSBg48XL2x4oy32d0YXRC5IqEZ5vAIo7JE PlOZgKZAbQKBgQCVlVN/XHc76NErGoIx5stGYTPOzqxIZbJYaMntKGFEGeG71yUq a+ZiOMwmx5+Zt0rg0EkL/rzuInfejBNSLyawC3cUCU0b7oAYy7hZ6owfFAvEYz9V 1ZQ/BrkNSDGeEtXe5LVZI3mS7fhJM9G4wpU26tTQ3kf8whec+UKEbgLz+wKBgH1H EG2/5nFDd/ZPMh9Bn9+WIRwuV3liAADKV5SG2No5X7I4iecMNLQcHM0VInHvsNwt 2NgeQ51a+qJL9AfDRaqK0kOqZM/OI/AlPRTDdWyxNdL/yRJ2CBGR4x6FT1gt9DHI DTpcu5KpinCUr7Co1FqlMAOn1mKPrvzS+kQHljzhAoGBAL2Ui1c/qrX9ezggR78D WTK8u4QkvSdOoSDVbOes1FYEExHWh8hvRfO4PLTzlK58MJJnWj9hgiMItWzI7Iic pOEsHqgNl19VjdtKEloRAe5HYuC0pTTNTXo7qhq/De1wPr/lM82jofNXzpSpDetU 43Q9DXQ5KNiBbsKedfkRhJdY\n-----END PRIVATE KEY-----')");
      String requestBody = getRequestBodyClaims();
      MvcResult mvcResult = mockMvc.perform(post("/"+ api_version + "/claim/submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      System.out.println(response.getContentAsString());
      int status = response.getStatus();
      assertEquals(202, status);
  }
//  @Test
//  public void check_claim_submit_exception_scenario() throws Exception {
//      postgresService.execute("DROP TABLE IF EXISTS mock_participant");
//      postgresService.execute("CREATE TABLE mock_participant (\n" +
//              "    parent_participant_code character varying,\n" +
//              "    child_participant_code character varying PRIMARY KEY,\n" +
//              "    primary_email character varying,\n" +
//              "    password character varying,\n" +
//              "    private_key character varying\n" +
//              ");");
//      postgresService.execute("INSERT INTO mock_participant(parent_participant_code,child_participant_code,primary_email,password,private_key)"+"VALUES ('hosp_hcx_955522@swasth-hcx-dev ','payr_hcxmoc_903588@swasth-hcx-dev','lagertha+mock_payor@yopmail.com','Opensaber@123','-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCK6oTARsBHWD1M 8KTeMDrzkMPklmLilUqxqUXgjKfACdAsZLZtMlxLgWviFRxRglAgC33gS5On6DIE poiK6DQn2gushScJl6bRlA4vGCmYA+cO2d8O0rpkkJxWGq0K5LGb7umBkSNO0hyG DgngHviH8pYkqn9D1X7FuG2t912X4qg/db5Fp3NuS8ZRWJ9tk7fgWzLveNizsPS3 m8bqxikJ/RJL9g0EHURtzJWc2liBYGTtqhO5oeM2kovmaOn7a//AhnTcOZn2/eOe WiOmPRxSjjFyugREK+bAGTSfVq83003CJQ+kHg+cfOqBbCT+0J0LwLQAeLiKrLPS 07HVns3TAgMBAAECggEAFfns32gx90zH1y9YvbJpQ7/6pY5+/Ukl7xOQM5USMrNl ZKiCzaVCbErWBjGrGwqft2S/WKehfSt4F5FAQk1izDfWp0fMpFwCmYmFHEEuegHg C1Lp/UksEJ2/WxskQKLA+vdhpyGOsY5UJzGlsU2PqXN1+j34x9fzaBR5ps8JK5kF rbwr8En+hsfShSn3nklMiEyBI7bPDbpaq2Qjpfay/XRzqRdkPhsd+n03ADKGd4ax lyAe/VFak6JdpTy+oxWjc2dr2mOWIX9sOCSVOL7sTVX3Wcxhg0FlNbwmbx6nF+UW OJxVO9UNod/OtzSzYsqObJ/z76itUMcBnDl7almtjQKBgQC3Doo8UlvECcQvkrny Nk49oOLaheZA8R2Cw+3oK3ufMvahLEemxCFlZjbkmoeeRySzyPCs7jEr8HK1GQGu tv8yS4mkx7lCR2B3jl0NJ7r7kcitZh4VFaMXJORwpMoawMeWvc/u7Xh+OZRMp7c2 bfc9114dynCDtwp+dLjG8+y+5wKBgQDCRTlxXu7Kg5AL7H07n5q1r9IfOqfL4inB LXNEciddvaOtFzomjr/6sbGdK+ICeFyk9E12esWnbziaI/E8oSogL4dX+t5hbc5T tvrPs/mILztR1xvUdTNLyT9SOr7dEehytvOLsuHlFcCli7H6nZdrRCVFgeYXWZhM KR5Uagl4NQKBgQCOTHY2+AqvtKvWE2gKmh5uF9/g4Q+hUg2PtkD9JrgdhB9mIKa+ Q152lWN8h4d/CWzFeSFmPG7q6ioxDvRY3ZY5gbDI8BzaIeQia/93l3fp0WS/Lk+a MkyqVBpkWiVlcJB2ZKz73Yu6C4Z1pDZu0ELOxtk5rUGTkjlNHez5c2qI5wKBgQC1 NbMWQBIHjt6vcKGEGyVJcj5SaPkZodWG3ulVIBH+S6VAEJlqW99RbayaSdOgsDri lwsmh/CUdJdcmEguYLqVmR/q/hOu16kMx3J+iYcp87ymYzsPW19EwuywvCFKvqiP UH4ugeZaNIclJAAZICScps0JX9iLUURRTk1/OgWpPQKBgAD6pmbZCYk0B8TClPvs FSV12HUdMo9f7jteQP4TkmJhi+IOrMmBWVrwH6JgPf/awJPhmGM+g375JswSeugd u7ovcvivsL5JKNMeM35SfrF3xZ3VxX7jcOt/gM1nK9vXgGMJivf5TQIaF0piuwzG b5tlHhnq33FF0Y8MYB2Y4d4B\n-----END PRIVATE KEY-----')");
//      String requestBody = getExceptionRequestBody();
//      MvcResult mvcResult = mockMvc.perform(post("/"+ api_version + "/claim/submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
//      MockHttpServletResponse response = mvcResult.getResponse();
//      System.out.println(response.getContentAsString());
//      int status = response.getStatus();
//      assertEquals(400, status);
//  }

  @Test
  public void check_claim_on_submit_success_scenario() throws Exception {
      postgresService.execute("DROP TABLE IF EXISTS mock_participant");
      postgresService.execute("CREATE TABLE mock_participant (\n" +
              "    parent_participant_code character varying,\n" +
              "    child_participant_code character varying PRIMARY KEY,\n" +
              "    primary_email character varying,\n" +
              "    password character varying,\n" +
              "    private_key character varying\n" +
              ");");
      postgresService.execute("INSERT INTO mock_participant(parent_participant_code,child_participant_code,primary_email,password,private_key)"+"VALUES ('hosp_hcx_955522@swasth-hcx-dev ','payr_hcxmoc_903588@swasth-hcx-dev','lagertha+mock_payor@yopmail.com','Opensaber@123','-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDAQLG7dTHuSE1F 8L46wS84SCU4Pf+ofSB8/h8tlyUHCw2SmY3CRZ3Uz1Jtw87UCdEvaSqs4YvkdKIh 1Nt04xpunUufUYNx6wBx/S/nuG7zcUu+bQeO1SpeEAA6+4HvnAyVBc+YCctnyREr Pgigs8MJG6dMA2rEap1na8VTrVN6JNNhcJxqnCyMoZ/aJIrmRxMj0Eh55LC20AZh jkxt5q/12qarpc/0ItQ2nv11fucojVVQyQGBaeqULVM9fGXbtquLXnRLJWzTLuSN Zk3Z+NpZZ5W7gxyBsxGTc7ccWp5edOxMXK3wELxsqirT0EOIz2ZMEYVbWrCyxfGC WYGUGRkPAgMBAAECggEACRAjsMweDfcfmW/3b8qioLsZ8hROJJG9gdby/lG1P6dA PQx7F9DzV1BsuNr1uZ4628rP5bV+REdRS1n+/HwmR1en9HtBwJGNBFKkuv5BUL2j DfeS1yNqa2PCtwUbQ4HuP+o1tpujmKFsa0bx3i2LQLcqhFVW56qFABafB0pQ+J2F 5MIgoS3e84Q/4oHYhxcRCgINQRoWKkqLGdAMg83NGe/G97PHGW9PPG9AQnfVhqey adov6rO/VOhbEYoKIwBq4G2qFnq6GwxDsx+wKl7SMVIwJzk8lRd5IaCGNscGEirU f/1PKxRT5WR735wyFyqLtSATFlrvH+sIiKsYR0PRlQKBgQDewV/umGidbm4jcmYR PFW53Ipr05lz4VNUOxdI/NIQ5jNfwjTE7Ep4R2xWycgzFwtoYYzWOhgHfSEcQ7KH 4z7w5Oe5CO+NB9viTxsrRx/kc6KU5cr3iqYCQzu2IaaVL2n6a0dDWR1ZXSGckKCf RWENuPkU9BAMn5LqEbcxVkSp6wKBgQDc8e4tMef6jpX5i8UEtd3y0HE302I1opfM 7Sdl5KVYsTOcTVobYM3niRKGtaY26EP7JCI2D0rCGS/DpDmsf7v5uHD4pZ4whRGR uhj64NIzveG3goki/ZerFZTc83mBxcaSBg48XL2x4oy32d0YXRC5IqEZ5vAIo7JE PlOZgKZAbQKBgQCVlVN/XHc76NErGoIx5stGYTPOzqxIZbJYaMntKGFEGeG71yUq a+ZiOMwmx5+Zt0rg0EkL/rzuInfejBNSLyawC3cUCU0b7oAYy7hZ6owfFAvEYz9V 1ZQ/BrkNSDGeEtXe5LVZI3mS7fhJM9G4wpU26tTQ3kf8whec+UKEbgLz+wKBgH1H EG2/5nFDd/ZPMh9Bn9+WIRwuV3liAADKV5SG2No5X7I4iecMNLQcHM0VInHvsNwt 2NgeQ51a+qJL9AfDRaqK0kOqZM/OI/AlPRTDdWyxNdL/yRJ2CBGR4x6FT1gt9DHI DTpcu5KpinCUr7Co1FqlMAOn1mKPrvzS+kQHljzhAoGBAL2Ui1c/qrX9ezggR78D WTK8u4QkvSdOoSDVbOes1FYEExHWh8hvRfO4PLTzlK58MJJnWj9hgiMItWzI7Iic pOEsHqgNl19VjdtKEloRAe5HYuC0pTTNTXo7qhq/De1wPr/lM82jofNXzpSpDetU 43Q9DXQ5KNiBbsKedfkRhJdY\n-----END PRIVATE KEY-----')");
      String requestBody = getRequestBodyClaims();
      MvcResult mvcResult = mockMvc.perform(post("/"+ api_version + "/claim/on_submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
      MockHttpServletResponse response = mvcResult.getResponse();
      int status = response.getStatus();
      assertEquals(202, status);
  }

//  @Test
//  public void check_claim_on_submit_exception_scenario() throws Exception {
//      String requestBody = getExceptionRequestBody();
//      MvcResult mvcResult = mockMvc.perform(post("/"+ api_version + "/claim/on_submit").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
//      MockHttpServletResponse response = mvcResult.getResponse();
//      int status = response.getStatus();
//      assertEquals(500, status);
//  }

}
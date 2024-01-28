package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class NotificationControllerTests extends BaseSpec {
    @Value("${hcx_application.api_version}")
    private String api_version;
    @Test
    public void notification_subscribe_success_scenario() throws Exception {
        postgresService.execute("DROP TABLE IF EXISTS payersystem_data");
        String requestBody = getBspListExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/bsp/request/list").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(500, status);
    }
}

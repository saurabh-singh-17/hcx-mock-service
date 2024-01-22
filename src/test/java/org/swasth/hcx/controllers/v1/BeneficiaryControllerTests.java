package org.swasth.hcx.controllers.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class BeneficiaryControllerTests extends BaseSpec {

    @Value("${hcx_application.api_version}")
    private String api_version;

    @Test
    public void create_coverage_eligibility_success_scenario() throws Exception {
        String requestBody = getCreateCoverageEligibilityRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/create/coverageeligibility/check").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(202, status);
    }
}

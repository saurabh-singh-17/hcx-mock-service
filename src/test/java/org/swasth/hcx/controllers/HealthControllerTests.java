package org.swasth.hcx.controllers;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.swasth.hcx.dto.Response;
import org.swasth.hcx.exception.ClientException;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class HealthControllerTests extends BaseSpec{

    @InjectMocks
    HealthController healthController;
    protected MockMvc mockMvc;
    @BeforeEach
    void setup()  {
        this.mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }


    @Test
    public void testServiceHealth() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/service/health")).andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
    }


}

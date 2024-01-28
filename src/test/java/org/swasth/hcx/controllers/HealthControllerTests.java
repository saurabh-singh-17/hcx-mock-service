package org.swasth.hcx.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

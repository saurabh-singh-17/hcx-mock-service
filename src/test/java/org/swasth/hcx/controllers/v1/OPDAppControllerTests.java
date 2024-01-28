package org.swasth.hcx.controllers.v1;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.swasth.hcx.controllers.BaseSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class OPDAppControllerTests extends BaseSpec {

    @Value("${hcx_application.api_version}")
    private String api_version;

    @Test
    public void add_consultation_info_success_scenario() throws Exception {
        postgresService.execute("DROP TABLE IF EXISTS consultation_info");
        postgresService.execute(
                "CREATE TABLE consultation_info\n" +
                        "(\n" +
                        "  workflow_id character varying NOT NULL PRIMARY KEY,\n" +
                        "  treatment_type character varying,\n" +
                        "  service_type character varying,\n" +
                        "  symptoms character varying,\n" +
                        "  supporting_documents_url character varying\n" +
                        ");"
        );
        String requestBody = getConsultationAddRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/consultation/add").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    public void addConsultationInfoInvalidWorkflowIdScenario() throws Exception {
        // Drop and create the table before the test
        postgresService.execute("DROP TABLE IF EXISTS consultation_info");
        postgresService.execute(
                "CREATE TABLE consultation_info\n" +
                        "(\n" +
                        "  workflow_id character varying NOT NULL PRIMARY KEY,\n" +
                        "  treatment_type character varying,\n" +
                        "  service_type character varying,\n" +
                        "  symptoms character varying,\n" +
                        "  supporting_documents_url character varying\n" +
                        ");"
        );
        String requestBody = getConsultationAddFailRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/consultation/add")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String status = response.getContentAsString();
        assertEquals("Work flow id cannot be empty", status);
    }

    @Test
    public void addConsultationInfoErrorWhilePerformingDbScenario() throws Exception {
        // Drop and create the table before the test
        postgresService.execute("DROP TABLE IF EXISTS consultation_info");
        postgresService.execute(
                "CREATE TABLE consultation_info\n" +
                        "(\n" +
                        "  workflow_id character varying NOT NULL PRIMARY KEY,\n" +
                        "  treatment_type character varying,\n" +
                        "  service_type character varying,\n" +
                        "  symptoms character varying,\n" +
                        "  supporting_documents_url character varying\n" +
                        ");"
        );
        String requestBody = getConsultationAddExceptionRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/consultation/add")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    public void get_consultation_info_success_scenario() throws Exception {
        postgresService.execute("DROP TABLE IF EXISTS consultation_info");
        postgresService.execute(
                "CREATE TABLE consultation_info\n" +
                        "(\n" +
                        "  workflow_id character varying NOT NULL PRIMARY KEY,\n" +
                        "  treatment_type character varying,\n" +
                        "  service_type character varying,\n" +
                        "  symptoms character varying,\n" +
                        "  supporting_documents_url character varying\n" +
                        ");"
        );
        postgresService.execute( "INSERT INTO consultation_info" +
                "(workflow_id, treatment_type, service_type, symptoms, supporting_documents_url)" +
                "VALUES" +
                "('7a549702-ddb2-4e72-ac21-438d1fe3c439', 'OPD', 'consultation', 'fever', 'http://123.com')");
        MvcResult mvcResult = mockMvc.perform(get( "/" + api_version + "/consultation/7a549702-ddb2-4e72-ac21-438d1fe3c439").contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    public void get_consultation_info_doesnot_exists_scenario() throws Exception {
        postgresService.execute("DROP TABLE IF EXISTS consultation_info");
        postgresService.execute(
                "CREATE TABLE consultation_info\n" +
                        "(\n" +
                        "  workflow_id character varying NOT NULL PRIMARY KEY,\n" +
                        "  treatment_type character varying,\n" +
                        "  service_type character varying,\n" +
                        "  symptoms character varying,\n" +
                        "  supporting_documents_url character varying\n" +
                        ");"
        );
        postgresService.execute( "INSERT INTO consultation_info" +
                "(workflow_id, treatment_type, service_type, symptoms, supporting_documents_url)" +
                "VALUES" +
                "('7a549702-ddb2-4e72-ac21-438d1fe3c4675', 'OPD', 'consultation', 'fever', 'http://123.com')");
        MvcResult mvcResult = mockMvc.perform(get( "/" + api_version + "/consultation/7a549702-ddb2-4e72-ac21-438d1fe3c439").contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(400, status);
    }

    @Test
    public void get_consultation_info_not_found_exception_scenario() throws Exception {
        postgresService.execute("DROP TABLE IF EXISTS consultation_info");
        postgresService.execute(
                "CREATE TABLE consultation_info\n" +
                        "(\n" +
                        "  workflow_id character varying NOT NULL PRIMARY KEY,\n" +
                        "  treatment_type character varying,\n" +
                        "  service_type character varying,\n" +
                        "  symptoms character varying,\n" +
                        "  supporting_documents_url character varying\n" +
                        ");"
        );
        postgresService.execute( "INSERT INTO consultation_info" +
                "(workflow_id, treatment_type, service_type, symptoms, supporting_documents_url)" +
                "VALUES" +
                "('null', 'OPD', 'consultation', 'fever', 'http://123.com')");
        MvcResult mvcResult = mockMvc.perform(get( "/" + api_version + "/consultation/7a549702-ddb2-4e72-ac21-438d1fe3c439").contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(400, status);
    }
}

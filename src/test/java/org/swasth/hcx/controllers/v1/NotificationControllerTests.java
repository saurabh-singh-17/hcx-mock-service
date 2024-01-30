package org.swasth.hcx.controllers.v1;

import com.github.fppt.jedismock.RedisServer;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.swasth.hcx.controllers.BaseSpec;
import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.service.PostgresService;
import org.swasth.hcx.service.RedisService;
import org.swasth.hcx.utils.JSONUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;

import static com.google.common.base.CharMatcher.any;
import static com.google.common.base.Verify.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class NotificationControllerTests extends BaseSpec {
    @Value("${hcx_application.api_version}")
    private String api_version;
    @Mock
    protected RedisService redis;
    @InjectMocks
    private NotificationController notificationController;

    private RedisServer redisServer;

    private static EmbeddedPostgres embeddedPostgres;
    @BeforeEach
    public void setup() throws ClientException, IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        embeddedPostgres = EmbeddedPostgres.builder().setPort(5432).start();
        String jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres");
        postgresService = new PostgresService(jdbcUrl, "postgres", "postgres");
        redisServer = RedisServer.newRedisServer(6379);
        redisServer.start();
        jedisMock = new Jedis("localhost",6379);
        jedisMock.getConnection();
        jedisMock.set("bsp","{\"topic_code\":\"notif-new-network-feature-added\",\"message\":\"Swasth-HCX now supports v0.9 on its platform. All participants can now initiate transactions relating to v0.9.\",\"sender_code\":\"hcxgateway.swasth@swasth-hcx-dev\",\"timestamp\":1706507135959}");
    }

    @AfterEach
    public void tearDown() throws IOException {
        redisServer.stop();
        jedisMock.close();
    }

    @Mock
    private Jedis jedisMock;

    @Test
    public void notification_request_list_matchingKey_empty_success_scenario() throws Exception {
        String requestBody = getNotificationListRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/notification/list").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    public void notification_request_list_exception_scenario() throws Exception {
        String participantRole = "bsp";
        List<Map<String, Object>> mockData = Arrays.asList(
                Map.of("bsp:notif-new-network-feature-added ", "{\"topic_code\":\"notif-new-network-feature-added\",\"message\":\"Swasth-HCX now supports v0.9 on its platform. All participants can now initiate transactions relating to v0.9.\",\"sender_code\":\"hcxgateway.swasth@swasth-hcx-dev\",\"timestamp\":1706507135959}")
        );
        when(redis.get(getNotificationListRequestBody())).thenReturn(mockData);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("participant_role", participantRole);
        ResponseEntity<Object> responseEntity = notificationController.getNotification(requestBody);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void notification_request_list_participant_code_success_scenario() throws Exception {
        String requestBody = getNotificationListParticipantCodeRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/notification/list").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(200, status);
    }

    @Test
    public void post_notification_notify_success_scenario() throws Exception {
        postgresService.execute("DROP TABLE IF EXISTS mock_participant");
        postgresService.execute("CREATE TABLE mock_participant (\n" +
                "    parent_participant_code character varying,\n" +
                "    child_participant_code character varying PRIMARY KEY NOT NULL,\n" +
                "    primary_email character varying,\n" +
                "    password character varying,\n" +
                "    private_key character varying\n" +
                ");");
        String requestBody = getNotificationNotifyRequestBody();
        MvcResult mvcResult = mockMvc.perform(post("/" + api_version + "/notification/notify").content(requestBody).contentType(MediaType.APPLICATION_JSON)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        System.out.println(response.getContentAsString());
        int status = response.getStatus();
        assertEquals(202, status);
    }
}

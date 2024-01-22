package org.swasth.hcx.dto;

import org.swasth.hcx.exception.ClientException;
import org.swasth.hcx.exception.ErrorCodes;
import org.swasth.hcx.utils.JSONUtils;

import java.util.Map;
import static org.swasth.hcx.utils.Constants.*;

public class Request {

    private final Map<String, Object> payload;
    protected Map<String, Object> hcxHeaders = null;

    private String action;

    public Request(Map<String, Object> body, String action) throws Exception {
        this.payload = body;
        this.action = action;
        try {
            if (body.containsKey(PAYLOAD)) {
                hcxHeaders = JSONUtils.decodeBase64String(((String) body.get(PAYLOAD)).split("\\.")[0], Map.class);
            } else if (body.containsKey(STATUS))
                hcxHeaders = body;
        } catch (Exception e) {
            throw new ClientException(ErrorCodes.ERR_INVALID_PAYLOAD, "Invalid Payload");
        }
    }

    // TODO remove this method. We should restrict accessing it to have a clean code.
    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getWorkflowId() {
        return getHeader(WORKFLOW_ID);
    }

    public String getApiCallId() {
        return getHeader(API_CALL_ID);
    }

    public String getCorrelationId() {
        return getHeader(CORRELATION_ID);
    }

    public String getSenderCode() {
        return getHeader(SENDER_CODE);
    }

    public String getRecipientCode() {
        return getHeader(RECIPIENT_CODE);
    }

    public String getTimestamp() {
        return getHeader(TIMESTAMP);
    }

    public String getStatus() {
        return getHeader(STATUS);
    }

    public Map<String, Object> getHcxHeaders() {
        return hcxHeaders;
    }

    protected String getHeader(String key) {
        return (String) hcxHeaders.getOrDefault(key, null);
    }

    protected Map<String, Object> getHeaderMap(String key) {
        return (Map<String, Object>) hcxHeaders.getOrDefault(key, null);
    }

    public String getSubscriptionId() { return (String) payload.get(SUBSCRIPTION_ID); }

    public String getAction() { return this.action; }
}


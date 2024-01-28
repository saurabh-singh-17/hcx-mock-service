package org.swasth.hcx.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String VERSION_PREFIX = "/v0.7";

    // Coverage Eligibility APIs
    public static final String COVERAGE_ELIGIBILITY_CHECK = VERSION_PREFIX + "/coverageeligibility/check";
    public static final String COVERAGE_ELIGIBILITY_ONCHECK = VERSION_PREFIX + "/coverageeligibility/on_check";

    public static final String CREATE_COVERAGEELIGIBILITY_REQUEST =  "/create/coverageeligibility/check";
    public static final String CREATE_CLAIM_SUBMIT = "/create/claim/submit";
    public static final String CREATE_PRE_AUTH_SUBMIT = "/create/preauth/submit";
    public static final String CREATE_COMMUNICATION_REQUEST = "/create/communication/request";
    public static final String CREATE_COMMUNICATION_ON_REQUEST = "create/communication/on_request";
    public static final String COVERAGE_ELIGIBILITY = "coverageeligibility";
    public static final String COMMUNICATION = "communication";
    public static final String CLAIM = "claim";
    public static final String PRE_AUTH = "preauth";
    public static final String BSP_REQUEST_LIST = "/bsp/request/list";
    public static final String SEND_OTP = "/send/otp";
    public static final String VERIFY_OTP = "verify/otp";
    public static final String UPLOAD_DOCUMENTS  = "/upload/documents";
    // Claims APIs
    public static final String PRE_AUTH_SUBMIT = VERSION_PREFIX + "/preauth/submit";
    public static final String PRE_AUTH_ONSUBMIT = VERSION_PREFIX + "/preauth/on_submit";
    public static final String CLAIM_SUBMIT = VERSION_PREFIX + "/claim/submit";
    public static final String CLAIM_ONSUBMIT = VERSION_PREFIX + "/claim/on_submit";
    public static final String CONSULTATION_ADD = "/consultation/add";

    //Payment Notice APIs
    public static final String PAYMENT_NOTICE_REQUEST = VERSION_PREFIX + "/paymentnotice/request";
    public static final String PAYMENT_NOTICE_ONREQUEST = VERSION_PREFIX + "/paymentnotice/on_request";

    //Communication APIs
    public static final String COMMUNICATION_REQUEST = "/communication/request";
    public static final String COMMUNICATION_ONREQUEST = VERSION_PREFIX + "/communication/on_request";

    //Predetermination APIs
    public static final String PREDETERMINATION_SUBMIT = VERSION_PREFIX + "/predetermination/submit";
    public static final String PREDETERMINATION_ONSUBMIT = VERSION_PREFIX + "/predetermination/on_submit";

    public static final String HEALTHY = "healthy";

    //event generator props
    public static final String MID = "mid";
    public static final String PAYLOAD = "payload";
    public static final String ETS = "ets";
    public static final String ACTION = "action";
    public static final String HEADERS = "headers";
    public static final String JOSE="jose";
    public static final String PROTOCOL = "protocol";

    //Request props
    public static final String PROTOCOL_HEADERS_MANDATORY = "protocol.headers.mandatory";
    public static final String ERROR_HEADERS_MANDATORY = "plainrequest.headers.mandatory";
    public static final String ERROR_HEADERS_OPTIONAL = "plainrequest.headers.optional";
    public static final String PROTOCOL_HEADERS_OPTIONAL = "protocol.headers.optional";
    public static final String REDIRECT_HEADERS_MANDATORY = "redirect.headers.mandatory";
    public static final String REDIRECT_HEADERS_OPTIONAL = "redirect.headers.optional";
    public static final String JOSE_HEADERS = "headers.jose";
    public static final String SENDER_CODE = "x-hcx-sender_code";
    public static final String RECIPIENT_CODE = "x-hcx-recipient_code";
    public static final String API_CALL_ID = "x-hcx-api_call_id";
    public static final String CORRELATION_ID = "x-hcx-correlation_id";
    public static final String WORKFLOW_ID = "x-hcx-workflow_id";
    public static final String TIMESTAMP = "x-hcx-timestamp";
    public static final String DEBUG_FLAG = "x-hcx-debug_flag";
    public static final String STATUS = "x-hcx-status";
    public static final String ERROR_DETAILS = "x-hcx-error_details";
    public static final String DEBUG_DETAILS = "x-hcx-debug_details";
    public static final String SERVICE_MODE = "service.mode";
    public static final String GATEWAY = "gateway";

    public static final String PARTIAL_STATUS = "response.partial";

    public static final String REDIRECT_STATUS = "response.redirect";

    // Notification APIs
    public static final String NOTIFICATION_SUBSCRIBE = "/notification/subscribe";
    public static final String NOTIFICATION_NOTIFY = "/notification/notify";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final String APPLICANT_CODE = "applicant_code";
    public static final String VERIFIER_CODE = "verifier_code";
    public static final String RESULT = "result";
    public static final String ACCEPTED = "accepted";
    public static final String VERIFICATION_TOKEN = "verification_token";
    public static final String MOBILE = "mobile";
    public static final String SUB = "sub";
    public static final String ISS = "iss";
    public static final String ADDITIONAL_VERIFICATION = "additionalVerification";
    public static final String PENDING = "Pending";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";
    public static final String SUCCESSFUL = "successful";
    public static final String FAILED = "failed";
    public static final List<String> PAYOR_APPROVAL_TYPES = Arrays.asList("medical", "financial");
}

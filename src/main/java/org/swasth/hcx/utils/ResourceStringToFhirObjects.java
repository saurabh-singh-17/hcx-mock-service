package org.swasth.hcx.utils;

import org.hl7.fhir.r4.model.*;

public enum ResourceStringToFhirObjects {

    COVERAGE_ELIGIBILITY_CHECK("/coverageeligibility/check", Bundle.class, CoverageEligibilityRequest.class),
    COVERAGE_ELIGIBILITY_ON_CHECK("/coverageeligibility/on_check", Bundle.class, CoverageEligibilityResponse.class),
    PRE_AUTH_SUBMIT("/preauth/submit", Bundle.class, Claim.class),
    PRE_AUTH_ON_SUBMIT("/preauth/on_submit", Bundle.class, ClaimResponse.class),
    CLAIM_SUBMIT("/claim/submit", Bundle.class, Claim.class),
    CLAIM_ON_SUBMIT("/claim/on_submit", Bundle.class, ClaimResponse.class),
    PAYMENT_NOTICE_REQUEST("/paymentnotice/request", PaymentNotice.class, PaymentNotice.class),
    PAYMENT_NOTICE_ON_REQUEST("/paymentnotice/on_request", PaymentReconciliation.class, PaymentReconciliation.class),
    COMMUNICATION_REQUEST("/communication/request", CommunicationRequest.class, CommunicationRequest.class),
    COMMUNICATION_ON_REQUEST("/communication/on_request", Communication.class, Communication.class),
    PREDETERMINATION_SUBMIT("/predetermination/submit", Bundle.class, Claim.class),
    PREDETERMINATION_ON_SUBMIT("/predetermination/on_submit", Bundle.class, ClaimResponse.class);
    private final String api;
    private final Class fhirClass;
    private final Class mainClass;

    private ResourceStringToFhirObjects(String api, Class fhirClass, Class mainClass) {
        this.api = api;
        this.fhirClass = fhirClass;
        this.mainClass = mainClass;
    }

    public String getApi() {
        return this.api;
    }

    public Class getFhirClass() {
        return this.fhirClass;
    }

    public Class getMainClass() {
        return this.mainClass;
    }

    public static String getEnumByString(String code){
        for(ResourceStringToFhirObjects e : ResourceStringToFhirObjects.values()){
            if(e.api.equals(code)) return e.name();
        }
        return null;
    }
}

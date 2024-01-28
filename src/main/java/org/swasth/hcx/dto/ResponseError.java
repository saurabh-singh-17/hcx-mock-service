package org.swasth.hcx.dto;

import org.swasth.hcx.exception.ErrorCodes;

public class ResponseError {
    private ErrorCodes code;
    private String message;
    private Throwable trace;

    public ResponseError() {}

    public ResponseError(ErrorCodes code, String message, Throwable trace) {
        this.code = code;
        this.message = message;
        this.trace = trace;
    }

    public ErrorCodes getCode() {
        return code;
    }

    public void setCode(ErrorCodes code) {
        this.code = code;
    }

}

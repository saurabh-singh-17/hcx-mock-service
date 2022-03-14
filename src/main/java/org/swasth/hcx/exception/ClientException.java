package org.swasth.hcx.exception;

import org.swasth.hcx.exception.ErrorCodes;

public class ClientException extends Exception {

    private org.swasth.hcx.exception.ErrorCodes errCode;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(org.swasth.hcx.exception.ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }

}

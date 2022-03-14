package org.swasth.hcx.exception;

import org.swasth.hcx.exception.ErrorCodes;

import javax.naming.ServiceUnavailableException;

public class ServiceUnavailbleException extends ServiceUnavailableException {

    private org.swasth.hcx.exception.ErrorCodes errCode;

    public ServiceUnavailbleException(String message) {
        super(message);
    }

    public ServiceUnavailbleException(org.swasth.hcx.exception.ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }
}

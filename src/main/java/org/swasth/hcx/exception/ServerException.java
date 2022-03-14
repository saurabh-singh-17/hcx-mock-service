package org.swasth.hcx.exception;

import org.swasth.hcx.exception.ErrorCodes;

public class ServerException extends Exception{

    private org.swasth.hcx.exception.ErrorCodes errCode;

    public ServerException(String message) {
        super(message);
    }

    public ServerException(org.swasth.hcx.exception.ErrorCodes errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public ErrorCodes getErrCode() {
        return errCode;
    }
}

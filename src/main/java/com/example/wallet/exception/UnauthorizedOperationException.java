package com.example.wallet.exception;
import org.springframework.http.HttpStatus;

public class UnauthorizedOperationException extends ApiException {
    public UnauthorizedOperationException(String message) {
        super(HttpStatus.FORBIDDEN, "UNAUTHORIZED_OPERATION", message);
    }
}

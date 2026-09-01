package com.example.wallet.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends ApiException {
    public InsufficientBalanceException(String message) {
        super(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", message);
    }
}

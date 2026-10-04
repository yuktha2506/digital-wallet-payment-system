package com.example.wallet.service;
import com.example.wallet.dto.request.TransferRequest;
import com.example.wallet.dto.response.TransferResponse;

public interface PaymentService {
    TransferResponse transfer(String senderEmail, TransferRequest request);
}

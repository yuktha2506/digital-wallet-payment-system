package com.example.wallet.dto.response;

import com.example.wallet.entity.TransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        String transactionId,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal cashback,
        String senderWallet,
        String receiverWallet,
        Instant timestamp
) {
}

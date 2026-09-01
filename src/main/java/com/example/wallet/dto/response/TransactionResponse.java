package com.example.wallet.dto.response;

import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String transactionId,
        TransactionStatus status,
        TransactionType transactionType,
        BigDecimal amount,
        String description,
        String senderWallet,
        String receiverWallet,
        Instant timestamp
) {
}

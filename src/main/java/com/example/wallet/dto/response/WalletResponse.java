package com.example.wallet.dto.response;
import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(String walletNumber, BigDecimal balance, String ownerName, Instant createdAt, Instant updatedAt) {
}

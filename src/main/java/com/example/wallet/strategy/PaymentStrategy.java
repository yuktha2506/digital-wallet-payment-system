package com.example.wallet.strategy;
import com.example.wallet.entity.Wallet;
import java.math.BigDecimal;

public interface PaymentStrategy {
    void apply(Wallet sender, Wallet receiver, BigDecimal amount);
}

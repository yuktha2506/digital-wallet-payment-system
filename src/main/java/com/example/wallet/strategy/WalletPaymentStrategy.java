package com.example.wallet.strategy;
import com.example.wallet.entity.Wallet;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class WalletPaymentStrategy implements PaymentStrategy {
    @Override
    public void apply(Wallet sender, Wallet receiver, BigDecimal amount) {
        sender.debit(amount);
        receiver.credit(amount);
    }
}

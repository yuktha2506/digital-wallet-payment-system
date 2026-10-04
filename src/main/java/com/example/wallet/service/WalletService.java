package com.example.wallet.service;
import com.example.wallet.dto.request.MoneyRequest;
import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.dto.response.WalletResponse;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletResponse getWallet(String email);
    BigDecimal getBalance(String email);
    TransactionResponse addMoney(String email, MoneyRequest request);
    TransactionResponse withdraw(String email, MoneyRequest request);
    List<WalletResponse> getAllWallets();
}

package com.example.wallet.service;

import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import java.math.BigDecimal;

public interface RewardService {
    BigDecimal applyCashback(Wallet senderWallet, WalletTransaction transferTransaction);
    BigDecimal cashbackFor(WalletTransaction transferTransaction);
}

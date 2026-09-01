package com.example.wallet.service.impl;

import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.dto.response.UserResponse;
import com.example.wallet.dto.response.WalletResponse;
import com.example.wallet.entity.User;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;

final class DtoMapper {
    private DtoMapper() {
    }

    static UserResponse user(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), user.getCreatedAt());
    }

    static WalletResponse wallet(Wallet wallet) {
        return new WalletResponse(wallet.getWalletNumber(), wallet.getBalance(), wallet.getUser().getName(), wallet.getCreatedAt(), wallet.getUpdatedAt());
    }

    static TransactionResponse transaction(WalletTransaction transaction) {
        String sender = transaction.getSenderWallet() == null ? null : transaction.getSenderWallet().getWalletNumber();
        String receiver = transaction.getReceiverWallet() == null ? null : transaction.getReceiverWallet().getWalletNumber();
        return new TransactionResponse(transaction.getTransactionId(), transaction.getStatus(), transaction.getTransactionType(),
                transaction.getAmount(), transaction.getDescription(), sender, receiver, transaction.getCreatedAt());
    }
}

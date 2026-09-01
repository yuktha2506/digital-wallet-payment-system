package com.example.wallet.service.impl;

import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.exception.UnauthorizedOperationException;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(String email, TransactionStatus status, TransactionType type, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserEmail(email).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        return transactionRepository.findUserTransactions(wallet.getId(), status, type, pageable).map(DtoMapper::transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String email, String transactionId) {
        Wallet wallet = walletRepository.findByUserEmail(email).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        WalletTransaction transaction = transactionRepository.findDetailedByTransactionId(transactionId)
                .orElseThrow(() -> new WalletNotFoundException("Transaction not found"));
        boolean ownsSender = transaction.getSenderWallet() != null && transaction.getSenderWallet().getId().equals(wallet.getId());
        boolean ownsReceiver = transaction.getReceiverWallet() != null && transaction.getReceiverWallet().getId().equals(wallet.getId());
        if (!ownsSender && !ownsReceiver) {
            throw new UnauthorizedOperationException("You cannot view another user's transaction");
        }
        return DtoMapper.transaction(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(DtoMapper::transaction);
    }
}

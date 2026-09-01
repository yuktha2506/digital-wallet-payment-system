package com.example.wallet.service.impl;

import com.example.wallet.dto.request.MoneyRequest;
import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.dto.response.WalletResponse;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.WalletService;
import com.example.wallet.util.IdGenerator;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletServiceImpl implements WalletService {
    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(String email) {
        return DtoMapper.wallet(findByEmail(email));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String email) {
        return findByEmail(email).getBalance();
    }

    @Override
    @Transactional
    public TransactionResponse addMoney(String email, MoneyRequest request) {
        Wallet wallet = walletRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        wallet.credit(request.amount());
        WalletTransaction transaction = transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), null, wallet,
                request.amount(), TransactionType.ADD_MONEY, TransactionStatus.SUCCESS, description(request.description(), "Money added")));
        log.info("Add money successful transactionId={} wallet={}", transaction.getTransactionId(), wallet.getWalletNumber());
        return DtoMapper.transaction(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(String email, MoneyRequest request) {
        Wallet wallet = walletRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        wallet.debit(request.amount());
        WalletTransaction transaction = transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), wallet, null,
                request.amount(), TransactionType.WITHDRAW, TransactionStatus.SUCCESS, description(request.description(), "Money withdrawn")));
        log.info("Withdraw successful transactionId={} wallet={}", transaction.getTransactionId(), wallet.getWalletNumber());
        return DtoMapper.transaction(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> getAllWallets() {
        return walletRepository.findAll().stream().map(DtoMapper::wallet).toList();
    }

    private Wallet findByEmail(String email) {
        return walletRepository.findByUserEmail(email).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    private String description(String requested, String fallback) {
        return requested == null || requested.isBlank() ? fallback : requested;
    }
}

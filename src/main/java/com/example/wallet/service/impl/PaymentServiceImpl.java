package com.example.wallet.service.impl;
import com.example.wallet.dto.request.TransferRequest;
import com.example.wallet.dto.response.TransferResponse;
import com.example.wallet.entity.PaymentRequest;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.exception.InvalidPaymentException;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.PaymentRequestRepository;
import com.example.wallet.repository.RewardRepository;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.PaymentService;
import com.example.wallet.service.RewardService;
import com.example.wallet.strategy.WalletPaymentStrategy;
import com.example.wallet.util.IdGenerator;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final RewardRepository rewardRepository;
    private final RewardService rewardService;
    private final WalletPaymentStrategy walletPaymentStrategy;

    public PaymentServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository,
                              PaymentRequestRepository paymentRequestRepository, RewardRepository rewardRepository,
                              RewardService rewardService, WalletPaymentStrategy walletPaymentStrategy) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.rewardRepository = rewardRepository;
        this.rewardService = rewardService;
        this.walletPaymentStrategy = walletPaymentStrategy;
    }

    @Override
    @Transactional
    public TransferResponse transfer(String senderEmail, TransferRequest request) {
        var existingRequest = paymentRequestRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existingRequest.isPresent()) {
            if (existingRequest.get().getTransaction() != null) {
                return responseFor(existingRequest.get().getTransaction());
            }
            throw new InvalidPaymentException("Payment request is still being processed");
        }

        Wallet senderSnapshot = walletRepository.findByUserEmail(senderEmail)
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));
        Wallet receiverSnapshot = walletRepository.findByWalletNumber(request.receiverWalletNumber())
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));
        if (senderSnapshot.getId().equals(receiverSnapshot.getId())) {
            throw new InvalidPaymentException("Cannot transfer to the same wallet");
        }

        Wallet first = lockFirst(senderSnapshot, receiverSnapshot);
        Wallet second = lockSecond(senderSnapshot, receiverSnapshot);
        Wallet sender = first.getId().equals(senderSnapshot.getId()) ? first : second;
        Wallet receiver = first.getId().equals(receiverSnapshot.getId()) ? first : second;

        PaymentRequest paymentRequest;
        try {
            paymentRequest = paymentRequestRepository.saveAndFlush(new PaymentRequest(request.idempotencyKey(), sender, receiver, request.amount()));
        } catch (DataIntegrityViolationException ex) {
            return paymentRequestRepository.findByIdempotencyKey(request.idempotencyKey())
                    .filter(existing -> existing.getTransaction() != null)
                    .map(existing -> responseFor(existing.getTransaction()))
                    .orElseThrow(() -> new InvalidPaymentException("Payment request is still being processed"));
        }

        log.info("Payment initiated idempotencyKey={} senderWallet={} receiverWallet={}", request.idempotencyKey(),
                sender.getWalletNumber(), receiver.getWalletNumber());
        walletPaymentStrategy.apply(sender, receiver, request.amount());
        WalletTransaction transferTransaction = transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), sender,
                receiver, request.amount(), TransactionType.TRANSFER, TransactionStatus.SUCCESS, description(request.description())));
        BigDecimal cashback = rewardService.applyCashback(sender, transferTransaction);
        paymentRequest.markSuccess(transferTransaction);
        log.info("Payment successful transactionId={} cashback={}", transferTransaction.getTransactionId(), cashback);
        return new TransferResponse(transferTransaction.getTransactionId(), transferTransaction.getStatus(), transferTransaction.getAmount(),
                cashback, sender.getWalletNumber(), receiver.getWalletNumber(), transferTransaction.getCreatedAt());
    }

    private Wallet lockFirst(Wallet sender, Wallet receiver) {
        Long firstId = Math.min(sender.getId(), receiver.getId());
        return walletRepository.findByIdForUpdate(firstId).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    private Wallet lockSecond(Wallet sender, Wallet receiver) {
        Long secondId = Math.max(sender.getId(), receiver.getId());
        return walletRepository.findByIdForUpdate(secondId).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    private String description(String description) {
        return description == null || description.isBlank() ? "Wallet transfer" : description;
    }

    private TransferResponse responseFor(WalletTransaction transaction) {
        BigDecimal cashback = rewardRepository.findByTransaction(transaction)
                .map(reward -> reward.getCashbackAmount())
                .orElse(BigDecimal.ZERO.setScale(2));
        return new TransferResponse(transaction.getTransactionId(), transaction.getStatus(), transaction.getAmount(), cashback,
                transaction.getSenderWallet().getWalletNumber(), transaction.getReceiverWallet().getWalletNumber(), transaction.getCreatedAt());
    }

}


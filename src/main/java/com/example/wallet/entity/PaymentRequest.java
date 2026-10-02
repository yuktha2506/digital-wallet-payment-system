package com.example.wallet.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_requests", indexes = @Index(name = "idx_payment_requests_idempotency_key", columnList = "idempotency_key"))
public class PaymentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_wallet_id", nullable = false)
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_wallet_id", nullable = false)
    private Wallet receiverWallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private WalletTransaction transaction;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentRequest() {
    }

    public PaymentRequest(String idempotencyKey, Wallet senderWallet, Wallet receiverWallet, BigDecimal amount) {
        this.idempotencyKey = idempotencyKey;
        this.senderWallet = senderWallet;
        this.receiverWallet = receiverWallet;
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.status = TransactionStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void markSuccess(WalletTransaction transaction) {
        this.transaction = transaction;
        this.status = TransactionStatus.SUCCESS;
    }

    public Long getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Wallet getSenderWallet() { return senderWallet; }
    public Wallet getReceiverWallet() { return receiverWallet; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public WalletTransaction getTransaction() { return transaction; }
    public Instant getCreatedAt() { return createdAt; }
}

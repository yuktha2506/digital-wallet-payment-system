package com.example.wallet.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rewards")
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private WalletTransaction transaction;

    @Column(name = "cashback_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashbackAmount;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Reward() {
    }

    public Reward(User user, WalletTransaction transaction, BigDecimal cashbackAmount, String description) {
        this.user = user;
        this.transaction = transaction;
        this.cashbackAmount = cashbackAmount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public WalletTransaction getTransaction() { return transaction; }
    public BigDecimal getCashbackAmount() { return cashbackAmount; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}

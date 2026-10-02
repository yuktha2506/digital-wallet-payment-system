package com.example.wallet.entity;
import com.example.wallet.exception.InsufficientBalanceException;
import com.example.wallet.exception.InvalidPaymentException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallets", indexes = @Index(name = "idx_wallets_wallet_number", columnList = "wallet_number"))
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_number", nullable = false, unique = true, length = 40)
    private String walletNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Wallet() {
    }

    public Wallet(String walletNumber, User user, BigDecimal openingBalance) {
        this.walletNumber = walletNumber;
        this.user = user;
        this.balance = money(openingBalance);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void credit(BigDecimal amount) {
        validatePositive(amount);
        balance = balance.add(money(amount));
    }

    public void debit(BigDecimal amount) {
        validatePositive(amount);
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }
        balance = balance.subtract(money(amount));
    }

    private static void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Amount must be greater than zero");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public String getWalletNumber() { return walletNumber; }
    public User getUser() { return user; }
    public BigDecimal getBalance() { return balance; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

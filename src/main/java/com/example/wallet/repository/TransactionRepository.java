package com.example.wallet.repository;

import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.WalletTransaction;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByTransactionId(String transactionId);

    @Query("""
            select t from WalletTransaction t
            left join fetch t.senderWallet sw
            left join fetch t.receiverWallet rw
            where t.transactionId = :transactionId
            """)
    Optional<WalletTransaction> findDetailedByTransactionId(@Param("transactionId") String transactionId);

    @Query("""
            select t from WalletTransaction t
            where (t.senderWallet.id = :walletId or t.receiverWallet.id = :walletId)
              and (:status is null or t.status = :status)
              and (:type is null or t.transactionType = :type)
            """)
    Page<WalletTransaction> findUserTransactions(@Param("walletId") Long walletId,
                                                 @Param("status") TransactionStatus status,
                                                 @Param("type") TransactionType type,
                                                 Pageable pageable);

    long countByTransactionType(TransactionType transactionType);
}

package com.example.wallet.repository;
import com.example.wallet.entity.PaymentRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    @EntityGraph(attributePaths = {"transaction", "senderWallet", "receiverWallet"})
    Optional<PaymentRequest> findByIdempotencyKey(String idempotencyKey);
}

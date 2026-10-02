package com.example.wallet.repository;
import com.example.wallet.entity.Reward;
import com.example.wallet.entity.WalletTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    Optional<Reward> findByTransaction(WalletTransaction transaction);
}

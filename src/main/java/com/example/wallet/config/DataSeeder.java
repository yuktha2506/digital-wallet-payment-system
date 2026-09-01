package com.example.wallet.config;

import com.example.wallet.entity.PaymentRequest;
import com.example.wallet.entity.Reward;
import com.example.wallet.entity.Role;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.User;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.repository.PaymentRequestRepository;
import com.example.wallet.repository.RewardRepository;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.util.IdGenerator;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(SeedRunner seedRunner) {
        return args -> seedRunner.seed();
    }

    @Configuration
    static class SeedRunner {
        private final boolean enabled;
        private final UserRepository userRepository;
        private final WalletRepository walletRepository;
        private final TransactionRepository transactionRepository;
        private final PaymentRequestRepository paymentRequestRepository;
        private final RewardRepository rewardRepository;
        private final PasswordEncoder passwordEncoder;

        SeedRunner(@Value("${app.seed.enabled:true}") boolean enabled, UserRepository userRepository,
                   WalletRepository walletRepository, TransactionRepository transactionRepository,
                   PaymentRequestRepository paymentRequestRepository, RewardRepository rewardRepository,
                   PasswordEncoder passwordEncoder) {
            this.enabled = enabled;
            this.userRepository = userRepository;
            this.walletRepository = walletRepository;
            this.transactionRepository = transactionRepository;
            this.paymentRequestRepository = paymentRequestRepository;
            this.rewardRepository = rewardRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        void seed() {
            if (!enabled || userRepository.existsByEmail("admin@wallet.test")) {
                return;
            }
            User admin = userRepository.save(new User("Admin User", "admin@wallet.test", "9000000000", passwordEncoder.encode("Admin@123"), Role.ADMIN));
            User anaya = userRepository.save(new User("Anaya Sharma", "anaya@wallet.test", "9000000001", passwordEncoder.encode("User@1234"), Role.USER));
            User rohan = userRepository.save(new User("Rohan Mehta", "rohan@wallet.test", "9000000002", passwordEncoder.encode("User@1234"), Role.USER));
            User kavya = userRepository.save(new User("Kavya Iyer", "kavya@wallet.test", "9000000003", passwordEncoder.encode("User@1234"), Role.USER));

            Wallet adminWallet = walletRepository.save(new Wallet("WALLETADMIN001", admin, new BigDecimal("10000.00")));
            Wallet anayaWallet = walletRepository.save(new Wallet("WALLETUSER001", anaya, new BigDecimal("2500.00")));
            Wallet rohanWallet = walletRepository.save(new Wallet("WALLETUSER002", rohan, new BigDecimal("1500.00")));
            walletRepository.save(new Wallet("WALLETUSER003", kavya, new BigDecimal("750.00")));

            WalletTransaction addMoney = transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), null, anayaWallet,
                    new BigDecimal("2500.00"), TransactionType.ADD_MONEY, TransactionStatus.SUCCESS, "Opening balance"));
            WalletTransaction transfer = transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), anayaWallet, rohanWallet,
                    new BigDecimal("500.00"), TransactionType.TRANSFER, TransactionStatus.SUCCESS, "Seed payment"));
            WalletTransaction cashback = transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), null, anayaWallet,
                    new BigDecimal("10.00"), TransactionType.CASHBACK, TransactionStatus.SUCCESS, "Cashback for seed payment"));
            transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), adminWallet, null,
                    new BigDecimal("250.00"), TransactionType.WITHDRAW, TransactionStatus.SUCCESS, "Admin test withdrawal"));
            paymentRequestRepository.save(new PaymentRequest("SEED-PAYMENT-001", anayaWallet, rohanWallet, new BigDecimal("500.00")));
            rewardRepository.save(new Reward(anaya, transfer, new BigDecimal("10.00"), "Seed cashback via " + cashback.getTransactionId()));
            addMoney.getTransactionId();
        }
    }
}

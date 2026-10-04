package com.example.wallet.service.impl;
import com.example.wallet.entity.Reward;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.Wallet;
import com.example.wallet.entity.WalletTransaction;
import com.example.wallet.repository.RewardRepository;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.service.RewardService;
import com.example.wallet.strategy.RewardPaymentStrategy;
import com.example.wallet.util.IdGenerator;
import com.example.wallet.util.MoneyUtil;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class RewardServiceImpl implements RewardService {
    private static final BigDecimal CASHBACK_PERCENT = new BigDecimal("0.02");
    private static final BigDecimal MAX_CASHBACK = new BigDecimal("100.00");
    private final RewardRepository rewardRepository;
    private final TransactionRepository transactionRepository;
    private final RewardPaymentStrategy rewardPaymentStrategy;

    public RewardServiceImpl(RewardRepository rewardRepository, TransactionRepository transactionRepository,
                             RewardPaymentStrategy rewardPaymentStrategy) {
        this.rewardRepository = rewardRepository;
        this.transactionRepository = transactionRepository;
        this.rewardPaymentStrategy = rewardPaymentStrategy;
    }

    @Override
    public BigDecimal applyCashback(Wallet senderWallet, WalletTransaction transferTransaction) {
        BigDecimal cashback = cashbackFor(transferTransaction);
        if (cashback.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        rewardPaymentStrategy.apply(null, senderWallet, cashback);
        WalletTransaction cashbackTransaction = transactionRepository.save(new WalletTransaction(IdGenerator.transactionId(), null,
                senderWallet, cashback, TransactionType.CASHBACK, TransactionStatus.SUCCESS,
                "Cashback for " + transferTransaction.getTransactionId()));
        rewardRepository.save(new Reward(senderWallet.getUser(), transferTransaction, cashback,
                "2% cashback credited via " + cashbackTransaction.getTransactionId()));
        return cashback;
    }

    @Override
    public BigDecimal cashbackFor(WalletTransaction transferTransaction) {
        BigDecimal calculated = transferTransaction.getAmount().multiply(CASHBACK_PERCENT);
        BigDecimal capped = calculated.min(MAX_CASHBACK);
        return MoneyUtil.scale(capped);
    }
}

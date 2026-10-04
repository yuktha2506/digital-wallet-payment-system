package com.example.wallet.service;
import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    Page<TransactionResponse> getTransactions(String email, TransactionStatus status, TransactionType type, Pageable pageable);
    TransactionResponse getTransaction(String email, String transactionId);
    Page<TransactionResponse> getAllTransactions(Pageable pageable);
}

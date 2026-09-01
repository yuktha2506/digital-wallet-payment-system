package com.example.wallet.controller;

import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.entity.TransactionStatus;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.security.CustomUserDetails;
import com.example.wallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Get authenticated user's transaction history")
    public Page<TransactionResponse> transactions(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) TransactionStatus status,
                                                  @RequestParam(required = false) TransactionType type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return transactionService.getTransactions(userDetails.getUsername(), status, type, pageable);
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get one transaction owned by authenticated user")
    public TransactionResponse transaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable String transactionId) {
        return transactionService.getTransaction(userDetails.getUsername(), transactionId);
    }
}

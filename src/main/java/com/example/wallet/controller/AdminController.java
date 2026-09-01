package com.example.wallet.controller;

import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.dto.response.UserResponse;
import com.example.wallet.dto.response.WalletResponse;
import com.example.wallet.service.TransactionService;
import com.example.wallet.service.UserService;
import com.example.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
public class AdminController {
    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    public AdminController(UserService userService, WalletService walletService, TransactionService transactionService) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public List<UserResponse> users() {
        return userService.getAllUsers();
    }

    @GetMapping("/wallets")
    @Operation(summary = "List all wallets")
    public List<WalletResponse> wallets() {
        return walletService.getAllWallets();
    }

    @GetMapping("/transactions")
    @Operation(summary = "List all transactions")
    public Page<TransactionResponse> transactions(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return transactionService.getAllTransactions(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}

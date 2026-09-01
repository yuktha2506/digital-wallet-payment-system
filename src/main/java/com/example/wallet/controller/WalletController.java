package com.example.wallet.controller;

import com.example.wallet.dto.request.MoneyRequest;
import com.example.wallet.dto.response.TransactionResponse;
import com.example.wallet.dto.response.WalletResponse;
import com.example.wallet.security.CustomUserDetails;
import com.example.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@Tag(name = "Wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @Operation(summary = "Get authenticated user's wallet")
    public WalletResponse getWallet(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return walletService.getWallet(userDetails.getUsername());
    }

    @GetMapping("/balance")
    @Operation(summary = "Get authenticated user's wallet balance")
    public Map<String, BigDecimal> balance(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Map.of("balance", walletService.getBalance(userDetails.getUsername()));
    }

    @PostMapping("/add-money")
    @Operation(summary = "Add simulated money to wallet")
    public TransactionResponse addMoney(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @Valid @RequestBody MoneyRequest request) {
        return walletService.addMoney(userDetails.getUsername(), request);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw simulated money from wallet")
    public TransactionResponse withdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @Valid @RequestBody MoneyRequest request) {
        return walletService.withdraw(userDetails.getUsername(), request);
    }
}

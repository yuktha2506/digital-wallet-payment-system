package com.example.wallet.controller;
import com.example.wallet.dto.request.TransferRequest;
import com.example.wallet.dto.response.TransferResponse;
import com.example.wallet.security.CustomUserDetails;
import com.example.wallet.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money from authenticated user's wallet to another wallet")
    public TransferResponse transfer(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @Valid @RequestBody TransferRequest request) {
        return paymentService.transfer(userDetails.getUsername(), request);
    }
}

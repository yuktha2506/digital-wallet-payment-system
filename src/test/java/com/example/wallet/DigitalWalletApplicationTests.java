package com.example.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wallet.dto.request.MoneyRequest;
import com.example.wallet.dto.request.TransferRequest;
import com.example.wallet.dto.response.TransferResponse;
import com.example.wallet.dto.response.UserResponse;
import com.example.wallet.entity.Role;
import com.example.wallet.entity.TransactionType;
import com.example.wallet.entity.User;
import com.example.wallet.entity.Wallet;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.security.CustomUserDetails;
import com.example.wallet.security.JwtService;
import com.example.wallet.service.PaymentService;
import com.example.wallet.service.UserService;
import com.example.wallet.service.WalletService;
import com.example.wallet.util.IdGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DigitalWalletApplicationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserService userService;
    @Autowired WalletService walletService;
    @Autowired PaymentService paymentService;
    @Autowired WalletRepository walletRepository;
    @Autowired UserRepository userRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @Test
    void registrationDuplicateEmailLoginAndInvalidLogin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Asha","email":"asha@test.com","phone":"9876543210","password":"Password1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("asha@test.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Asha Two","email":"asha@test.com","phone":"9876543211","password":"Password1"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"asha@test.com","password":"Password1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"asha@test.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addMoneyWithdrawAndRejectInsufficientBalance() throws Exception {
        String token = registerAndLogin("wallet@test.com", "9876543212");

        mockMvc.perform(post("/api/wallet/add-money").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + quote("amount") + ":1000.00," + quote("description") + ":" + quote("Top up") + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("ADD_MONEY"));

        mockMvc.perform(post("/api/wallet/withdraw").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + quote("amount") + ":250.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("WITHDRAW"));

        mockMvc.perform(post("/api/wallet/withdraw").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + quote("amount") + ":1000.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_BALANCE"));

        mockMvc.perform(get("/api/wallet/balance").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(750.00));
    }

    @Test
    void transferCreatesTransactionRewardsAndSupportsIdempotency() {
        userService.register(register("sender@test.com", "9876543213"));
        userService.register(register("receiver@test.com", "9876543214"));
        walletService.addMoney("sender@test.com", new MoneyRequest(new BigDecimal("1000.00"), "fund account"));
        String receiverWallet = walletRepository.findByUserEmail("receiver@test.com").orElseThrow().getWalletNumber();

        TransferRequest request = new TransferRequest(receiverWallet, new BigDecimal("500.00"), "Dinner", "PAY-123");
        TransferResponse first = paymentService.transfer("sender@test.com", request);
        TransferResponse duplicate = paymentService.transfer("sender@test.com", request);

        assertThat(duplicate.transactionId()).isEqualTo(first.transactionId());
        assertThat(first.cashback()).isEqualByComparingTo("10.00");
        assertThat(transactionRepository.countByTransactionType(TransactionType.TRANSFER)).isEqualTo(1);
        assertThat(walletRepository.findByUserEmail("sender@test.com").orElseThrow().getBalance()).isEqualByComparingTo("510.00");
        assertThat(walletRepository.findByUserEmail("receiver@test.com").orElseThrow().getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void transferRejectsInvalidReceiverInvalidAmountAndRollsBackInsufficientBalance() throws Exception {
        String senderToken = registerAndLogin("payer@test.com", "9876543215");
        String receiverToken = registerAndLogin("payee@test.com", "9876543216");
        String receiverWallet = walletNumber(receiverToken);

        mockMvc.perform(post("/api/payments/transfer").header("Authorization", bearer(senderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"receiverWalletNumber":"NOPE","amount":50.00,"description":"test","idempotencyKey":"BAD-RECEIVER"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/payments/transfer").header("Authorization", bearer(senderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"receiverWalletNumber":"%s","amount":0.00,"description":"test","idempotencyKey":"BAD-AMOUNT"}
                                """.formatted(receiverWallet)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/payments/transfer").header("Authorization", bearer(senderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"receiverWalletNumber":"%s","amount":700.00,"description":"test","idempotencyKey":"NO-FUNDS"}
                                """.formatted(receiverWallet)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_BALANCE"));

        assertThat(walletRepository.findByWalletNumber(receiverWallet).orElseThrow().getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void authorizationProtectsAdminApisAndOtherUsersTransactions() throws Exception {
        String userToken = registerAndLogin("normal@test.com", "9876543217");
        String senderToken = registerAndLogin("owner@test.com", "9876543218");
        String receiverToken = registerAndLogin("receiver2@test.com", "9876543219");
        String receiverWallet = walletNumber(receiverToken);
        mockMvc.perform(post("/api/wallet/add-money").header("Authorization", bearer(senderToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":300.00}"))
                .andExpect(status().isOk());
        String transactionId = transfer(senderToken, receiverWallet, "AUTH-1").get("transactionId").asText();

        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/transactions/" + transactionId).header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        String adminToken = createAdminAndToken();
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void concurrentTransfersDoNotOverdrawWallet() throws Exception {
        userService.register(register("concurrent@test.com", "9876543220"));
        userService.register(register("target@test.com", "9876543221"));
        walletService.addMoney("concurrent@test.com", new MoneyRequest(new BigDecimal("1000.00"), "fund account"));
        String receiverWallet = walletRepository.findByUserEmail("target@test.com").orElseThrow().getWalletNumber();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> transferA = () -> attemptTransfer("concurrent@test.com", receiverWallet, "CONCURRENT-A");
        Callable<Boolean> transferB = () -> attemptTransfer("concurrent@test.com", receiverWallet, "CONCURRENT-B");
        List<Boolean> results = executor.invokeAll(List.of(transferA, transferB)).stream()
                .map(future -> {
                    try { return future.get(); } catch (Exception ex) { return false; }
                }).toList();
        executor.shutdown();

        assertThat(results).containsExactlyInAnyOrder(true, false);
        assertThat(walletRepository.findByUserEmail("concurrent@test.com").orElseThrow().getBalance()).isEqualByComparingTo("314.00");
        assertThat(walletRepository.findByUserEmail("target@test.com").orElseThrow().getBalance()).isEqualByComparingTo("700.00");
    }

    private boolean attemptTransfer(String senderEmail, String receiverWallet, String key) {
        try {
            paymentService.transfer(senderEmail, new TransferRequest(receiverWallet, new BigDecimal("700.00"), "race", key));
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String registerAndLogin(String email, String phone) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register(email, phone))))
                .andExpect(status().isCreated());
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private JsonNode transfer(String token, String receiverWallet, String key) throws Exception {
        String response = mockMvc.perform(post("/api/payments/transfer").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"receiverWalletNumber":"%s","amount":100.00,"description":"test","idempotencyKey":"%s"}
                                """.formatted(receiverWallet, key)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String walletNumber(String token) throws Exception {
        String response = mockMvc.perform(get("/api/wallet").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("walletNumber").asText();
    }

    private String createAdminAndToken() {
        User admin = userRepository.save(new User("Test Admin", "admin-test@test.com", "9876543230",
                passwordEncoder.encode("Adminpass1"), Role.ADMIN));
        return jwtService.generateToken(new CustomUserDetails(admin));
    }

    private com.example.wallet.dto.request.RegisterRequest register(String email, String phone) {
        return new com.example.wallet.dto.request.RegisterRequest("Test User", email, phone, "Password1");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String quote(String value) {
        return "\"" + value + "\"";
    }
}

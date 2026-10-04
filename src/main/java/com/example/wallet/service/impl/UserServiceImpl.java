package com.example.wallet.service.impl;
import com.example.wallet.dto.request.LoginRequest;
import com.example.wallet.dto.request.RegisterRequest;
import com.example.wallet.dto.response.AuthResponse;
import com.example.wallet.dto.response.UserResponse;
import com.example.wallet.entity.Role;
import com.example.wallet.entity.User;
import com.example.wallet.entity.Wallet;
import com.example.wallet.exception.DuplicateTransactionException;
import com.example.wallet.exception.UserNotFoundException;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.security.CustomUserDetails;
import com.example.wallet.security.JwtService;
import com.example.wallet.service.UserService;
import com.example.wallet.util.IdGenerator;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, WalletRepository walletRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateTransactionException("Email is already registered");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicateTransactionException("Phone is already registered");
        }
        User user = userRepository.save(new User(request.name(), request.email(), request.phone(),
                passwordEncoder.encode(request.password()), Role.USER));
        walletRepository.save(new Wallet(IdGenerator.walletNumber(), user, BigDecimal.ZERO));
        log.info("Registered user {}", user.getEmail());
        return DtoMapper.user(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for {}", request.email());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            log.warn("Login failed for {}", request.email());
            throw ex;
        }
        User user = getByEmail(request.email());
        String token = jwtService.generateToken(new CustomUserDetails(user));
        log.info("Login successful for {}", request.email());
        return new AuthResponse(token, DtoMapper.user(user));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(DtoMapper::user).toList();
    }
}

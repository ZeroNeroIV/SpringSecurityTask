package com.zerowhisper.secondtask.Service;

import com.zerowhisper.secondtask.Repository.RefreshTokenRepository;
import com.zerowhisper.secondtask.Repository.UserAccountRepository;
import com.zerowhisper.secondtask.Security.JWTUtility;
import com.zerowhisper.secondtask.dto.SignUpAttributeInputDto;
import com.zerowhisper.secondtask.dto.VerifyAccountDto;
import com.zerowhisper.secondtask.model.RefreshToken;
import com.zerowhisper.secondtask.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JWTUtility jwtUtility;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder, UserService userService, JWTUtility jwtUtility, AuthenticationManager authenticationManager, RefreshTokenRepository refreshTokenRepository, UserAccountRepository userAccountRepository, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtility = jwtUtility;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.emailService = emailService;
    }

    public UserAccount signup(SignUpAttributeInputDto sign) {

        UserAccount userAccount = userService.createUser(
                passwordEncoder.encode(sign.getPassword()),
                sign.getEmail(),
                sign.getUsername());

        userAccount.setVerificationCode(generateVerificationToken());
        sendVerificationEmail(userAccount);
        userAccount.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));

        return userAccountRepository.save(userAccount);
    }

    public ResponseEntity<?> login(String email, String password) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email shouldn't be empty.");
        }
        if (password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password shouldn't be null.");
        }
        try {
            // Authenticate the user
            Optional<UserAccount> optionalUserAccount = userAccountRepository.findByEmail(email);

            if (optionalUserAccount.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            UserAccount user = optionalUserAccount.get();

            // To check if the username/password is/are correct
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), password));

            // User is not enabled
            if (!user.isEnabled()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String accessToken = jwtUtility.generateAccessToken(user.getUsername());
            String refreshToken = jwtUtility.generateRefreshToken(user.getUsername());

            RefreshToken token = new RefreshToken();
            token.setUser(user);
            token.setRefreshToken(refreshToken);
            refreshTokenRepository.save(token);

            // Prepare the response body
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            // Return the tokens in the response
            return ResponseEntity.ok(tokens);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }
    }

    public void verifyUser(VerifyAccountDto verifyAccountDto) {
        Optional<UserAccount> optionalUser = userAccountRepository
                .findByEmail(verifyAccountDto.email);
        if (optionalUser.isPresent()) {
            UserAccount userAccount = optionalUser.get();
            if (userAccount.getVerificationTokenExpiry().isBefore(LocalDateTime.now()))
                throw new RuntimeException("Verification token expired");
            if (userAccount.getVerificationCode().equals(verifyAccountDto.code)) {
                userAccount.setEnabled(true);
                userAccount.setVerificationCode(null);
                userAccount.setVerificationTokenExpiry(null);
                userAccountRepository.save(userAccount);
            } else {
                throw new RuntimeException("Invalid verification token");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    private void sendVerificationEmail(UserAccount userAccount) {
        String subject = "Account Verification";
        String verificationCode = userAccount.getVerificationCode();
        String message = "<h2>Hello "
                + userAccount.getUsername()
                + ",</h2>\n\n"
                + "<b>Please use this verification code to verify your account: "
                + verificationCode
                + "</b>\n\n<h3>You have 10 minutes to verify your account."
                + "</h3>\n\n...<code>have a nice day :3 👍</code>";
        emailService.sendVerificationEmail(userAccount.getEmail(), subject, message);
    }

    private String generateVerificationToken() {
        Random rand = new Random();
        Integer randomInteger = rand.nextInt(90000000) + 10000000;
        return String.valueOf(randomInteger);
    }

}

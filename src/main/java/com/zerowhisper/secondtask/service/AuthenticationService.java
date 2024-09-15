package com.zerowhisper.secondtask.service;

import com.zerowhisper.secondtask.dto.LoginAttributeOutputDto;
import com.zerowhisper.secondtask.dto.SignUpAttributeInputDto;
import com.zerowhisper.secondtask.dto.VerifyAccountDto;
import com.zerowhisper.secondtask.model.RefreshToken;
import com.zerowhisper.secondtask.model.UserAccount;
import com.zerowhisper.secondtask.repository.RefreshTokenRepository;
import com.zerowhisper.secondtask.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserAccountService userAccountService;
    private final JWTUtility jwtUtility;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public AuthenticationService(
            PasswordEncoder passwordEncoder,
            UserAccountService userAccountService,
            JWTUtility jwtUtility,
            AuthenticationManager authenticationManager,
            RefreshTokenRepository refreshTokenRepository,
            UserAccountRepository userAccountRepository,
            EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountService = userAccountService;
        this.jwtUtility = jwtUtility;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.emailService = emailService;
    }

    // private -> generate new access token
    // when an access token is expired but there is a refresh token for the same user in the database


    // We have to use this (after the meeting cuz I am afraid it will destroy everything we worked for til now)
    private String generateAccessTokenIfNeeded(String accessToken) {
        if (jwtUtility.isAccessTokenExpired(accessToken)) {
            Long userId = jwtUtility.extractUserIdFromAccessToken(accessToken);
            Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(userId);
            if (optionalUserAccount.isEmpty())
                throw new RuntimeException("No such user.");
            UserAccount userAccount = optionalUserAccount.get();
            List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserId(userId);
            if (refreshTokens.isEmpty())
                throw new RuntimeException("Please log in.\nNo valid refresh token found.");
            for (RefreshToken r : refreshTokens) {
                if (!jwtUtility.isRefreshTokenExpired(r.getRefreshToken()))
                    return jwtUtility.generateAccessToken(userAccount);
                refreshTokenRepository.deleteRefreshTokenByRefreshTokenId(r.getTokenId());
            }
            throw new RuntimeException("No valid refresh token found for such user.");
        }
        return accessToken;
    }

    public UserAccount signup(SignUpAttributeInputDto sign) {

        UserAccount userAccount = userAccountService.createUser(
                passwordEncoder.encode(sign.getPassword()),
                sign.getEmail(),
                sign.getUsername());

        userAccount.setVerificationCode(generateVerificationToken());
        sendVerificationEmail(userAccount);
        userAccount.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));

        return userAccountRepository.save(userAccount);
    }

    public LoginAttributeOutputDto login(String email, String password) {
        if (email == null) {
            throw new IllegalArgumentException("Email shouldn't be empty.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password shouldn't be null.");
        }
        try {
            // Authenticate the user
            Optional<UserAccount> optionalUserAccount = userAccountRepository.findByEmail(email);

            if (optionalUserAccount.isEmpty()) {
                throw new RuntimeException("Invalid email or password.");
            }

            UserAccount user = optionalUserAccount.get();

            // To check if the username/password is/are correct
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), password));

            SecurityContextHolder.getContext().setAuthentication(auth);

            // User is not enabled
            if (!user.isEnabled()) {
                throw new IllegalStateException("User is not enabled.");
            }

            String accessToken = jwtUtility.generateAccessToken(user);
            String refreshToken = jwtUtility.generateRefreshToken(user);

            RefreshToken token = new RefreshToken();
            token.setUserAccount(user);
            token.setRefreshToken(refreshToken);
            refreshTokenRepository.save(token);

            // Prepare the response body
            LoginAttributeOutputDto output = new LoginAttributeOutputDto();
            output.setEmail(email);
            output.setUsername(user.getUsername());
            output.setAccessToken(accessToken);
            output.setRefreshToken(refreshToken);

            // Return the output in the response
            return output;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid email or password.");
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

    public String createAccessTokenFromRefreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty() /* refreshToken == "" */) {
            throw new IllegalArgumentException("Refresh token is required.");
        }

        // check if refreshToken is not expired
        if (jwtUtility.isRefreshTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token is expired.");
        }

        // Get userId from refreshToken
        Long id = jwtUtility.extractUserIdFromRefreshToken(refreshToken);
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(id);

        if (optionalUserAccount.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        UserAccount userAccount = optionalUserAccount.get();

        // Generate new accessToken from userAccount
        return jwtUtility.generateAccessToken(userAccount);

    }

    // When using DML use this annotation.
    // it has many benefits,
    // but the one I know is that it rolls back previous DML queries if an exception got raised.
    // -- Zero
    public void logOut(String token) {
        String jwtToken = token.replace("Bearer ", "");

        if (!isTokenValid(jwtToken)) {
            throw new RuntimeException("Invalid token. Please login again.");
        }

        Long id = jwtUtility.extractUserIdFromAccessToken(jwtToken);

        if (refreshTokenRepository.findAllByUserId(id).isEmpty()) {
            throw new RuntimeException("No refresh tokens found for this user.");
        }

        refreshTokenRepository.deleteAllTokensByUserId(id);
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

    private Boolean isTokenValid(String token) {
        return jwtUtility
                .isAccessTokenValid(token, jwtUtility.extractUsernameFromAccessToken(token))
                || !jwtUtility.isAccessTokenExpired(token);
    }
}

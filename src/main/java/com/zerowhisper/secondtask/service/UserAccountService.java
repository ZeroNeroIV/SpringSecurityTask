package com.zerowhisper.secondtask.service;

import com.zerowhisper.secondtask.dto.UserInfoDto;
import com.zerowhisper.secondtask.model.UserAccount;
import com.zerowhisper.secondtask.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;
    private final JWTUtility jwtUtility;

    @Autowired
    public UserAccountService(
            UserAccountRepository userAccountRepository,
            JWTUtility jwtUtility
    ) {
        this.userAccountRepository = userAccountRepository;
        this.jwtUtility = jwtUtility;
    }

    public UserAccount createUser(String password, String email, String username) {
        UserAccount userAccount = new UserAccount(username, email, password);
        return userAccountRepository.save(userAccount);
    }

    public UserAccount findByUsername(String username) throws Exception {
        Optional<UserAccount> user = userAccountRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new Exception("Username/Password is wrong.");
        }
        return user.get();
    }

    public UserAccount findByEmail(String email) {
        Optional<UserAccount> user = userAccountRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("Email not found");
        }
        return user.get();
    }

    public UserInfoDto getUserInfo(String token) throws Exception {
        String jwtToken = jwtUtility.getToken(token);

        if (jwtUtility.isAccessTokenExpired(jwtToken)) {
            throw new IllegalStateException("Account not enabled.\n" +
                    "Please verify your account.");
        }

        String username = jwtUtility.extractUsernameFromAccessToken(jwtToken);
        //Throw email extract from the database the info about the user

        UserAccount userAccount = findByUsername(username);

        if (!userAccount.isEnabled())
            throw new RuntimeException();

        return new UserInfoDto(
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getUsername(),
                userAccount.isEnabled());
    }

    public List<UserAccount> getAllUsersInfo(String token) {
        String jwtToken = token.replace("Bearer", "");
        if (!jwtUtility.extractAuthoritiesFromAccessToken(token).contains("ADMIN")) {
            throw new RuntimeException("Unauthorized access.");
        }
        return userAccountRepository.findAll();
    }
}

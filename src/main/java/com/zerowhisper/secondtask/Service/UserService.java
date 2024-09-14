package com.zerowhisper.secondtask.Service;

import com.zerowhisper.secondtask.Repository.UserAccountRepository;
import com.zerowhisper.secondtask.Security.JWTUtility;
import com.zerowhisper.secondtask.dto.RefreshTokenRequestBody;
import com.zerowhisper.secondtask.dto.UserInfoDto;
import com.zerowhisper.secondtask.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserAccountRepository userAccountRepository;
    private final JWTUtility jwtUtility;

    @Autowired
    public UserService(
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

    public UserInfoDto getUserInfo(RefreshTokenRequestBody token) throws Exception {
        String jwtToken = token.refreshToken.substring(7);

        if (jwtUtility.isTokenExpired(jwtToken))
            throw new IllegalStateException();

        String username = jwtUtility.extractUsername(jwtToken);

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
}

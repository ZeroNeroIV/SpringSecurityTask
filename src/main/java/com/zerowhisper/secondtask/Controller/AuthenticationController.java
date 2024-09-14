package com.zerowhisper.secondtask.Controller;

import com.zerowhisper.secondtask.Security.JWTUtility;
import com.zerowhisper.secondtask.Service.AuthenticationService;
import com.zerowhisper.secondtask.dto.LoginAttributeInputDto;
import com.zerowhisper.secondtask.dto.SignUpAttributeInputDto;
import com.zerowhisper.secondtask.dto.VerifyAccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private AuthenticationService authenticationService;
    private JWTUtility jwtUtility;

    @Autowired
    public AuthenticationController(
            JWTUtility jwtUtility,
            AuthenticationService authenticationService) {
        this.jwtUtility = jwtUtility;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpAttributeInputDto sign) {
        return new ResponseEntity<>(authenticationService.signup(sign), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifiedUser(@RequestBody VerifyAccountDto verifyAccountDto) {
        try {
            authenticationService.verifyUser(verifyAccountDto);
            return ResponseEntity.ok("Account Verified Successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Wrong code OR account verified");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginAttributeInputDto log) {
        return authenticationService.login(log.getEmail(), log.getPassword());
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {

        // TODO: Put this in the authenticationService

        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Refresh token is required.");
        }
//checkkkk
        if (jwtUtility.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token is expired.");
        }
        String username = jwtUtility.extractUsername(refreshToken);
// Generate new access token
        String newAccessToken = jwtUtility.generateAccessToken(username);

        // Return it
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);

        return ResponseEntity.ok(response);
    }

}

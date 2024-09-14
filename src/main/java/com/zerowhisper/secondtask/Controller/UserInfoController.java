package com.zerowhisper.secondtask.Controller;

import com.zerowhisper.secondtask.Security.JWTUtility;
import com.zerowhisper.secondtask.Service.UserService;
import com.zerowhisper.secondtask.dto.RefreshTokenRequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/user")
public class UserInfoController {

    private final UserService userService;
    private final JWTUtility jwtUtility;

    @Autowired
    public UserInfoController(
            UserService userService,
            JWTUtility jwtUtility
    ) {
        this.userService = userService;
        this.jwtUtility = jwtUtility;
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") RefreshTokenRequestBody token) {

        try {
            // Remove the "Bearer " prefix from the token
            return new ResponseEntity<>(userService.getUserInfo(token), HttpStatus.OK);
        } catch (IllegalStateException i) {
            return new ResponseEntity<>("Access token expired.", HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException r) {
            return new ResponseEntity<>(
                    "Account not enabled.\nPlease verify your account.",
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }
    }
}

package com.zerowhisper.secondtask.controller;

import com.zerowhisper.secondtask.dto.LoginAttributeInputDto;
import com.zerowhisper.secondtask.dto.RefreshTokenRequestBody;
import com.zerowhisper.secondtask.dto.SignUpAttributeInputDto;
import com.zerowhisper.secondtask.dto.VerifyAccountDto;
import com.zerowhisper.secondtask.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(
            AuthenticationService authenticationService) {
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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginAttributeInputDto log) {
        try {
            return new ResponseEntity<>(authenticationService
                    .login(log.getEmail(), log.getPassword()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<String> refreshToken(@RequestBody RefreshTokenRequestBody request) {

        // TODO: Put this in authenticationService
        try {
            return ResponseEntity.ok(authenticationService
                    .createAccessTokenFromRefreshToken(request.refreshToken));
        } catch (Exception r) {
            return new ResponseEntity<>(r.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logOut(@RequestHeader("Authorization") String token) {
        try {
            authenticationService.logOut(token);
            return new ResponseEntity<>("Logged out successful.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

}

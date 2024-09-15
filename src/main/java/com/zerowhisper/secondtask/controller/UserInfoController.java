package com.zerowhisper.secondtask.controller;

import com.zerowhisper.secondtask.service.UserAccountService;
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

    private final UserAccountService userAccountService;

    @Autowired
    public UserInfoController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {

        try {
            // Remove the "Bearer " prefix from the token
            return new ResponseEntity<>(userAccountService.getUserInfo(token), HttpStatus.OK);
        } catch (IllegalStateException i) {
            return new ResponseEntity<>(i.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException r) {
            return new ResponseEntity<>(
                    r.getMessage(),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }
    }

    //    @PostAuthorize()
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsersInfo(@RequestHeader("Authorization") String token) {
        try {
            return new ResponseEntity<>(userAccountService.getAllUsersInfo(token), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

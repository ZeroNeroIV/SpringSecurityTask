package com.zerowhisper.secondtask.dto;

import lombok.Data;

@Data
public class LoginAttributeOutputDto {
    private String username;
    private String email;
    private String accessToken;
    private String refreshToken;
}

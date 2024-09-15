package com.zerowhisper.secondtask.dto;

import lombok.Data;

@Data
public class LoginAttributeOutputDto {
    public String username;
    public String email;
    public String accessToken;
    public String refreshToken;
}

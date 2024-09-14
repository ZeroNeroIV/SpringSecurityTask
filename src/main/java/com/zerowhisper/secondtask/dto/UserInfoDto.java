package com.zerowhisper.secondtask.dto;

public class UserInfoDto {
    public Long id;
    public String email;
    public String username;
    public Boolean enabled;

    public UserInfoDto() {
    }

    public UserInfoDto(Long id, String email, String username, Boolean enabled) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.enabled = enabled;
    }
}

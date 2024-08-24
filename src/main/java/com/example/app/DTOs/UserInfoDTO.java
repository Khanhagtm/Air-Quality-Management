package com.example.app.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfoDTO {
    @JsonProperty("UserName")
    private String userName;
    @JsonProperty("UserEmail")
    private String userEmail;
    @JsonProperty("UserPassword")
    private String userPassword;
    @JsonProperty("COThreshold")
    private int COThreshold;
    @JsonProperty("GasThreshold")
    private int gasThreshold;
}

package com.example.demo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
package com.mobile.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInReq {
    @NotBlank
    String studentId;

    @NotBlank
    String password;
}

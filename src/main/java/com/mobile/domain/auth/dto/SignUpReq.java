package com.mobile.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignUpReq {
    @NotBlank
    @Size(min = 2, max = 30)
    String nickname;

    @NotBlank
    String studentId;

    @NotBlank
    String password;
}

package com.mobile.domain.auth.dto;

import com.mobile.domain.auth.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignInRes {
    String accessToken;
    String studentId;
    RoleType role;
}

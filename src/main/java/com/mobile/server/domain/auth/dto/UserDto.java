package com.mobile.server.domain.auth.dto;

import com.mobile.server.domain.auth.entity.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private RoleType role;
}

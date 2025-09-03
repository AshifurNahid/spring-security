package com.nahid.userservice.dto;

import com.nahid.userservice.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Role role;
    private String message;
    private String email;
}
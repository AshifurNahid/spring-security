package com.nahid.userservice.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Table
@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @Email(message = "Email should be valid")
    private String email;
    @NotBlank
    private String password;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number should be 11 digits")
    private String phone;
    @NotBlank
    private String address;

    private boolean active;
    @NotBlank
    private String role;


}

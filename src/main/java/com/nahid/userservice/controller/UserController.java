package com.nahid.userservice.controller;


import com.nahid.userservice.entity.User;
import com.nahid.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser (@RequestBody @Valid User user) {

        return ResponseEntity.ok("User registered successfully");
    }




}

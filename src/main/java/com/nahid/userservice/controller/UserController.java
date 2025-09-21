package com.nahid.userservice.controller;

import com.nahid.userservice.dto.response.ApiResponse;
import com.nahid.userservice.dto.response.UserResponse;
import com.nahid.userservice.service.UserService;
import com.nahid.userservice.util.helper.ApiResponseUtil;
import com.nahid.userservice.util.contant.ApiResponseConstant;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse userResponse = userService.getMe();
        return ApiResponseUtil.success(userResponse, ApiResponseConstant.USER_PROFILE_FETCHED);
    }
}
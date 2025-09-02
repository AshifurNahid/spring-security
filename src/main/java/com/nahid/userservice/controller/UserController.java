package com.nahid.userservice.controller;


import com.nahid.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser (@RequestBody @Valid User user) {
//
//        return ResponseEntity.ok(userService.registerUser(user));
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> loginUser ( @RequestBody @Valid LoginDto dto) {
//        return ResponseEntity.ok("Login successful");
//    }


}

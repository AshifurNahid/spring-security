package com.nahid.userservice.service;


import com.nahid.userservice.entity.User;
import com.nahid.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;


    public User registerUser(User user) {
        user.setActive(true);
        user.setRole("USER");
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already in use");
        }
        userRepository.save(user);
        return user;
    }
}

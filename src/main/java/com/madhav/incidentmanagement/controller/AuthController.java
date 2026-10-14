
        package com.madhav.incidentmanagement.controller;

import com.madhav.incidentmanagement.entity.User;
import com.madhav.incidentmanagement.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public record RegisterRequest(String username, String password) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(
                request.username(),
                request.password()
        );

        return "User registered successfully: " + user.getUsername();
    }
}


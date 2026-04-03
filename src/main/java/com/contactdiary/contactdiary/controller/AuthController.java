package com.contactdiary.contactdiary.controller;


import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        try {
            service.register(user);
            return "Registered Successfully!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/login")
    public Object login(@RequestBody User user) {
        Optional<User> u = service.login(user.getEmail(), user.getPassword());
        return u.orElse(null);
    }
}
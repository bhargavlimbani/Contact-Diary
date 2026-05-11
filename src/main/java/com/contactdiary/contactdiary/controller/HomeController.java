package com.contactdiary.contactdiary.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class HomeController {

    @GetMapping
    public Map<String, String> home() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Welcome to the Contact Diary API");
        response.put("status", "running");
        response.put("version", "4.0.5");
        response.put("endpoints", "/api/auth, /api/contacts");
        return response;
    }
}

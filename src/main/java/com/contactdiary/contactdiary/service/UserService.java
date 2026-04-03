package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User register(User user) {

        // check if email already exists
        if(repo.findByEmail(user.getEmail()).isPresent()){
            throw new RuntimeException("Email already registered!");
        }

        return repo.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> user = repo.findByEmail(email);

        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }
}
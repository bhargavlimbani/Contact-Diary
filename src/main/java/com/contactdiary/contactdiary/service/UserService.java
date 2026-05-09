package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {

        // check if email already exists
        if(repo.findByEmail(user.getEmail()).isPresent()){
            throw new RuntimeException("Email already registered! please login. ");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> user = repo.findByEmail(email);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        User existingUser = user.get();
        String storedPassword = existingUser.getPassword();

        if (storedPassword != null && storedPassword.startsWith("$2") && passwordEncoder.matches(password, storedPassword)) {
            return Optional.of(existingUser);
        }

        if (storedPassword != null && storedPassword.equals(password)) {
            existingUser.setPassword(passwordEncoder.encode(password));
            return Optional.of(repo.save(existingUser));
        }

        return Optional.empty();
    }
}
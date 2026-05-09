package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository repo = mock(UserRepository.class);
    private final UserService service = new UserService(repo, new BCryptPasswordEncoder());

    @Test
    void registerRejectsDuplicateEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        when(repo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.register(user));

        assertEquals("Email already registered! please login. ", exception.getMessage());
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void loginAcceptsMatchingPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(encoder.encode("secret"));

        when(repo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = service.login("test@example.com", "secret");

        assertTrue(result.isPresent());
    }
}

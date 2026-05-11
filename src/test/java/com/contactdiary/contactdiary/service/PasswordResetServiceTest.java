package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.PasswordResetToken;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.PasswordResetTokenRepository;
import com.contactdiary.contactdiary.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordResetService service = new PasswordResetService(
            userRepository,
            tokenRepository,
            passwordEncoder,
            mailSender
    );

    @Test
    void sendResetLinkCreatesTokenAndSendsEmail() {
        User user = new User();
        user.setName("Bhargav");
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        service.sendResetLink("test@example.com", "http://localhost:8083");

        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        User user = new User();
        user.setPassword("old-password");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired-token");
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByTokenAndUsedFalse("expired-token")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class, () -> service.resetPassword("expired-token", "newpass"));
    }

    @Test
    void resetPasswordWithOtpMarksTokenUsed() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("old-password");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("token");
        token.setOtp("123456");
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserAndUsedFalseOrderByIdDesc(user)).thenReturn(Optional.of(token));

        service.resetPasswordWithOtp("test@example.com", "123456", "newpass");

        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }
}

package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.PendingRegistration;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.PendingRegistrationRepository;
import com.contactdiary.contactdiary.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationOtpServiceTest {

    private final PendingRegistrationRepository pendingRegistrationRepository = mock(PendingRegistrationRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final RegistrationOtpService service = new RegistrationOtpService(
            pendingRegistrationRepository,
            userRepository,
            new BCryptPasswordEncoder(),
            mailSender
    );

    @Test
    void startRegistrationSendsOtp() {
        User user = new User();
        user.setName("Bhargav");
        user.setEmail("test@example.com");
        user.setPassword("secret");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        service.startRegistration(user);

        verify(pendingRegistrationRepository).deleteByEmail("test@example.com");
        verify(pendingRegistrationRepository).save(any(PendingRegistration.class));
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void verifyRegistrationCreatesRealUser() {
        PendingRegistration pendingRegistration = new PendingRegistration();
        pendingRegistration.setName("Bhargav");
        pendingRegistration.setEmail("test@example.com");
        pendingRegistration.setEncodedPassword("encoded");
        pendingRegistration.setOtp("123456");
        pendingRegistration.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(pendingRegistrationRepository.findByEmailAndOtp("test@example.com", "123456"))
                .thenReturn(Optional.of(pendingRegistration));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = service.verifyRegistration("test@example.com", "123456");

        assertEquals("Bhargav", savedUser.getName());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encoded", savedUser.getPassword());
        verify(pendingRegistrationRepository).delete(pendingRegistration);
    }

    @Test
    void verifyRegistrationRejectsExpiredOtp() {
        PendingRegistration pendingRegistration = new PendingRegistration();
        pendingRegistration.setEmail("test@example.com");
        pendingRegistration.setOtp("123456");
        pendingRegistration.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(pendingRegistrationRepository.findByEmailAndOtp("test@example.com", "123456"))
                .thenReturn(Optional.of(pendingRegistration));

        assertThrows(RuntimeException.class, () -> service.verifyRegistration("test@example.com", "123456"));
    }
}

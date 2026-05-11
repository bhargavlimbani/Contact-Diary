package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.PendingRegistration;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.PendingRegistrationRepository;
import com.contactdiary.contactdiary.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class RegistrationOtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final Random random = new Random();
    private String lastGeneratedOtp;

    public RegistrationOtpService(
            PendingRegistrationRepository pendingRegistrationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender
    ) {
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public void startRegistration(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered! please login. ");
        }

        pendingRegistrationRepository.deleteByEmail(user.getEmail());

        PendingRegistration pendingRegistration = new PendingRegistration();
        pendingRegistration.setName(user.getName());
        pendingRegistration.setEmail(user.getEmail());
        pendingRegistration.setEncodedPassword(passwordEncoder.encode(user.getPassword()));
        pendingRegistration.setOtp(generateOtp());
        lastGeneratedOtp = pendingRegistration.getOtp();
        pendingRegistration.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        pendingRegistrationRepository.save(pendingRegistration);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Contact Diary Registration OTP");
        message.setText(
                "Hello " + user.getName() + ",\n\n" +
                "Your Contact Diary registration OTP is: " + pendingRegistration.getOtp() + "\n\n" +
                "This OTP will expire in " + OTP_EXPIRY_MINUTES + " minutes."
        );
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            System.out.println("Registration OTP email failed for " + user.getEmail());
            System.out.println("Registration OTP: " + pendingRegistration.getOtp());
            throw ex;
        }
    }

    public String getLastGeneratedOtp() {
        return lastGeneratedOtp;
    }

    @Transactional
    public User verifyRegistration(String email, String otp) {
        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid registration OTP."));

        if (pendingRegistration.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Registration OTP has expired.");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            pendingRegistrationRepository.delete(pendingRegistration);
            throw new RuntimeException("Email already registered! please login. ");
        }

        User user = new User();
        user.setName(pendingRegistration.getName());
        user.setEmail(pendingRegistration.getEmail());
        user.setPassword(pendingRegistration.getEncodedPassword());
        User savedUser = userRepository.save(user);
        pendingRegistrationRepository.delete(pendingRegistration);
        return savedUser;
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}

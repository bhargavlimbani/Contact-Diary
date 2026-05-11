package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.PasswordResetToken;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.PasswordResetTokenRepository;
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
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final Random random = new Random();
    private String lastGeneratedOtp;
    private String lastGeneratedResetLink;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public String sendResetLink(String email, String appBaseUrl) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return "If an account exists for this email, check your mail for OTP and reset link.";
        }

        User user = userOptional.get();
        tokenRepository.deleteByUser(user);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setOtp(generateOtp());
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        String resetLink = appBaseUrl + "/app.html?resetToken=" + resetToken.getToken();
        lastGeneratedOtp = resetToken.getOtp();
        lastGeneratedResetLink = resetLink;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Contact Diary Password Reset");
        message.setText(
                "Hello " + user.getName() + ",\n\n" +
                "Use this link to reset your Contact Diary password:\n" +
                resetLink + "\n\n" +
                "Or use this OTP: " + resetToken.getOtp() + "\n\n" +
                "This link will expire in " + TOKEN_EXPIRY_MINUTES + " minutes.\n\n" +
                "If you did not request this, you can ignore this email."
        );
        try {
            mailSender.send(message);
            return "Check your mail for OTP and reset link.";
        } catch (MailException ex) {
            System.out.println("Forgot-password email failed for " + user.getEmail());
            System.out.println("Reset OTP: " + resetToken.getOtp());
            System.out.println("Reset link: " + resetLink);
            return "Email send failed. Use OTP: " + resetToken.getOtp() + " or reset link: " + resetLink;
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link."));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset link has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    @Transactional
    public void resetPasswordWithOtp(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP."));

        PasswordResetToken resetToken = tokenRepository.findTopByUserAndUsedFalseOrderByIdDesc(user)
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP."));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset OTP has expired.");
        }

        if (!resetToken.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid email or OTP.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    public String getLastGeneratedOtp() {
        return lastGeneratedOtp;
    }

    public String getLastGeneratedResetLink() {
        return lastGeneratedResetLink;
    }
}

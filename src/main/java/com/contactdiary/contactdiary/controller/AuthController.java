package com.contactdiary.contactdiary.controller;


import com.contactdiary.contactdiary.dto.ForgotPasswordRequest;
import com.contactdiary.contactdiary.dto.OtpVerificationRequest;
import com.contactdiary.contactdiary.dto.ResetPasswordRequest;
import com.contactdiary.contactdiary.dto.ResetPasswordWithOtpRequest;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.service.PasswordResetService;
import com.contactdiary.contactdiary.service.RegistrationOtpService;
import com.contactdiary.contactdiary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserService service;
    private final PasswordResetService passwordResetService;
    private final RegistrationOtpService registrationOtpService;

    public AuthController(
            UserService service,
            PasswordResetService passwordResetService,
            RegistrationOtpService registrationOtpService
    ) {
        this.service = service;
        this.passwordResetService = passwordResetService;
        this.registrationOtpService = registrationOtpService;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody User user) {
        try {
            registrationOtpService.startRegistration(user);
            return "Registration OTP sent to your email. Please verify to complete signup.";
        } catch (Exception e) {
            String fallbackOtp = registrationOtpService.getLastGeneratedOtp();
            if (fallbackOtp != null && !fallbackOtp.isBlank()) {
                return "Email send failed. Use this OTP for now: " + fallbackOtp;
            }
            return e.getMessage();
        }
    }

    @PostMapping("/verify-registration")
    public Object verifyRegistration(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            return registrationOtpService.verifyRegistration(request.getEmail(), request.getOtp());
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/login")
    public Object login(@RequestBody User user) {
        Optional<User> u = service.login(user.getEmail(), user.getPassword());
        return u.orElse(null);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        try {
            String appBaseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName() +
                    (httpRequest.getServerPort() == 80 || httpRequest.getServerPort() == 443 ? "" : ":" + httpRequest.getServerPort());
            return passwordResetService.sendResetLink(request.getEmail(), appBaseUrl);
        } catch (Exception e) {
            String fallbackOtp = passwordResetService.getLastGeneratedOtp();
            String fallbackLink = passwordResetService.getLastGeneratedResetLink();
            if (fallbackOtp != null && fallbackLink != null) {
                return "Email send failed. Use OTP: " + fallbackOtp + " or reset link: " + fallbackLink;
            }
            return "Unable to send reset email right now. " + e.getMessage();
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getPassword());
            return "Password reset successful. Please login.";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/reset-password-otp")
    public String resetPasswordWithOtp(@Valid @RequestBody ResetPasswordWithOtpRequest request) {
        try {
            passwordResetService.resetPasswordWithOtp(request.getEmail(), request.getOtp(), request.getPassword());
            return "Password reset successful. Please login.";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}

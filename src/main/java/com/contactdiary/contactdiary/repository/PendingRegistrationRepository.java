package com.contactdiary.contactdiary.repository;

import com.contactdiary.contactdiary.model.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByEmail(String email);

    Optional<PendingRegistration> findByEmailAndOtp(String email, String otp);

    void deleteByEmail(String email);
}

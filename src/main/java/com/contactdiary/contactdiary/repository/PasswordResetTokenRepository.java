package com.contactdiary.contactdiary.repository;

import com.contactdiary.contactdiary.model.PasswordResetToken;
import com.contactdiary.contactdiary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    Optional<PasswordResetToken> findTopByUserAndUsedFalseOrderByIdDesc(User user);

    void deleteByUser(User user);
}

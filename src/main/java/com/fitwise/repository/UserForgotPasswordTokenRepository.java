package com.fitwise.repository;

import com.fitwise.entity.UserForgotPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The Interface UserForgotPasswordTokenRepository.
 */
@Repository
public interface UserForgotPasswordTokenRepository extends JpaRepository<UserForgotPasswordToken, Long> {
    
    /**
     * Find by reset token.
     *
     * @param token the tokenRE
     * @return the user forgot password token
     */
    UserForgotPasswordToken findByResetToken(String token);
}

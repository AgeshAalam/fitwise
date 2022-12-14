package com.fitwise.repository;

import com.fitwise.entity.User;
import com.fitwise.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {
    List<UserOtp> findByEmailOrderByOtpId(String email);

    List<UserOtp> findByEmailAndOtpOrderByOtpId(String email, int otp);

    UserOtp findFirstByEmailAndOtpOrderByUpdatedOnDesc(String email, int otp);

}

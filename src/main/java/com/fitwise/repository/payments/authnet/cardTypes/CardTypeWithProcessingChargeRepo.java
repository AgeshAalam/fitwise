package com.fitwise.repository.payments.authnet.cardTypes;

import com.fitwise.entity.payments.authNet.cardTypes.CardTypeWithProcessingCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardTypeWithProcessingChargeRepo extends JpaRepository<CardTypeWithProcessingCharge, Long> {
    CardTypeWithProcessingCharge findByCardType(String cardType);
}

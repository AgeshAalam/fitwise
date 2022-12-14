package com.fitwise.entity.payments.stripe.settings;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class PayoutModeSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long payoutSettingsId;

    private boolean isManual = false;
}

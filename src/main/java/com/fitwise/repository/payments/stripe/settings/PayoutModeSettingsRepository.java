package com.fitwise.repository.payments.stripe.settings;

import com.fitwise.entity.payments.stripe.settings.PayoutModeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayoutModeSettingsRepository extends JpaRepository<PayoutModeSettings, Long> {
    PayoutModeSettings findByIsManualFalse();

    PayoutModeSettings findTop1ByPayoutSettingsId(Long payoutSettingsId);
}

package com.fitwise.listeners.block.packaging;

import com.fitwise.utils.mail.AsyncMailerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * Created by Vignesh G on 29/01/21
 */
@Component
@RequiredArgsConstructor
public class MailPackageBlockListener implements PackageBlockListener {

    private final AsyncMailerUtil asyncMailerUtil;

    @Override
    public void packageBlocked(PackageBlockEvent packageBlockEvent) {
        asyncMailerUtil.triggerPackageBlockMail(packageBlockEvent);
    }

    @Override
    public void packageUnblocked(PackageBlockEvent packageBlockEvent) {
        asyncMailerUtil.triggerPackageUnblockMail(packageBlockEvent);
    }
}

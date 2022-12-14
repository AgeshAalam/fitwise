package com.fitwise.listeners.block.packaging;

import com.fitwise.entity.packaging.SubscriptionPackage;
import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh G on 29/01/21
 */
@Getter
@Setter
public class PackageBlockEvent {

    private Long subscriptionPackageId;

    private SubscriptionPackage subscriptionPackage;

    private String blockType;

}

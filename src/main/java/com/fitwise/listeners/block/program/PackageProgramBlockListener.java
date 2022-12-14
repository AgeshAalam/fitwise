package com.fitwise.listeners.block.program;

import com.fitwise.block.service.AdminBlockedService;
import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.packaging.BlockedPackage;
import com.fitwise.entity.packaging.PackageProgramMapping;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.packaging.BlockedPackageRepository;
import com.fitwise.repository.packaging.PackageProgramMappingRepository;
import com.fitwise.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 29/01/21
 */
@Component
public class PackageProgramBlockListener implements ProgramBlockListener {

    @Autowired
    private PackageProgramMappingRepository packageProgramMappingRepository;
    @Autowired
    private AdminBlockedService adminBlockedService;
    @Autowired
    private BlockedPackageRepository blockedPackageRepository;
    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public void programBlocked(ProgramBlockEvent programBlockEvent) {

        //BLocking associated Packages of the program
        List<PackageProgramMapping> packageProgramMappingList = packageProgramMappingRepository.findByProgramAndSubscriptionPackageStatus(programBlockEvent.getProgram(), KeyConstants.KEY_PUBLISH);
        if (!packageProgramMappingList.isEmpty()) {

            List<Long> subscriptionPackageIdList = packageProgramMappingList.stream()
                    .map(packageProgramMapping -> packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId())
                    .distinct()
                    .collect(Collectors.toList());

            for (Long subscriptionPackageId : subscriptionPackageIdList) {
                adminBlockedService.blockPackage(subscriptionPackageId, programBlockEvent.getBlockType());
            }
        }
    }

    @Override
    public void programUnblocked(ProgramBlockEvent programBlockEvent) {

        //unblocking associated Packages of the program
        List<PackageProgramMapping> packageProgramMappingList = packageProgramMappingRepository.findByProgram(programBlockEvent.getProgram());
        if (!packageProgramMappingList.isEmpty()) {

            List<Long> subscriptionPackageIdList = packageProgramMappingList.stream()
                    .map(packageProgramMapping -> packageProgramMapping.getSubscriptionPackage().getSubscriptionPackageId())
                    .distinct()
                    .collect(Collectors.toList());


            List<BlockedPackage> blockedPackageList = blockedPackageRepository.findBySubscriptionPackageSubscriptionPackageIdInAndBlockType(subscriptionPackageIdList, programBlockEvent.getBlockType());

            //Getting blocked flagged video programs
            subscriptionPackageIdList = blockedPackageList.stream()
                    .map(blockedProgram -> blockedProgram.getSubscriptionPackage().getSubscriptionPackageId())
                    .distinct()
                    .collect(Collectors.toList());

            long paidSubscriptions = subscriptionService.getOverallActiveSubscriptionCountForPackagesList(subscriptionPackageIdList);
            if (paidSubscriptions > 0) {
                throw new ApplicationException(Constants.CONFLICT, MessageConstants.MSG_CANT_UNBLOCK_SUBSCRIBED_PACKAGE, MessageConstants.ERROR);
            }

            for (Long subscriptionPackageId : subscriptionPackageIdList) {
                adminBlockedService.unblockPackage(subscriptionPackageId, programBlockEvent.getBlockType());
            }


        }

    }
}

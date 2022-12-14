package com.fitwise.listeners.invokers.admin;

import com.fitwise.constants.Constants;
import com.fitwise.listeners.block.packaging.MailPackageBlockListener;
import com.fitwise.listeners.block.packaging.PackageBlockEvent;
import com.fitwise.listeners.block.packaging.PackageBlockListener;
import com.fitwise.listeners.block.packaging.SubscriptionPackageBlockListener;
import com.fitwise.listeners.block.program.MailProgramBlockListener;
import com.fitwise.listeners.block.program.PackageProgramBlockListener;
import com.fitwise.listeners.block.program.ProgramBlockEvent;
import com.fitwise.listeners.block.program.ProgramBlockListener;
import com.fitwise.listeners.block.program.SubscriptionProgramBlockListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Vignesh G on 29/06/20
 */
@Component
public class BlockListeners {

    @Autowired
    SubscriptionProgramBlockListener subscriptionProgramBlockListener;
    @Autowired
    MailProgramBlockListener mailProgramBlockListener;
    @Autowired
    PackageProgramBlockListener packageProgramBlockListener;
    @Autowired
    SubscriptionPackageBlockListener subscriptionPackageBlockListener;
    @Autowired
    MailPackageBlockListener mailPackageBlockListener;


    /**
     * Load listener classes
     * @return
     */
    private List<ProgramBlockListener> loadProgramBlockListeners() {
        List<ProgramBlockListener> programBlockListeners = new ArrayList<>();
        programBlockListeners.add(subscriptionProgramBlockListener);
        programBlockListeners.add(mailProgramBlockListener);
        programBlockListeners.add(packageProgramBlockListener);
        return programBlockListeners;
    }

    /**
     * Invoke listeners for program block/unblock
     * @param operation
     * @param programBlockEvent
     */
    public void invokeProgramBlockListeners(int operation, ProgramBlockEvent programBlockEvent) {
        List<ProgramBlockListener> programBlockListeners = loadProgramBlockListeners();

        if (operation == Constants.LISTENER_BLOCK_OPERATION) {
            programBlockListeners.forEach(programBlockListener -> programBlockListener.programBlocked(programBlockEvent));
        } else if (operation == Constants.LISTENER_UNBLOCK_OPERATION) {
            programBlockListeners.forEach(programBlockListener -> programBlockListener.programUnblocked(programBlockEvent));
        }

    }

    /**
     * Load listener classes
     * @return
     */
    private List<PackageBlockListener> loadPackageBlockListeners() {
        List<PackageBlockListener> packageBlockListeners = new ArrayList<>();
        packageBlockListeners.add(subscriptionPackageBlockListener);
        packageBlockListeners.add(mailPackageBlockListener);
        return packageBlockListeners;
    }

    /**
     * Invoke listeners for SubscriptionPackage block/unblock
     * @param operation
     * @param packageBlockEvent
     */
    public void invokePackageBlockListeners(int operation, PackageBlockEvent packageBlockEvent) {
        List<PackageBlockListener> packageBlockListeners = loadPackageBlockListeners();

        if (operation == Constants.LISTENER_BLOCK_OPERATION) {
            packageBlockListeners.forEach(packageBlockListener -> packageBlockListener.packageBlocked(packageBlockEvent));
        } else if (operation == Constants.LISTENER_UNBLOCK_OPERATION) {
            packageBlockListeners.forEach(packageBlockListener -> packageBlockListener.packageUnblocked(packageBlockEvent));
        }

    }

}

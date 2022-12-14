package com.fitwise.listeners.block.packaging;

/*
 * Created by Vignesh G on 29/01/21
 */
public interface PackageBlockListener {

    public void packageBlocked(PackageBlockEvent packageBlockEvent);

    public void packageUnblocked(PackageBlockEvent packageBlockEvent);

}

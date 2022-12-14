package com.fitwise.model.packaging;

import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 22/09/20
 */
@Data
public class AccessModel {

    private boolean isRestrictedAccess;

    private List<Long> clientIdList;

    private List<String> externalClientEmailList;

}

package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdditionalResourcesListResponseView {

    private long additionalResourcesCount;

    private List<AdditionalResourcesResponseView> additionalResources;
}

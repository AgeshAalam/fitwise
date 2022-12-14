package com.fitwise.view.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EquipmentResponseView {

    private Long equipmentId;

    private String equipmentName;

    private Long programCount;

    private Long exerciseCount;

    private boolean isUsedInExercises;

    private boolean isPrimary;

    private Date createdDate;

    private String createdDateFormatted;


}

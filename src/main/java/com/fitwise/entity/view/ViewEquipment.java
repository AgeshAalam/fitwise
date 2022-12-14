package com.fitwise.entity.view;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "view_equipment")
public class ViewEquipment {

    @Id
    @Column(name = "equipment_id")
    private Long equipmentId;

    private String equipmentName;

    private Date createdDate;

    private boolean isPrimary;

    private boolean isUsed;

    private Long programCount;

    private Long exerciseCount;

}

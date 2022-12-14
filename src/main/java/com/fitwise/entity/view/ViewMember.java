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
@Table(name = "view_member")
public class ViewMember {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private String imageUrl;

    private String name;

    private Long activePackageSubscriptions;

    private Long activeProgramSubscriptions;

    private Long completedPrograms;

    private Date lastUserAccess;

    private Double programAmtSpent;

    private Double packageAmtSpent;

    private Double totalSpent;

    private Boolean blocked;

    private Date onboardedOn;

    private String email;

    private String contactNumber;

}

package com.fitwise.entity.view;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "view_admin_programs")
public class ViewAdminPrograms {

    @Id
    @Column(name = "program_id")
    private Long programId;

    private String programName;
    private String instructorFirstName;
    private String instructorLastName;
    private String instructorFullName;

    private String programPublishStatus;

    private Long programTypeId;
    private BigDecimal rating;

    private Date createdDate;

    private Date modifiedDate;

    private Boolean isBlocked;

    private long activeSubscriptionCount;
}

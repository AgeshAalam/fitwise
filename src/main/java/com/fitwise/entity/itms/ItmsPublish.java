package com.fitwise.entity.itms;

import com.fitwise.entity.AuditingEntity;
import com.fitwise.entity.Programs;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ItmsPublish extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long itmsPublishId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Programs program;

    private Boolean needUpdate;

    private Boolean isNew;

    private String status;

}

package com.fitwise.entity.payments.authNet;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class AuthNetWebHookLogger {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreationTimestamp
    private Date receivedTime;

    @Column(columnDefinition = "LONGTEXT")
    private String webHookNotification;
}

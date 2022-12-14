package com.fitwise.entity.payments.authNet;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Class that holds the customer profile id of Auth.net mapped towards a user id
 */
@Entity
@Getter
@Setter
public class AuthNetCustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Changing it to Identity causes detached entity error
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    private String authNetCustomerProfileId;
}

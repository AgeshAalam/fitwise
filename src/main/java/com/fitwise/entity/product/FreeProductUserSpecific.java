package com.fitwise.entity.product;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class FreeProductUserSpecific {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long freeProductUserSpecificId;

	@ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE })
    @JoinColumn(name = "free_product_id", nullable = false)
    private FreeProduct freeProduct;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "free_access_program_id")
    private FreeAccessProgram freeAccessProgram;
    
    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "free_access_package_id")
    private FreeAccessPackages freeAccessSubscriptionPackages;
}

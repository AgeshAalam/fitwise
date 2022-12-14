package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class ApplePaymentDAO {

    private Integer orderManagementId;

    private Date appleExpiryDate;

    private Date createdDate;
}

package com.fitwise.specifications.jpa.dao;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class StripePaymentDAO {

    private Integer orderManagementId;

    private String stripeTransactionStatus;

    private Date modifiedDate;
}

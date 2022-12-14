package com.fitwise.view.payment.stripe.admin;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh.G on 27/04/21
 */
@Getter
@Setter
public class FailedTopUpPayout {

    private String instructorName;
    private String instructorShare;
    private String dueDate;
    private String failureMessage;

}

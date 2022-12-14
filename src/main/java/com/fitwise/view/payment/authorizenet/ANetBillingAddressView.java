package com.fitwise.view.payment.authorizenet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ANetBillingAddressView {
    protected String firstName;
    protected String lastName;
    protected String address;
    protected String city;
    protected String state;
    protected String zip;
}

package com.fitwise.constants;

public class StripeConstants {

    private StripeConstants() {
    }

    public static final String STRIPE_RESOURCE_MISSING = "resource_missing";
    public static final String STRIPE_TOP_UP_MANUAL_MODE_PROMPT = "You can only create a top-up if your account is on a manual payout schedule.";
    public static final Double STRIPE_MAXIMUM_PRICE = 999999.99;
    public static final Double STRIPE_MINIMUM_PRICE = 4.99;

    public static final String STRIPE_PROP_PAYMENT_METHOD = "payment_method";
    public static final String STRIPE_PROP_EMAIL = "email";
    public static final String STRIPE_PROP_CUSTOMER = "customer";
    public static final String STRIPE_PROP_FAILED = "failed";
    public static final String STRIPE_PROP_CURRENCY = "currency";
    public static final String STRIPE_PROP_DESTINATION = "destination";
    public static final String STRIPE_PROP_TRANSFER_GROUP = "transfer_group";
    public static final String STRIPE_PROP_CHARGE = "charge";

}

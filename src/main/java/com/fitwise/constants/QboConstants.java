package com.fitwise.constants;

/**
 * This class represents the constants for quick book online
 */
public class QboConstants {

    private QboConstants() {
    }

    /**
     * Constants for the Keys maintaining for redis
     */
    public static final String KEY_REALM_ID = "qboRealmId";
    public static final String KEY_ACCESS_TOKEN = "qboAccessToken";
    public static final String KEY_REFRESH_TOKEN = "qboRefreshToken";
    public static final String KEY_CSRF_TOKEN = "qboCsrfToken";
    public static final String KEY_AUTH_CODE = "qboAuthCode";

    /**
     * Constants for account names
     */
    public static final String ACCOUNT_INCOME_TRAINNR_ANDROID = "Trainnr revenue - Android";
    public static final String ACCOUNT_INCOME_TRAINNR_IOS = "Trainnr revenue - iOS";
    public static final String ACCOUNT_INCOME_TRAINNR_WEB = "Trainnr revenue - web";
    public static final String ACCOUNT_EXPENSE_INSTRUCTOR_PAYMENTS = "Trainnr instructor payments";
    public static final String ACCOUNT_EXPENSE_CREDIT_CARD_VARIABLE_PROCESSING = "Credit card processing fees (variable)";
    public static final String ACCOUNT_EXPENSE_CREDIT_CARD_FIXED_PROCESSING = "Credit card processing fees (fixed)";
    public static final String ACCOUNT_EXPENSE_APP_STORE_FEE = "Apple App Store Fees";
    public static final String ACCOUNT_DEFAULT_PAYABLE = "Accounts Payable (A/P)";
    public static final String ACCOUNT_EXPENSE_STRIPE = "Stripe processing fees";

    /**
     * Constants for vendor
     */
    public static final String VENDOR_VISA = "Visa International";
    public static final String VENDOR_APPLE = "Apple";
    public static final String VENDOR_STRIPE = "Stripe";

    /**
     * QBO entity creation status messages
     */
    public static final String MSG_ENTITY_CREATE_FAILURE = "Entity creation failed";
    public static final String MSG_EXPENSE_ACCOUNT_MISSING = "Expense account is not available.";
    public static final String MSG_INCOME_ACCOUNT_MISSING = "Income account is not available.";
    public static final String MSG_PROCESSING_ACCOUNT_MISSING = "Processing account is not available.";
    public static final String MSG_VENDOR_MISSING = "Vendor is not created yet.";
    public static final String MSG_VENDOR_NOT_AVAILABLE = "Vendor is not available.";
    public static final String MSG_PRODUCT_MISSING = "Product is not created yet.";
    public static final String MSG_PRODUCT_NOT_AVAILABLE = "Product is not available.";
    public static final String MSG_CUSTOMER_MISSING = "Customer is not created yet.";
    public static final String MSG_CUSTOMER_NOT_AVAILABLE = "Customer is not available.";
    public static final String MSG_INVOICE_NOT_AVAILABLE = "Invoice is not available.";
    public static final String MSG_PAYMENT_MISSING = "Payment is not created yet.";
    public static final String MSG_UPDATED = "Updated.";
    public static final String MSG_ANET_TRANSACTION_NOT_FOUND = "AuthNet transaction not found.";
    public static final String MSG_PRODUCT_CATEGORY_NOT_AVAILABLE = "Product Category not available";

    /**
     * Product category
     */
    public static final String QBO_CAT_SUBSCRIPTION_PACKAGE = "Subscription Package";
    public static final String QBO_CAT_TIER = "Tier";

    /**
     * Constant used to specify payout method in admin payout page
     */
    public static final String QBO_PAYPAL_PAYOUT_MODE = "QBO/PayPal";

}

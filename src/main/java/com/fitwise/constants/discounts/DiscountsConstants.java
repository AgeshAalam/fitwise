package com.fitwise.constants.discounts;

import org.springframework.stereotype.Component;

@Component
public class DiscountsConstants {

	public static final String CODE_USED="Used";
	public static final String CODE_UNUSED="Un Used";
	public static final String CODE_REMOVED="Removed";
	public static final String MODE_FREE="free";
	public static final String MODE_PAY_AS_YOU_GO="pay-as-you-go";
	public static final String DEFAULT_PERIOD="1";
	public static final String OFFER_ACTIVE="Active";
	public static final String OFFER_INACTIVE="In Active";
	public static final String PROGRAM_LEVEL="Program";
	public static final String INSTRUCTOR_LEVEL="Instructor";
	public static final String MSG_PRODUCT_IDENTIFIER_NULL_EMPTY = "Product Identifier Cannot be null or empty.";
	public static final String MSG_OFFER_IDENTIFIER_NULL_EMPTY = "Offer Identifier Cannot be null or empty.";
	public static final String MSG_PRODUCT_IDENTIFIER_INVALID = "Invalid Product Identifier.";
	public static final String MSG_OFFER_IDENTIFIER_INVALID = "Invalid Offer Identifier.";
	public static final String PAY_AS_YOU_GO_DURATION="1 month";
	public static final String NEW_DISCOUNT="Newly Added";
	public static final String UPDATE_DISCOUNT="Updated with Price Change";
	public static final String REMOVE_DISCOUNT="Discount Removed ";
	public static final String REMOVE_DISCOUNT_INACTIVE="Discount Removed (In Active Offer)";
	public static final String EXPIRED_DISCOUNT="Discount Removed (Expired Offer)";
	public static final String TYPE_INTRO="introductory";
}

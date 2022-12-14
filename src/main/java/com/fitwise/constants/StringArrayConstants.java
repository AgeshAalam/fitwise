package com.fitwise.constants;

import java.util.Arrays;
import java.util.List;

public class StringArrayConstants {

    private StringArrayConstants() {
    }

    public static final List<String> VIDEO_UPLOAD_ONCE_STATUS = Arrays.asList(VideoUploadStatus.COMPLETED, VideoUploadStatus.VIMEO_PROCESSING_FAILED, VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED);
    protected static final String[] EXPORT_PAYOUT_HEADERS = {"Instructor Name", "Instructor Payout Mode", "Instructor Share", "Due Date", "Status", "Paid Via", "Transaction ID", "Bill ID", "Platform Type", "Paid Date", "Transfer Error", "Subscription Type", StringConstants.EMAIL};
    protected static final String[] EXPORT_MEMBER_HEADERS = {StringConstants.MEMBER_NAME, "Blocked Status", "Total Spent", "Total Spent For Program", "Total Spent For Package", "Package Subscriptions", "Program Subscriptions", "Completed Programs", "Last Active", "Onboarded On", StringConstants.EMAIL, "Phone Number"};
    protected static final String[] EXPORT_INSTRUCTOR_HEADERS = {"Instructor Name", "Blocked Status", "Instructor Payout Mode", "Total Outstanding", "Packages Published", "Packages Subscriptions", "Programs Published", "Programs Subscriptions", "Total Exercises", "Onboarded On", "Last Active", StringConstants.EMAIL, "Phone Number", "Tier"};
    protected static final String[] EXPORT_FREE_ACCESS_PROGRAM_HEADERS = {StringConstants.MEMBER_NAME, StringConstants.EMAIL, "Onboarding Date", "Program Name", "Free Access Start Date", "Free Access End Date", "Last Active Date"};
    protected static final String[] EXPORT_FREE_ACCESS_PACKAGE_HEADERS = {StringConstants.MEMBER_NAME, StringConstants.EMAIL, "Onboarding Date", "Package Name", "Free Access Start Date", "Free Access End Date", "Last Active Date"};
    protected static final String[] EXPORT_INVITE_MEMBERS_HEADERS = {StringConstants.EMAIL, "First Name", "Last Name", "Status", "Invited On"};

    /**
     * Sort list
     */
    public static final List<String> SORT_ADMIN_INSTRUCTOR = Arrays.asList(SearchConstants.INSTRUCTOR_NAME, SearchConstants.UPCOMING_PAYMENT, SearchConstants.TOTAL_SUBSCRIPTION, SearchConstants.PUBLISHED_PROGRAM, SearchConstants.ONBOARDED_DATE, SearchConstants.TOTAL_EXERCISES, SearchConstants.PACKAGE_SUBSCRIPTION_COUNT, SearchConstants.PUBLISHED_PACKAGE_COUNT, SearchConstants.USER_LAST_ACCESS_DATE, SearchConstants.USER_EMAIL, SortConstants.SORT_BY_TIER_TYPE);
    public static final List<String> SORT_ADMIN_MEMBER = Arrays.asList(SearchConstants.MEMBER_NAME, SearchConstants.AMOUNT_SPENT, SearchConstants.TOTAL_SUBSCRIPTION, SearchConstants.COMPLETED_PROGRAM, SearchConstants.STATUS, SecurityFilterConstants.ROLE_INSTRUCTOR, SearchConstants.PACKAGE_SUBSCRIPTION_COUNT, SearchConstants.USER_LAST_ACCESS_DATE, SearchConstants.ONBOARDED_DATE, SearchConstants.USER_EMAIL);
}

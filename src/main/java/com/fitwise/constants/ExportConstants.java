package com.fitwise.constants;

import java.util.Arrays;
import java.util.List;

public class ExportConstants {

    private ExportConstants() {
    }

    //Filenames for the export file
    public static final String FILE_NAME_PAYOUT = "payouts.csv";
    public static final String FILE_NAME_MEMBER = "members.csv";
    public static final String FILE_NAME_INSTRUCTOR = "instructors.csv";
    public static final String FILE_NAME_FREE_ACCESS_PROGRAM = "free_access_program.csv";
    public static final String FILE_NAME_FREE_ACCESS_PACKAGE = "free_access_package.csv";
    public static final String FILE_NAME_INVITE_MEMBERS = "invite_members.csv";


    //Headers for the export file
    public static final List<String> EXPORT_HEADER_PAYOUT = Arrays.asList(StringArrayConstants.EXPORT_PAYOUT_HEADERS);
    public static final List<String> EXPORT_HEADER_MEMBER = Arrays.asList(StringArrayConstants.EXPORT_MEMBER_HEADERS);
    public static final List<String> EXPORT_HEADER_INSTRUCTOR = Arrays.asList(StringArrayConstants.EXPORT_INSTRUCTOR_HEADERS);
    public static final List<String> EXPORT_FREE_ACCESS_HEADER_PROGRAM = Arrays.asList(StringArrayConstants.EXPORT_FREE_ACCESS_PROGRAM_HEADERS);
    public static final List<String> EXPORT_FREE_ACCESS_HEADER_PACKAGE = Arrays.asList(StringArrayConstants.EXPORT_FREE_ACCESS_PACKAGE_HEADERS);
    public static final List<String> EXPORT_INVITE_MEMBERS = Arrays.asList(StringArrayConstants.EXPORT_INVITE_MEMBERS_HEADERS);


    // Key Name
    public static final String KEY_NAME_FREE_ACCESS_PROGRAM = "program";
    public static final String KEY_NAME_FREE_ACCESS_PACKAGE = "package";


}

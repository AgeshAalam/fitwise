package com.fitwise.constants.payments.appleiap;

public final class InAppPurchaseConstants {
	private InAppPurchaseConstants() {
		
	}

	public static final String VENDOR_ID="Trainnr_Fitness_User" ;
	public static final String PROGRAM_TYPE="auto-renewable" ;
	public static final String ONE_MONTH="1 Month" ;
	public static final String METADATA_XML="metadata.xml" ;
	public static final String US="US" ;	
	public static final String SCHEDULAR="1 6 */4 * * * " ; //every 4 hour 6 minute
	public static final String SCHEDULAR_RENEWAL="0 0/1 * * * ?" ; //every 1 minute
	public static final String PAYMENT_SETTLEMENT="1 30 11 * * *"; //11.30 AM server time UTC - everyday
	
	public static final String IMG_ERR="Review Screenshot not available for Program." ;
	public static final String VEN_ERR="Vendor Information is not available in DB :".concat(VENDOR_ID) ;
	public static final String PGM_ERR="There is/are no Published/Unpublished programs to upload into Appstore." ;
	public static final String XML_ERR="Exception occured while generating XML." ;
	public static final String DWN_ERR="Exception occured while downloading Screenshot Image from S3." ;
	public static final String SGP_ERR="Exception occured while deriving Subscription Group Name for Program." ;
	public static final String FLE_ERR="Unable to determine files.";
	public static final String ZIP_ERR="Unable to determine files to be packed in Jobdefinition." ;
	public static final String OK_MSG="Meta Data XML generated Successfully along with Screenshots.";
	public static final String ERR_MSG="Upload failure.";
	
	public static final String ZIP_EXTN=".zip";
	public static final String JOB_PUBLISH="jobPublishIAP-";
	public static final String JOB_UNPUBLISH="jobUnPublishIAP-";
	public static final String DIR_PREFIX="FitwiseJob-";
	
}

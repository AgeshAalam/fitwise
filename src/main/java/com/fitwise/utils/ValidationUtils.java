package com.fitwise.utils;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.Regex;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.exception.ApplicationException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;
import net.authorize.Environment;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * The Class ValidationUtils.
 */
@Slf4j
public class ValidationUtils {

    private ValidationUtils(){
    }

    /**
     * months[] - Contains the list of months.
     */
    public static final String[] months = {KeyConstants.KEY_JAN, KeyConstants.KEY_FEB, KeyConstants.KEY_MAR,
            KeyConstants.KEY_APR, KeyConstants.KEY_MAY, KeyConstants.KEY_JUNE, KeyConstants.KEY_JULY,
            KeyConstants.KEY_AUG,
            KeyConstants.KEY_SEP, KeyConstants.KEY_OCT, KeyConstants.KEY_NOV, KeyConstants.KEY_DEC};

    /**
     * weeks[] - Contains the list of weeks.
     */
    public static final String[] weeks = {KeyConstants.KEY_FIRST_WEEK, KeyConstants.KEY_SECOND_WEEK,
            KeyConstants.KEY_THIRD_WEEK, KeyConstants.KEY_FOURTH_WEEK};

    /**
     * Dividing weeks based on number of days
     */
    public static final String[] monthSplitWithTotal28Days = {"1-7", "8-14", "15-21", "22-28"};
    public static final String[] monthSplitWithTotal29Days = {"1-7", "8-14", "15-21", "22-29"};
    public static final String[] monthSplitWithTotal30Days = {"1-7", "8-14", "15-22", "23-30"};
    public static final String[] monthSplitWithTotal31Days = {"1-7", "8-15", "16-23", "24-31"};


    /**
     * Map which contains the months data
     */
    public static final Map<Integer, String> monthsMap;

    static {
        Map<Integer, String> aMap = new HashMap<>();
        for (int i = 0; i < ValidationUtils.months.length; i++) {
            aMap.put(i, ValidationUtils.months[i]);
        }
        monthsMap = Collections.unmodifiableMap(aMap);
    }

    /**
     * Map that contains weeks data
     */
    public static final Map<Integer, String> weeksMap;

    static {
        Map<Integer, String> aMap = new HashMap<>();
        for (int i = 0; i < ValidationUtils.weeks.length; i++) {
            aMap.put(i, ValidationUtils.weeks[i]);
        }
        weeksMap = Collections.unmodifiableMap(aMap);
    }

    /**
     * Checks if is empty string.
     *
     * @param text the text
     * @return true, if is empty string
     */
    public static boolean isEmptyString(final String text) {
        return text == null || text.trim().length() == 0;
    }

    /**
     * this method is to check given value is null or less than 0
     *
     * @param value
     * @return true, if given value is null or less than 0 otherwise, false
     */
    public static boolean isNullValue(Number value) {
        return Objects.isNull(value) || value.intValue() < 0;
    }

    /**
     * Method to validate email regex
     *
     * @param emailStr
     * @return
     */
    public static boolean emailRegexValidate(String emailStr) {
        Matcher matcher = Regex.EMAIL.matcher(emailStr);
        return matcher.find();
    }

    /**
     * Throw Exception based on status
     */
    public static void throwException(final boolean isException, final String message, final long status) {
        if (isException)
            throw new ApplicationException(status, message, Constants.RESPONSE_FAILURE);
    }

    /**
     * Throw Exception
     *
     * @param message
     * @param status
     * @throws ApplicationException
     */
    public static void throwException(final String message, final long status) {
        throw new ApplicationException(status, message, Constants.RESPONSE_FAILURE);
    }

    /**
     * Returns the number of days in the month
     *
     * @param year
     * @param month
     * @return
     */
    public static int getNumberOfDaysInTheMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.lengthOfMonth();
    }

    /**
     * Returns the validation status
     *
     * @param dob
     * @return
     */
    public static boolean validateDOB(String dob) {
        Date date;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
            date = simpleDateFormat.parse(dob);
        } catch (ParseException exception) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE, null);
        }
        if (date.after(new Date())) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, MessageConstants.MSG_DATE_NOT_ALLOWED_FUTURE_DATE, null);
        }
        return true;
    }

    /**
     * This method is validating the phonenumber via libphonenumber
     *
     * @param countryCode
     * @param phonenumberStr
     * @return status
     */
    public static boolean validatePhonenumber(String countryCode, String phonenumberStr) {
        boolean isValid = false;
        Matcher matcher = Regex.PHONE_NUMBER.matcher(phonenumberStr);
        if(!matcher.matches()){
            return false;
        }
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phonenumber = phoneNumberUtil.parse(phonenumberStr, countryCode);
            isValid = phoneNumberUtil.isValidNumber(phonenumber);
        } catch (NumberParseException exception) {
            log.info(exception.getMessage());
        }
        return isValid;
    }

    /**
     * Returns the validation status
     *
     * @param date
     * @return
     */
    public static boolean validateDate(String date) {
        Date dates;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
            dates = simpleDateFormat.parse(date);
        } catch (ParseException exception) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE, null);
        }
        if (dates.after(new Date())) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, null);
        }
        return true;
    }
    
    /**
     * Returns the validate email by empty and format
     *
     * @param email
     * @return
     */
    public static boolean validateEmail(String email) {
        if (isEmptyString(email)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_EMPTY, null);
        }
        if (!emailRegexValidate(email)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_EMAIL_ERROR, null);
        }
        return true;
    }

    /**
     * Get age from DOB string
     *
     * @param dateString
     * @return
     */
    public static int getAgeFromDob(String dateString) {
        validateDate(dateString);
        String[] dateArr = dateString.split("/");
        LocalDate dob = LocalDate.of(Integer.parseInt(dateArr[2]), Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]));
        LocalDate now = LocalDate.now();
        Period diff = Period.between(dob, now);
        return diff.getYears();
    }

    /**
     * Validate the name for empty string and given length
     *
     * @param name
     * @param maxLength
     */
    public static void validateName(String name, int maxLength) {
        if (isEmptyString(name))
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_NAME_EMPTY, null);
        if (name.length() > maxLength)
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERR_NAME_LENGTH_EXCEEDED, null);
    }

    /**
     * Get week splits based on the number of days in month
     *
     * @param noOfDaysInTheMonth
     * @return
     */
    public static String[] getWeekSplit(int noOfDaysInTheMonth) {
        String[] weekSplit = null;

        if (noOfDaysInTheMonth == 28) {
            weekSplit = ValidationUtils.monthSplitWithTotal28Days;
        } else if (noOfDaysInTheMonth == 29) {
            weekSplit = ValidationUtils.monthSplitWithTotal29Days;
        } else if (noOfDaysInTheMonth == 30) {
            weekSplit = ValidationUtils.monthSplitWithTotal30Days;
        } else if (noOfDaysInTheMonth == 31) {
            weekSplit = ValidationUtils.monthSplitWithTotal31Days;
        }
        return weekSplit;
    }


    /**
     * Returns environment type for Authorize.net
     *
     * @param environment
     * @return
     */
    public static Environment authorizeNetEnvironment(String environment) {
        if (environment.equalsIgnoreCase(KeyConstants.KEY_PROD)) {
            return Environment.PRODUCTION;
        }
        return Environment.SANDBOX;
    }

    public static <T> void emptyList(final List<T> list) {
        if (list.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }
    }

    /**
     * Validate the url
     * @param urlString
     * @return
     */
    public static boolean isUrlValid(final String urlString){
        boolean validUrl = true;
        try{
            new URL(urlString).toURI();
        } catch (MalformedURLException | URISyntaxException exception) {
            validUrl = false;
        }
        return validUrl;
    }

    public static void paramNullCheck(Object object, String errorMessage) {
        if (object == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, errorMessage, null);
        }
    }
}
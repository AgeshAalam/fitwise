package com.fitwise.utils;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.exception.ApplicationException;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class RequestParamValidator {

    private RequestParamValidator() {
    }

    public static void pageSetup(final int pageNo, final int pageSize) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
    }

    public static void sortList(final List<String> allowedSortByList, final String sortBy, final String sortOrder) {
        if (!allowedSortByList.contains(sortBy)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!Arrays.asList(SearchConstants.ORDER_DSC, SearchConstants.ORDER_ASC).contains (sortOrder)) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
    }

    public static void userBlockStatus(final String blockStatus) {
        if (StringUtils.isEmpty(blockStatus)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        } else if (!Arrays.asList(KeyConstants.KEY_ALL, KeyConstants.KEY_OPEN, KeyConstants.KEY_BLOCKED).contains(blockStatus)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }
    }

    public static void stringLengthValidation(String name, Long lengthMin, Long lengthMax, String errorMessage){
        if ((lengthMin != null && name.length() < lengthMin.intValue()) || (lengthMax != null && name.length() > lengthMax.intValue())) {
            throw new ApplicationException(Constants.BAD_REQUEST, errorMessage, null);
        }
    }

    public static void emptyString(String name, String errorMessage){
        if(StringUtils.isEmpty(name)){
            throw new ApplicationException(Constants.BAD_REQUEST, errorMessage, null);
        }
    }

    public static void allowOnlyAlphabets(String name, String errorMessage){
        emptyString(name, errorMessage);
        if(!name.matches("^[a-zA-Z ]*$")){
            throw new ApplicationException(Constants.BAD_REQUEST, errorMessage, null);
        }
    }
}

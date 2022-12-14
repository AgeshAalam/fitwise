package com.fitwise.service.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.jpa.TierTypesAndCountsJPA;
import com.fitwise.specifications.jpa.dao.TierTypesAndCountsDAO;
import com.fitwise.view.CalendarView;
import com.fitwise.view.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AdminDashBoardService {
    private final TierTypesAndCountsJPA tierTypesAndCountsJPA;
    private final ValidationService validationService;

    /**
     * Get tier types and counts
     * @param startDate Start date
     * @param frequency frequency
     * @return Success response
     */
    public ResponseModel getTierTypesAndCounts(Date startDate, String frequency) throws ParseException {
        CalendarView startCalendar = validationService.validateAndGetDateMonthYearFromDate(startDate);
        int year = startCalendar.getYear();
        Date startTime;
        Date endTime;
        if(frequency.equalsIgnoreCase(KeyConstants.KEY_WEEKLY)){
            Calendar calendarStart=Calendar.getInstance();
            calendarStart.setTime(startDate);
            calendarStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendarStart.set(Calendar.HOUR_OF_DAY, 0);
            calendarStart.set(Calendar.MINUTE, 0);
            calendarStart.set(Calendar.SECOND, 0);
            // returning the first date
            startTime=calendarStart.getTime();
            Calendar calendarEnd=Calendar.getInstance();
            calendarEnd.setTime(startDate);
            calendarEnd.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
            calendarEnd.set(Calendar.MINUTE, 59);
            calendarEnd.set(Calendar.SECOND, 59);
            // returning the last date
            endTime=calendarEnd.getTime();
        } else if (frequency.equalsIgnoreCase(KeyConstants.KEY_MONTHLY)) {
            Calendar calendarStart=Calendar.getInstance();
            calendarStart.setTime(startDate);
            calendarStart.set(Calendar.DATE, calendarStart.getActualMinimum(Calendar.DAY_OF_MONTH));
            calendarStart.set(Calendar.HOUR_OF_DAY, 0);
            calendarStart.set(Calendar.MINUTE, 0);
            calendarStart.set(Calendar.SECOND, 0);
            // returning the first date
            startTime=calendarStart.getTime();
            Calendar calendarEnd=Calendar.getInstance();
            calendarEnd.setTime(startDate);
            calendarEnd.set(Calendar.DATE, calendarEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
            calendarEnd.set(Calendar.MINUTE, 59);
            calendarEnd.set(Calendar.SECOND, 59);
            // returning the last date
            endTime=calendarEnd.getTime();
        } else if (frequency.equalsIgnoreCase(KeyConstants.KEY_YEARLY)) {
            Calendar calendarStart=Calendar.getInstance();
            calendarStart.setTime(startDate);
            calendarStart.set(Calendar.YEAR,year);
            calendarStart.set(Calendar.MONTH,0);
            calendarStart.set(Calendar.DAY_OF_MONTH,1);
            calendarStart.set(Calendar.HOUR_OF_DAY, 0);
            calendarStart.set(Calendar.MINUTE, 0);
            calendarStart.set(Calendar.SECOND, 0);
            // returning the first date
            startTime=calendarStart.getTime();
            Calendar calendarEnd=Calendar.getInstance();
            calendarEnd.setTime(startDate);
            calendarEnd.set(Calendar.YEAR,year);
            calendarEnd.set(Calendar.MONTH,11);
            calendarEnd.set(Calendar.DAY_OF_MONTH,31);
            calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
            calendarEnd.set(Calendar.MINUTE, 59);
            calendarEnd.set(Calendar.SECOND, 59);
            // returning the last date
            endTime=calendarEnd.getTime();
        } else {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_FREQUENCY_NOT_FOUND,MessageConstants.ERROR);
        }
        // Get the Tier types and counts
        List<TierTypesAndCountsDAO> tierTypesAndCountsDAOS = tierTypesAndCountsJPA.getCountsOfTiersGroupByTier(startTime,endTime);
        if(tierTypesAndCountsDAOS.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS,null,null);
        }
        // Construct the response
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        responseModel.setPayload(tierTypesAndCountsDAOS);
        return responseModel;
    }
}
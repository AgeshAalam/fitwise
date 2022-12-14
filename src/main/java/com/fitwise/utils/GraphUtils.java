package com.fitwise.utils;

import com.fitwise.constants.Constants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.exception.ApplicationException;
import com.fitwise.service.validation.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by Vignesh G on 11/03/20
 */
@Component
@Slf4j
public class GraphUtils {

    @Autowired
    private ValidationService validationService;

    /**
     * Get Start Date and End Date for a given Date string in MM/yyyy or MM/dd/yyyy
     *
     * @param startDateString
     * @param endDateString
     * @return
     */
    public DateRange getDateRangeForChart(String startDateString, String endDateString) {
        long apiStartTimeMillis = System.currentTimeMillis();

        if (startDateString == null || startDateString.isEmpty() || endDateString == null || endDateString.isEmpty()) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_PROVIDE_START_DATE_AND_END_DATE, null);
        }

        DateRange dateRange = new DateRange();
        Date startDate = null;
        Date endDate = null;

        Date today = new Date();

        if (startDateString.contains("/")) {
            String[] startDateSplit = startDateString.split("/");
            String[] endDateSplit = endDateString.split("/");

            if (startDateSplit.length != endDateSplit.length) {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_START_END_DATE_FORMAT_DIFFERENT, null);
            }
            if (startDateSplit.length < 2 || startDateSplit.length > 3) {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_INVALID_DATE, null);
            }

            if (startDateSplit.length == 3) {
                startDate = validationService.validateAndConstructDate(startDateString);
                endDate = validationService.validateAndConstructDate(endDateString);

            } else {

                int startMonth = Integer.parseInt(startDateSplit[0]);
                int endMonth = Integer.parseInt(endDateSplit[0]);

                int startYear = Integer.parseInt(startDateSplit[1]);
                int endYear = Integer.parseInt(endDateSplit[1]);

                LocalDate startLocalDate = LocalDate.of(startYear, startMonth, 1);
                LocalDateTime startLocalDateTime = startLocalDate.atStartOfDay();
                startDate = Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());

                Calendar calToday = Calendar.getInstance();
                calToday.setTime(today);

                if (calToday.get(Calendar.YEAR) < endYear || ((calToday.get(Calendar.YEAR) == endYear) && (calToday.get(Calendar.MONTH) < endMonth - 1))) {
                    throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, null);
                }

                YearMonth endYearMonth = YearMonth.of(endYear, endMonth);
                LocalDate endLocalDate = endYearMonth.atEndOfMonth();
                LocalTime endLocalTime = LocalTime.of(23, 59, 59);
                LocalDateTime endLocalDateTime = endLocalDate.atTime(endLocalTime);
                endDate = Date.from(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
            }

        } else {

            int startYear = Integer.parseInt(startDateString);
            int endYear = Integer.parseInt(endDateString);

            LocalDate startLocalDate = LocalDate.of(startYear, 1, 1);
            LocalDateTime startLocalDateTime = startLocalDate.atStartOfDay();
            startDate = Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());

            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);

            if (calToday.get(Calendar.YEAR) < endYear) {
                throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, null);
            }

            YearMonth endYearMonth = YearMonth.of(endYear, 12);
            LocalDate endLocalDate = endYearMonth.atEndOfMonth();
            LocalTime endLocalTime = LocalTime.of(23, 59, 59);
            LocalDateTime endLocalDateTime = endLocalDate.atTime(endLocalTime);
            endDate = Date.from(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());

        }

        if (startDate.after(today)) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_FUTURE_DATE_NOT_ALLOWED, null);
        }

        if (startDate.after(endDate)) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_START_DATE_AFTER_END_DATE, null);
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        if ((startCalendar.get(Calendar.YEAR) < 2020) || (endCalendar.get(Calendar.YEAR) < 2020)) {
            throw new ApplicationException(Constants.UNPROCESSABLE_ENTITY, ValidationMessageConstants.MSG_DATE_BEFORE_2020_NOT_ALLOWED, null);
        }

        dateRange.setStartDate(startDate);
        dateRange.setEndDate(endDate);

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info("Date range calculation duration : Time taken in millis : " + (apiEndTimeMillis - apiStartTimeMillis));

        return dateRange;
    }

}

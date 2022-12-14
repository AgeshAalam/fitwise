package com.fitwise.service.admin;

import com.fitwise.constants.*;
import com.fitwise.entity.*;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.*;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.response.TopRatedResponse;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.jpa.CalculateTopRatingJpa;
import com.fitwise.specifications.jpa.PlatformTypeJPA;
import com.fitwise.specifications.jpa.ProgramRatingJPA;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.specifications.jpa.UserRoleMappingJPA;
import com.fitwise.specifications.jpa.dao.InstructorRevenue;
import com.fitwise.specifications.jpa.dao.RevenueByPlatform;
import com.fitwise.specifications.jpa.dao.TopRatedProgram;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.utils.GraphUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.graph.*;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

/*
 * Created by Vignesh G on 23/03/20
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDashBoardInstructorService {

    @Autowired
    UserActivityAuditRepository userActivityAuditRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository roleRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramRatingRepository programRatingRepository;

    @Autowired
    ValidationService validationService;

    @Autowired
    private SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    private PlatformWiseTaxDetailRepository platformWiseTaxDetailRepository;

    @Autowired
    private PlatformTypeRepository platformTypeRepository;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    GraphUtils graphUtils;

    @Autowired
    InstructorPaymentRepository instructorPaymentRepository;

    private final UserRoleMappingJPA userRoleMappingJPA;

    private final CalculateTopRatingJpa calculateTopRatingJpa;

    @Autowired
    private SubscriptionAuditJPA subscriptionAuditJPA;

    @Autowired
    PlatformTypeJPA platformTypeJPA;

    @Autowired
    private ProgramRatingJPA programRatingJPA;



    public ResponseModel getInstructorActivityForYear(String startDateString, String endDateString) {

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        UserRole userRole = roleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);

        int years = diff.getYears();
        int noOfMonths = diff.getMonths();
        noOfMonths = noOfMonths + (years * 12);

        Date today = new Date();
        Date tempDate = new Date(startDate.getTime());

        List<SplitGraphEntry> splitGraphEntryList = new ArrayList<>();
        //Getting each month's last date data
        for (int i = 0; i <= noOfMonths; i++) {
            int monthOffset = 1;
            //data fetch starting from startDate month's last day
            if (i == 0) {
                monthOffset = 0;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(tempDate);
            cal.add(Calendar.MONTH, monthOffset);
            cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            tempDate = cal.getTime();

            Date startTime = new Date(tempDate.getTime());

            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);

            boolean isLaterMonth = false;
            //If a month's last day is AFTER the endDate param, endDate is considered as the day for getting data
            if (startTime.after(today) && calToday.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) {
                cal.setTime(today);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                startTime = cal.getTime();
            } else if(startTime.after(today)) {
                isLaterMonth = true;
            }

            //setting the end time of the day as the end time
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endTime = cal.getTime();

            int totalUsers = 0;
            int activeUsers = 0;
            int inactiveUsers = 0;

            if (!isLaterMonth) {
                totalUsers = userRepository.countByUserRoleMappingsUserRoleAndCreatedDateLessThan(userRole, endTime);
                activeUsers = userActivityAuditRepository.countByUserRoleAndLastActiveTimeBetween(userRole, startTime, endTime);
                inactiveUsers = totalUsers - activeUsers;
            }

            SplitGraphEntry splitGraphEntry = new SplitGraphEntry();
            splitGraphEntry.setPeriodId(i + 1);
            String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            splitGraphEntry.setPeriodName(monthName.substring(0, 3));

            splitGraphEntry.setFirstEntryName("Active");
            splitGraphEntry.setFirstEntryCount(activeUsers);
            float firstpercentage = ((((float) activeUsers) * 100) / (float) totalUsers);
            firstpercentage = (float) (Math.round(firstpercentage * 100.0) / 100.0);
            splitGraphEntry.setFirstEntryPercent(firstpercentage);

            splitGraphEntry.setSecondEntryName("Inactive");
            splitGraphEntry.setSecondEntryCount(inactiveUsers);
            float secondpercentage = ((((float) inactiveUsers) * 100) / (float) totalUsers);
            secondpercentage = (float) (Math.round(secondpercentage * 100.0) / 100.0);
            splitGraphEntry.setSecondEntryPercent(secondpercentage);

            splitGraphEntry.setTotalCount(totalUsers);

            splitGraphEntryList.add(splitGraphEntry);
        }

        SplitGraphData splitGraphData = new SplitGraphData();
        splitGraphData.setGraphEntryList(splitGraphEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(splitGraphData);

        return response;
    }

    public ResponseModel getInstructorActivityForMonth(String startDateString, String endDateString) {

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        UserRole userRole = roleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);

        Date today = new Date();


        if (diff.getYears() > 0 || diff.getMonths() > 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROVIDE_DATES_WITHIN_SAME_MONTH, null);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        int noOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] weekSplit = ValidationUtils.getWeekSplit(noOfDaysInMonth);

        Date tempDate = new Date(startDate.getTime());

        List<SplitGraphEntry> splitGraphEntryList = new ArrayList<>();
        //Getting each week's last date data
        for (int i = 0; i < weekSplit.length; i++) {
            boolean isLaterWeek = false;
            String week[] = weekSplit[i].split("-");
            int lastDayOfWeek = Integer.parseInt(week[week.length - 1]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tempDate);
            calendar.set(Calendar.DATE, lastDayOfWeek);
            Date startTime = calendar.getTime();

            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);


            //If the last day of this week is BEFORE the startDate param, we skip the week
            if (startTime.before(startDate)) {
                continue;
            }

            //If a month's last day is AFTER the endDate param, endDate is considered as the day for getting data
            int a= today.getDate();
            if (startTime.after(today) && a > Integer.parseInt(week[0])) {
                cal.setTime(today);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                startTime = cal.getTime();
            } else if(startTime.after(today)) {
                isLaterWeek = true;
            }


            //If a week's last day is AFTER the endDate param, endDate is considered as the day for getting data
            if (startTime.after(endDate)) {
                startTime = endDate;
                calendar.setTime(endDate);
            }
            int totalUsers = 0;
            int activeUsers = 0;
            int inactiveUsers = 0;


            //setting the end time of the day as the end time
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endTime = calendar.getTime();

            if(!isLaterWeek){
                totalUsers = userRepository.countByUserRoleMappingsUserRoleAndCreatedDateLessThan(userRole, endTime);
                activeUsers = userActivityAuditRepository.countByUserRoleAndLastActiveTimeBetween(userRole, startTime, endTime);
                inactiveUsers = totalUsers - activeUsers;
            }




            SplitGraphEntry splitGraphEntry = new SplitGraphEntry();
            splitGraphEntry.setPeriodId(i + 1);
            splitGraphEntry.setPeriodName("W" + (i + 1));

            splitGraphEntry.setFirstEntryName("Active");
            splitGraphEntry.setFirstEntryCount(activeUsers);
            float firstpercentage = ((((float) activeUsers) * 100) / (float) totalUsers);
            firstpercentage = (float) (Math.round(firstpercentage * 100.0) / 100.0);
            splitGraphEntry.setFirstEntryPercent(firstpercentage);

            splitGraphEntry.setSecondEntryName("Inactive");
            splitGraphEntry.setSecondEntryCount(inactiveUsers);
            float secondpercentage = ((((float) inactiveUsers) * 100) / (float) totalUsers);
            secondpercentage = (float) (Math.round(secondpercentage * 100.0) / 100.0);
            splitGraphEntry.setSecondEntryPercent(secondpercentage);

            splitGraphEntry.setTotalCount(totalUsers);

            splitGraphEntryList.add(splitGraphEntry);
        }

        SplitGraphData splitGraphData = new SplitGraphData();
        splitGraphData.setGraphEntryList(splitGraphEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(splitGraphData);

        return response;

    }

    /**
     * getting top rated instructors count based on rating.
     *
     * @param startDateString
     * @param endDateString
     * @param isForYear
     * @return
     * @throws ParseException
     */
    public ResponseModel getTopRatedInstructors(String startDateString , String endDateString, boolean isForYear) throws ParseException {
        log.info("getTopRatedInstructors starts.");
        long apiStartTimeMillis = new Date().getTime();

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Map<String, Object > map = new HashMap<>();
        String  entryKey = null;
       if(isForYear){
           entryKey = "("+ValidationUtils.months[startLocalDate.getMonthValue()-1]+"-"+ValidationUtils.months[endLocalDate.getMonthValue()-1]+")"+startLocalDate.getYear();
           map.put(KeyConstants.KEY_ENTRY_NAME , entryKey);
       }else {
           Period diff = Period.between(startLocalDate, endLocalDate);
           if (diff.getYears() > 0 || diff.getMonths() > 0) {
               throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROVIDE_DATES_WITHIN_SAME_MONTH, null);
           }
           entryKey = ValidationUtils.months[startLocalDate.getMonthValue()-1]+"-"+startLocalDate.getYear();
           map.put(KeyConstants.KEY_ENTRY_NAME , entryKey);
       }
        List<TopRatedResponse> topRatedResponses = calculateRating(startDate , endDate);
        map.put(KeyConstants.KEY_TOP_RATED , topRatedResponses);

        long apiEndTimeMillis = new Date().getTime();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getTopRatedInstructors ends.");

       return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TOP_RATED_DATA , map);
    }

    @Data
    private class ProgramRateCount {
        double share;
        int count;
    }
    /**
     * calculating rating
     *
     * @param startDate
     * @param endDate
     * @return
     */
    private List<TopRatedResponse> calculateRating(Date startDate , Date endDate){
        log.info("calculateRating method starts");
        long methodStartTimeMillis = new Date().getTime();

        long profilingStartTimeMillis = new Date().getTime();
        Map<Long,ProgramRateCount> map = new HashMap<>();
        List<TopRatedProgram> topRatedProgramList = calculateTopRatingJpa.getTopRatedInstructor(startDate,endDate);
        
        for(TopRatedProgram topRatedProgram : topRatedProgramList){
            if(!map.containsKey(topRatedProgram.getUserId())){
                ProgramRateCount programRateCount = new ProgramRateCount();
                programRateCount.setShare(topRatedProgram.getProgramRatings());
                programRateCount.setCount(1);
                map.put(topRatedProgram.getUserId(),programRateCount);
            }else{
                ProgramRateCount programRateCount = map.get(topRatedProgram.getUserId());
                programRateCount.setCount(programRateCount.getCount()+1);
                programRateCount.setShare(programRateCount.getShare()+topRatedProgram.getProgramRatings());

            }
        }
        long profilingEndTimeMillis = new Date().getTime();
        log.info("Program rating calculation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        int ratingFourPlus = 0;
        int ratingThreePlus = 0;
        int ratingTwoPlus = 0;
        int ratingOnePlus = 0;

        for (ProgramRateCount rateCount : map.values()) {
            double rating = rateCount.getShare()/rateCount.getCount();
            if(4 <=rating && rating <= 5 ){
                ratingFourPlus++;
            }else if(3<=rating && rating < 4 ){
                ratingThreePlus++;
            }else if(2<=rating && rating < 3){
                ratingTwoPlus++;
            }else if(1<= rating && rating < 2 ){
                ratingOnePlus++;
            }
        }
        int[] plusRating = {ratingOnePlus , ratingTwoPlus , ratingThreePlus, ratingFourPlus };

        profilingStartTimeMillis = new Date().getTime();
        List<TopRatedResponse> topRatedResponses = new ArrayList<>();
        for(int index = 0; index < plusRating.length; index ++){
            TopRatedResponse oneTopRated = new TopRatedResponse();
            oneTopRated.setRating(index+1);
            oneTopRated.setInstructorsCount(plusRating[index]);
            topRatedResponses.add(oneTopRated);
        }
        Collections.reverse(topRatedResponses);
        profilingEndTimeMillis = new Date().getTime();
        log.info("Rating segregation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long methodEndTimeMillis = new Date().getTime();
        log.info("calculateRating() duration : Time taken in millis : " + (methodEndTimeMillis - methodStartTimeMillis));
        log.info("calculateRating method ends.");

        return topRatedResponses;
    }


    public ResponseModel getRevenuePayoutForYear(String startDateString, String endDateString) throws ParseException {
        log.info("Get revenue payout for year starts");
        long start = new Date().getTime();
        long profilingStart;
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        double totalInstructorShare = 0.0;

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        int years = diff.getYears();
        int noOfMonths = diff.getMonths();
        noOfMonths = noOfMonths + (years * 12);

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});


        List<GraphEntryWithValue<Double>> graphEntryWithValues = new ArrayList<>();
        log.info("Zone changes done : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();

        int month = 1;
        int year = Integer.parseInt(startDateString);
        //Getting each month's last date data
        for (int i = 0; i <= noOfMonths; i++) {
            double instructorShare = 0.0;
            GraphEntryWithValue<Double> graphEntryWithValue = new GraphEntryWithValue<>();
            int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year, month);
            Calendar cal = Calendar.getInstance();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date startTime = formatter.parse(year + "-" + month + "-" + 1);
            Date endTime = formatter.parse(year + "-" + month + "-" + noOfDaysInTheMonth);
            cal.setTime(startTime);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startTime = cal.getTime();

            //setting the end time of the day as the end time
            cal.setTime(endTime);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endTime = cal.getTime();
            month++;

            graphEntryWithValue.setPeriodId(i + 1);
            graphEntryWithValue.setPeriodName(ValidationUtils.months[i]);
            Double instructorPayout = subscriptionAuditJPA.getInstructorShareBySubscriptionType(startTime, endTime);
            if (instructorPayout != null)
                instructorShare = instructorPayout;

            instructorShare = Math.round(instructorShare * 100.0) / 100.0;
            totalInstructorShare += instructorShare;
            graphEntryWithValue.setValue(instructorShare);
            graphEntryWithValue.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorShare));
            graphEntryWithValues.add(graphEntryWithValue);
        }
        log.info("Get revenue for each month of a year : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();

        GraphDataWithValue<Double> graphData = new GraphDataWithValue<Double>();
        totalInstructorShare = Math.round(totalInstructorShare * 100.0) / 100.0;
        graphData.setOverAllValue(totalInstructorShare);
        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalInstructorShare));

        graphData.setGraphEntryList(graphEntryWithValues);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);
        log.info("Calculating total revenue and making up the response model : Time taken in millis : "+(new Date().getTime() - profilingStart));

        log.info("Get revenue payout for year : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get revenue payout for year ends");

        return response;
    }


    public ResponseModel getRevenuePayoutForMonth(String startDateString, String endDateString) {
        log.info("Get revenue payout for month starts");
        long start = new Date().getTime();
        long profilingStart;
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        double totalInstructorShare = 0.0;

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        if (diff.getYears() > 0 || diff.getMonths() > 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROVIDE_DATES_WITHIN_SAME_MONTH, null);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        int noOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] weekSplit = ValidationUtils.getWeekSplit(noOfDaysInMonth);

        Date tempDate = new Date(startDate.getTime());
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        log.info("Zone changes done : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();


        List<GraphEntryWithValue<Double>> graphEntryWithValues = new ArrayList<>();
        //Getting each week's last date data
        for (int i = 0; i < weekSplit.length; i++) {
            GraphEntryWithValue<Double> graphEntryWithValue = new GraphEntryWithValue<>();
            double instructorShare = 0.0;
            String week[] = weekSplit[i].split("-");
            int firstDayOfWeek = Integer.parseInt(week[0]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tempDate);
            calendar.set(Calendar.DATE, firstDayOfWeek);
            Date startTime = calendar.getTime();


            //setting the end time of the day as the end time
            int lastDayOfWeek = Integer.parseInt(week[week.length - 1]);
            calendar.set(Calendar.DATE, lastDayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endTime = calendar.getTime();

            graphEntryWithValue.setPeriodId(i + 1);
            graphEntryWithValue.setPeriodName("W" + (i+1));
            Double instructorPayout = subscriptionAuditJPA.getInstructorShareBySubscriptionType(startTime, endTime);
            if (instructorPayout != null)
                instructorShare = instructorPayout;

            instructorShare = Math.round(instructorShare * 100.0) / 100.0;
            totalInstructorShare += instructorShare;
            graphEntryWithValue.setValue(instructorShare);
            graphEntryWithValue.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorShare));
            graphEntryWithValues.add(graphEntryWithValue);

        }
        log.info("Get revenue for each week of a month : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();

        GraphDataWithValue<Double> graphData = new GraphDataWithValue<Double>();
        totalInstructorShare = Math.round(totalInstructorShare * 100.0) / 100.0;
        graphData.setOverAllValue(totalInstructorShare);
        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalInstructorShare));

        graphData.setGraphEntryList(graphEntryWithValues);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);
        log.info("Calculating total revenue and making up the response model : Time taken in millis : "+(new Date().getTime() - profilingStart));

        log.info("Get revenue payout for month : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get revenue payout for month ends");

        return response;

    }

    public ResponseModel getRevenuePayoutByPlatform(String startDateString, String endDateString) {
        log.info("Get revenue payout by platform starts");
        long start = new Date().getTime();
        long profilingStart;
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();

        List<ChartEntryWithValue<Double>> chartEntryList = new ArrayList<>();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);

        profilingStart = new Date().getTime();
        List<RevenueByPlatform> revenueByPlatformList = platformTypeJPA.getInstructorShareByPlatformAndSubscriptionDateBetween(startDate, endDate);
        log.info("Query : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();

        double totalRevenue = 0.0;

        for (RevenueByPlatform revenueByPlatform : revenueByPlatformList) {
            double instructorShare = 0.0;
            ChartEntryWithValue<Double> chartEntry = new ChartEntryWithValue<Double>();
            chartEntry.setEntryName(platFormDisplayName.getProperty(revenueByPlatform.getPlatformName(), revenueByPlatform.getPlatformName()));

            if (revenueByPlatform.getRevenue() != null)
                instructorShare += revenueByPlatform.getRevenue();

            totalRevenue += instructorShare;
            instructorShare = Math.round(instructorShare * 100.0) / 100.0;
            chartEntry.setValue(instructorShare);
            chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorShare));
            chartEntryList.add(chartEntry);

        }
        log.info("Get total revenue by platform : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();

        new ChartDataUtils().populationEntryPercentages(chartEntryList, totalRevenue);

        ChartDataWithValue<Double> chartData = new ChartDataWithValue<>();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        chartData.setTotalValue(totalRevenue);
        chartData.setFormattedTotalValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));
        chartData.setChartEntryList(chartEntryList);


        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Calculating total revenue and making up the response model : Time taken in millis : "+(new Date().getTime() - profilingStart));

        log.info("Get revenue payout by platform : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get revenue payout by platform ends");

        return response;

    }

    public ResponseModel getTopEarned(String startDateString, String endDateString) {
        log.info("getTopEarned starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        List<String> chartEntryNames = Arrays.asList(new String[]{"$1000 +", "$501 - $1000", "< $500", "$0"});
        Map<String, Integer> topEarnedMap = new HashMap();

        for (String chartEntryName : chartEntryNames) {
            topEarnedMap.put(chartEntryName, 0);
        }


        long profilingStartTimeMillis = System.currentTimeMillis();
        List<InstructorRevenue> instructorRevenueList = userRoleMappingJPA.getAllInstructorProgramRevenue(startDate, endDate);
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        int totalCount = 0;
        for (InstructorRevenue instructorRevenueItem : instructorRevenueList) {
            double instructorRevenue = instructorRevenueItem.getRevenue() != null ? instructorRevenueItem.getRevenue() : 0.0;

            instructorRevenue = Math.round(instructorRevenue * 100.0) / 100.0;
            if (instructorRevenue == 0.0) {
                topEarnedMap.put("$0", topEarnedMap.get("$0") + 1);
            } else if (instructorRevenue <= 500 && instructorRevenue > 0) {
                topEarnedMap.put("< $500", topEarnedMap.get("< $500") + 1);
            } else if (instructorRevenue > 500 && instructorRevenue <= 1000) {
                topEarnedMap.put("$501 - $1000", topEarnedMap.get("$501 - $1000") + 1);
            } else if (instructorRevenue > 1000) {
                topEarnedMap.put("$1000 +", topEarnedMap.get("$1000 +") + 1);
            }
            totalCount++;

        }

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        List<ChartEntry> chartEntries = new ArrayList<>();
        for (String chartEntryName : chartEntryNames) {
            ChartEntry chartEntry = new ChartEntry();
            chartEntry.setCount(topEarnedMap.get(chartEntryName));
            chartEntry.setEntryName(chartEntryName);
            chartEntries.add(chartEntry);
        }
        new ChartDataUtils().populationEntryPercentage(chartEntries, totalCount);

        ChartData chartData = new ChartData();
        chartData.setChartEntryList(chartEntries);
        chartData.setTotalCount(totalCount);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Response construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getTopEarned ends.");

        return response;
    }
}

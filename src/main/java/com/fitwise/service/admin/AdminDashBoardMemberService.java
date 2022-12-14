package com.fitwise.service.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Duration;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.ProgramViewsAudit;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.DurationRepo;
import com.fitwise.repository.ExpertiseLevelRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.ProgramTypeRepository;
import com.fitwise.repository.ProgramViewsAuditRepository;
import com.fitwise.repository.UserActivityAuditRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleMappingRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.specifications.jpa.PlatformTypeJPA;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.specifications.jpa.dao.InstructorPayout;
import com.fitwise.specifications.jpa.dao.RevenueByPlatform;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.utils.GraphUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.graph.ChartData;
import com.fitwise.view.graph.ChartDataWithValue;
import com.fitwise.view.graph.ChartEntry;
import com.fitwise.view.graph.ChartEntryWithValue;
import com.fitwise.view.graph.GraphDataWithValue;
import com.fitwise.view.graph.GraphEntryWithValue;
import com.fitwise.view.graph.SplitGraphData;
import com.fitwise.view.graph.SplitGraphEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/*
 * Created by Vignesh G on 25/03/20
 */

@Service
@Slf4j
public class AdminDashBoardMemberService {

    @Autowired
    UserActivityAuditRepository userActivityAuditRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository roleRepository;

    @Autowired
    private ProgramTypeRepository programTypeRepository;

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    ExpertiseLevelRepository expertiseLevelRepository;

    @Autowired
    private DurationRepo durationRepo;

    @Autowired
    private PlatformTypeRepository platformTypeRepository;

    @Autowired
    GraphUtils graphUtils;

    @Autowired
    private ProgramViewsAuditRepository programViewsAuditRepository;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    private InstructorPaymentRepository instructorPaymentRepository;

    @Autowired
    private SubscriptionAuditJPA subscriptionAuditJPA;

    @Autowired
    private PlatformTypeJPA platformTypeJPA;

    public ResponseModel getMemberActivityForYear(String startDateString, String endDateString) {

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        UserRole userRole = roleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);

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

    public ResponseModel getMemberActivityForMonth(String startDateString, String endDateString) {

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        UserRole userRole = roleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);

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
            String week[] = weekSplit[i].split("-");
            int lastDayOfWeek = Integer.parseInt(week[week.length - 1]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tempDate);
            calendar.set(Calendar.DATE, lastDayOfWeek);
            Date startTime = calendar.getTime();

            //If the last day of this week is BEFORE the startDate param, we skip the week
            if (startTime.before(startDate)) {
                continue;
            }
            //If a week's last day is AFTER the endDate param, endDate is considered as the day for getting data
            if (startTime.after(endDate)) {
                startTime = endDate;
                calendar.setTime(endDate);
            }

            //setting the end time of the day as the end time
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endTime = calendar.getTime();

            int totalUsers = userRepository.countByUserRoleMappingsUserRoleAndCreatedDateLessThan(userRole, endTime);

            int activeUsers = userActivityAuditRepository.countByUserRoleAndLastActiveTimeBetween(userRole, startTime, endTime);
            int inactiveUsers = totalUsers - activeUsers;

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

    public ResponseModel getPreferredPrograms(String startDateString, String endDateString) {
        log.info("Get preferred programs starts.");
        long apiStartTimeMillis = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get date range : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        List<ProgramTypes> programTypesList = programTypeRepository.findByOrderByProgramTypeNameAsc();
        log.info("Query to get program type list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        ChartData chartData = new ChartData();

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        for (ProgramTypes programType : programTypesList) {
            ChartEntry chartEntry = new ChartEntry();
            int count = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndProgramSubscriptionProgramProgramTypeProgramTypeIdAndSubscriptionStatusSubscriptionStatusNameInAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, programType.getProgramTypeId(), statusList, startDate, endDate);
            chartEntry.setEntryName(programType.getProgramTypeName());
            chartEntry.setCount(count);

            chartEntryList.add(chartEntry);
            totalCount += count;
        }
        log.info("Query to get subscription count : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
            List<String> chartEntryNames = programTypesList.stream().map(programType -> programType.getProgramTypeName()).collect(Collectors.toList());
            log.info("Extract program type name : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Get preferred programs ends.");
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }

        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);

        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get preferred programs ends.");

        return response;

    }

    public ResponseModel getPreferredDuration(String startDateString, String endDateString) {
        log.info("Get preferred duration starts.");
        long apiStartTimeMillis = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get date range : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        List<Duration> durationList = durationRepo.findAllByOrderByDurationAsc();
        log.info("Get duration list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        ChartData chartData = new ChartData();

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        for (Duration duration : durationList) {
            ChartEntry chartEntry = new ChartEntry();
            int count = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndProgramSubscriptionProgramDurationDurationIdAndSubscriptionStatusSubscriptionStatusNameInAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, duration.getDurationId(), statusList, startDate, endDate);
            chartEntry.setEntryName(duration.getDuration() + " Days");
            chartEntry.setCount(count);

            chartEntryList.add(chartEntry);
            totalCount += count;
        }
        log.info("Get subscription count : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
            List<String> chartEntryNames = durationList.stream().map(duration -> duration.getDuration() + " Days").collect(Collectors.toList());
            log.info("Extract durations : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Get preferred duration ends.");
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }

        profilingEndTimeMillis = new Date().getTime();
        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);

        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get preferred duration ends.");

        return response;

    }

    /**
     * Generate chart of all users in Trainnr platform by Expertise level
     *
     * @param startDateString
     * @param endDateString
     * @return
     */
    public ResponseModel getUserDemographics(String startDateString, String endDateString) {
        log.info("Get user demographics starts.");
        long apiStartTimeMillis = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get date range : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        List<ExpertiseLevels> expertiseLevels = expertiseLevelRepository.findAll();
        log.info("Query to get expertise levels : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        ChartData chartData = new ChartData();

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        for (ExpertiseLevels expertiseLevel : expertiseLevels) {
            if(!expertiseLevel.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)){
                ChartEntry chartEntry = new ChartEntry();
                int count = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramExpertiseLevelAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, statusList, expertiseLevel, startDate, endDate);
                chartEntry.setEntryName(expertiseLevel.getExpertiseLevel());
                chartEntry.setCount(count);

                chartEntryList.add(chartEntry);
                totalCount += count;
            }
        }
        log.info("Construct chart entry : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
            List<String> chartEntryNames = expertiseLevels.stream().filter(expertiseLevels1 -> !expertiseLevels1.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)).map(expertiseLevel -> expertiseLevel.getExpertiseLevel()).collect(Collectors.toList());
            log.info("Get chart entries names : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Get user demographics ends.");
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }

        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);

        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get user demographics ends.");

        return response;
    }

    public ResponseModel getSpentForYear(String startDateString, String endDateString) throws ParseException {
        log.info("getSpentForYear starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        double totalSpent = 0.0;

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        int years = diff.getYears();
        int noOfMonths = diff.getMonths();
        noOfMonths = noOfMonths + (years * 12);

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});


        List<GraphEntryWithValue<Double>> graphEntryWithValues = new ArrayList<>();

        long profilingStartTimeMillis = System.currentTimeMillis();

        int month = 1;
        int year = Integer.parseInt(startDateString);
        //Getting each month's last date data
        for (int i = 0; i <= noOfMonths; i++) {
            double amountSpent = 0.0;
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
            Double instructorPayout = subscriptionAuditJPA.getTotalRevenueBySubscriptionType(startTime, endTime);
            if (instructorPayout != null)
                amountSpent = instructorPayout;

            amountSpent = Math.round(amountSpent * 100.0) / 100.0;
            totalSpent += amountSpent;
            graphEntryWithValue.setValue(amountSpent);
            graphEntryWithValue.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(amountSpent));
            graphEntryWithValues.add(graphEntryWithValue);
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        GraphDataWithValue<Double> graphData = new GraphDataWithValue<Double>();
        totalSpent = Math.round(totalSpent * 100.0) / 100.0;
        graphData.setOverAllValue(totalSpent);
        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalSpent));

        graphData.setGraphEntryList(graphEntryWithValues);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getSpentForYear ends.");

        return response;
    }


    public ResponseModel getSpentForMonth(String startDateString, String endDateString) {
        log.info("getSpentForMonth starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        double totalSpent = 0.0;

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

        long profilingStartTimeMillis = System.currentTimeMillis();

        List<GraphEntryWithValue<Double>> graphEntryWithValues = new ArrayList<>();
        //Getting each week's last date data
        for (int i = 0; i < weekSplit.length; i++) {
            GraphEntryWithValue<Double> graphEntryWithValue = new GraphEntryWithValue<>();
            double amountSpent = 0.0;
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
            Double instructorPayout = subscriptionAuditJPA.getTotalRevenueBySubscriptionType(startTime, endTime);
            if (instructorPayout != null)
                amountSpent = instructorPayout;

            amountSpent = Math.round(amountSpent * 100.0) / 100.0;
            totalSpent += amountSpent;
            graphEntryWithValue.setValue(amountSpent);
            graphEntryWithValue.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(amountSpent));
            graphEntryWithValues.add(graphEntryWithValue);

        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        GraphDataWithValue<Double> graphData = new GraphDataWithValue<Double>();
        totalSpent = Math.round(totalSpent * 100.0) / 100.0;
        graphData.setOverAllValue(totalSpent);
        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalSpent));

        graphData.setGraphEntryList(graphEntryWithValues);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getSpentForMonth ends.");

        return response;
    }


    public ResponseModel spentByPlatform(String startDateString, String endDateString) {
        log.info("spentByPlatform starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);

        Long temp = new Date().getTime();
        List<RevenueByPlatform> revenueByPlatformList = platformTypeJPA.getRevenueByPlatformAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, startDate, endDate);
        log.info("Query : Time taken in millis : " + (new Date().getTime() - temp));

        List<ChartEntryWithValue<Double>> chartEntryList = new ArrayList<>();
        double totalSpent = 0.0;

        for (RevenueByPlatform revenueByPlatform : revenueByPlatformList) {
            double price = 0.0;
            if (revenueByPlatform.getRevenue() != null)
                price = revenueByPlatform.getRevenue();
            String platformTypeName = revenueByPlatform.getPlatformName();

            ChartEntryWithValue<Double> chartEntry = new ChartEntryWithValue<Double>();
            chartEntry.setEntryName(platFormDisplayName.getProperty(platformTypeName, platformTypeName));

            price = Math.round(price * 100.0) / 100.0;
            chartEntry.setValue(price);
            chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(price));
            totalSpent += price;

            chartEntryList.add(chartEntry);
        }

        new ChartDataUtils().populationEntryPercentages(chartEntryList, totalSpent);

        ChartDataWithValue<Double> chartData = new ChartDataWithValue<>();
        totalSpent = Math.round(totalSpent * 100.0) / 100.0;
        chartData.setTotalValue(totalSpent);
        chartData.setFormattedTotalValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalSpent));
        chartData.setChartEntryList(chartEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("spentByPlatform ends.");

        return response;
    }

    /**
     * Member Client acquisition
     * @param startDateString
     * @param endDateString
     * @return
     */
    public ResponseModel getMemberClientAcquisition(String startDateString, String endDateString) {
        log.info("Get member client acquisition starts.");
        long apiStartTimeMillis = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        log.info("Get date range : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});


        int enrollmentCount = 0;
        int programViews = 0;
        int trialCount = 0;
        int subscriptionCount = 0;
        int renewal = 0;


        enrollmentCount = userRoleMappingRepository.countByUserRoleNameAndCreatedDateBetween(KeyConstants.KEY_MEMBER, startDate, endDate);
        List<ProgramViewsAudit> programViewsAudits = programViewsAuditRepository.findByUserNotNullAndDateBetween(startDate, endDate);
        log.info("Queries to get enrollment and program views audit : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        List<ProgramViewsAudit> programViewsAuditList = programViewsAudits.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(ProgramViewsAudit::getUser,comparing(User::getUserId)).thenComparing(ProgramViewsAudit::getProgram,comparing(Programs::getProgramId)))),
                        ArrayList::new));
        log.info("Get unique program views audit list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        programViews = programViewsAuditList.size();

        trialCount = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameAndSubscriptionDateBetween
                (KeyConstants.KEY_PROGRAM, KeyConstants.KEY_TRIAL, startDate, endDate);

        subscriptionCount = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndRenewalStatusAndSubscriptionDateBetween
                (KeyConstants.KEY_PROGRAM, statusList, KeyConstants.KEY_NEW, startDate, endDate);

        renewal = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndRenewalStatusAndSubscriptionDateBetween
                (KeyConstants.KEY_PROGRAM, statusList, KeyConstants.KEY_RENEWAL, startDate, endDate);
        log.info("Queries to get trial count, subscription count and renewal count from subscription audit repo : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        List<String> chartEntryNames = Arrays.asList(new String[]{KeyConstants.KEY_ENROLLMENT, KeyConstants.KEY_PROGRAM_VIEWS, KeyConstants.KEY_TRIAL, KeyConstants.KEY_SUBSCRIPTION,KeyConstants.KEY_RENEWAL_STATUS});
        List<ChartEntry> chartEntryList = new ArrayList<>();
        for (String chartEntryName : chartEntryNames) {
            ChartEntry chartEntry  = new ChartEntry();

            if (chartEntryName.equalsIgnoreCase(KeyConstants.KEY_ENROLLMENT)) {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setCount(enrollmentCount);
            } else if (chartEntryName.equalsIgnoreCase(KeyConstants.KEY_PROGRAM_VIEWS)) {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setCount(programViews);
            } else if (chartEntryName.equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setCount(trialCount);
            }else if (chartEntryName.equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION)) {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setCount(subscriptionCount);
            }else if (chartEntryName.equalsIgnoreCase(KeyConstants.KEY_RENEWAL_STATUS)) {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setCount(renewal);
            }
            chartEntryList.add(chartEntry);

        }
        log.info("Construct chart entry list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));


        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartEntryList);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get member client acquisition ends.");

        return response;
    }

}

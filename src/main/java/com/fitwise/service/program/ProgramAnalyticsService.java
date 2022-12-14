package com.fitwise.service.program;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.DiscardWorkoutReasons;
import com.fitwise.entity.FeedbackTypes;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.PlatformWiseTaxDetail;
import com.fitwise.entity.ProgramSubscriptionPaymentHistory;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.WorkoutDiscardFeedback;
import com.fitwise.entity.WorkoutDiscardFeedbackMapping;
import com.fitwise.entity.WorkoutSchedule;
import com.fitwise.entity.instructor.InstructorTierDetails;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.exception.ApplicationException;
import com.fitwise.program.graph.ProgramClientDemographicsByAge;
import com.fitwise.program.graph.ProgramClientDemographicsByGender;
import com.fitwise.program.graph.ProgramClientDemographicsGraph;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.PlatformWiseTaxDetailRepository;
import com.fitwise.repository.SubscriptionPaymentHistoryRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.feedback.DiscardWorkoutReasonsRepository;
import com.fitwise.repository.feedback.FeedbackTypesRepository;
import com.fitwise.repository.feedback.WorkoutDiscardFeedbackRepository;
import com.fitwise.repository.feedback.WorkoutFeedbackRepository;
import com.fitwise.repository.instructor.InstructorTierDetailsRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.response.ClientAcquisitionUsersResponse;
import com.fitwise.response.DiscardWorkoutFeedbackResponse;
import com.fitwise.response.DiscardWorkoutFeedbackStatsResponse;
import com.fitwise.response.WorkoutNormalFeedbackResponse;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.admin.FitwiseShareService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.UserProfileSpecifications;
import com.fitwise.specifications.jpa.ProgramPromoViewsJPA;
import com.fitwise.specifications.jpa.ProgramRevenueJPA;
import com.fitwise.specifications.jpa.ProgramViewsAuditJPA;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.specifications.jpa.dao.RevenueByPlatform;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.GraphUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ClientAcquisitionViewResponse;
import com.fitwise.view.MonthSubscriptionsOfNewAndRenewalResponseView;
import com.fitwise.view.MonthSubscriptionsOfNewAndRenewalView;
import com.fitwise.view.MonthlyRevenueResponseView;
import com.fitwise.view.MonthlyRevenueView;
import com.fitwise.view.MonthlySubscriptionResponseView;
import com.fitwise.view.MonthlySubscriptionView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.YearSubscriptionsOfNewAndRenewalResponseView;
import com.fitwise.view.YearSubscriptionsOfNewAndRenewalView;
import com.fitwise.view.YearlyRevenueResponseView;
import com.fitwise.view.YearlyRevenueView;
import com.fitwise.view.YearlySubscriptionResponseView;
import com.fitwise.view.YearlySubscriptionView;
import com.fitwise.view.graph.ChartData;
import com.fitwise.view.graph.ChartDataWithValue;
import com.fitwise.view.graph.ChartEntry;
import com.fitwise.view.graph.ChartEntryWithValue;
import com.fitwise.view.graph.SplitGraphDataWithValue;
import com.fitwise.view.graph.SplitGraphEntryWithValue;
import com.fitwise.view.instructor.ProgramAnalyticsResponseView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


/*
 * Created by Vignesh G on 17/03/20
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ProgramAnalyticsService {


    @Autowired
    ValidationService validationService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    ProgramClientDemographicsByGender programClientDemographicsByGender;

    @Autowired
    ProgramClientDemographicsByAge programClientDemographicsByAge;

    @Autowired
    PlatformTypeRepository platformTypeRepository;

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    WorkoutFeedbackRepository workoutFeedbackRepository;

    @Autowired
    WorkoutDiscardFeedbackRepository workoutDiscardFeedbackRepository;

    @Autowired
    DiscardWorkoutReasonsRepository discardWorkoutReasonsRepository;

    @Autowired
    FeedbackTypesRepository feedbackTypesRepository;

    @Autowired
    private PlatformWiseTaxDetailRepository platformWiseTaxDetailRepository;

    @Autowired
    private SubscriptionPaymentHistoryRepository subscriptionPaymentHistoryRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    GraphUtils graphUtils;

    private final ProgramViewsAuditJPA programViewsAuditJPA;
    private final ProgramPromoViewsJPA programPromoViewsJPA;
    private final SubscriptionAuditJPA subscriptionAuditJPA;
    private final FitwiseShareService fitwiseShareService;

    private final ProgramRevenueJPA programRevenueJpa;
    private final InstructorTierDetailsRepository instructorTierDetailsRepository;

    /**
     * Method used to get the instructor revenue data.
     *
     * @param date
     * @param programId
     * @param isRenewDataNeeded
     * @return
     */
    public ResponseModel getProgramRevenueStatsOfAnInstructor(String date, long programId, boolean isRenewDataNeeded) throws ParseException {
        log.info("Get program revenue stats of an instructor starts.");
        long apiStartTimeMillis = new Date().getTime();
        boolean isYearDataNeeded = false;

        validationService.validateProgramIdBlocked(programId);
        /*
         * YearlyRevenueResponseView - Splitted by means of months (Jan to Dec)
         */
        YearlyRevenueResponseView yearlyRevenueResponseView = new YearlyRevenueResponseView();

        double totalYearlyRevenue = 0.0;
        double totalMonthlyRevenue = 0.0;

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);


        /*
         * YearlyRevenueResponseView - Splitted by means of weeks (W1 to W4)
         */
        MonthlyRevenueResponseView monthlyRevenueResponseView = new MonthlyRevenueResponseView();
        monthlyRevenueResponseView.setTotalMonthlyRevue("$0.00");
        if (ValidationUtils.isEmptyString(date)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_DATE, MessageConstants.ERROR);
        }
        String[] RequestDates = date.split("/");
        if (RequestDates.length == 1) {
            isYearDataNeeded = true;
            validationService.validateYear(date);
        } else if (RequestDates.length == 2) {
            validationService.validateMonth(RequestDates[0]);
            validationService.validateYear(RequestDates[1]);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_REVENUE_DATA_FETCHED);
        if (isYearDataNeeded) {
            int month = 1;
            int year = Integer.parseInt(date);


            List<YearlyRevenueView> revenueViews = new ArrayList<>();

            for (int i = 0; i < 12; i++) {

                int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year, month);
                Calendar cal = Calendar.getInstance();

                SimpleDateFormat formatter = new SimpleDateFormat(StringConstants.PATTERN_DATE_YYYY_MM_DD);
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
                double newAmount = 0;
                double renewAmount;
                /*
                 * YearlyRevenueView - Per month view
                 */
                YearlyRevenueView yearlyRevenueView = new YearlyRevenueView();
                yearlyRevenueView.setMonthId(i + 1);
                yearlyRevenueView.setMonthName(ValidationUtils.months[i]);
                if (isRenewDataNeeded) {
                    renewAmount = programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_RENEWAL,startTime,endTime);
                    renewAmount = Math.round(renewAmount * 100.0) / 100.0;
                    totalYearlyRevenue += renewAmount;
                    yearlyRevenueView.setMonthRevenue(renewAmount);
                    yearlyRevenueView.setFormattedMonthRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(renewAmount));
                } else {
                    newAmount += programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_NEW,startTime,endTime);
                    newAmount = Math.round(newAmount * 100.0) / 100.0;
                    totalYearlyRevenue += newAmount;
                    yearlyRevenueView.setMonthRevenue(newAmount);
                    yearlyRevenueView.setFormattedMonthRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(newAmount));
                }
                revenueViews.add(yearlyRevenueView);
            }
            yearlyRevenueResponseView.setMonthlyRevenueSplits(revenueViews);
            totalYearlyRevenue = Math.round(totalYearlyRevenue * 100.0) / 100.0;
            yearlyRevenueResponseView.setTotalYearlyRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalYearlyRevenue));

            responseModel.setPayload(yearlyRevenueResponseView);
            log.info("Construct response model for year : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            List<MonthlyRevenueView> revenueViews = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat(StringConstants.PATTERN_DATE_YYYY_MM_DD);
            Date tempDate = formatter.parse(Integer.parseInt(RequestDates[1]) + "-" + Integer.parseInt(RequestDates[0]) + "-" + "1");
            cal.setTime(tempDate);
            int noOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            String[] weekSplit = ValidationUtils.getWeekSplit(noOfDaysInMonth);

            /*
             * Setting the date limits to the weeks by parsing through the weekSplit array
             */
            for (int i = 0; i < weekSplit.length; i++) {
                String[] week = weekSplit[i].split("-");
                MonthlyRevenueView monthlyRevenueView = new MonthlyRevenueView();
                Date startDate = formatter.parse(Integer.parseInt(RequestDates[1]) + "-" + Integer.parseInt(RequestDates[0]) + "-" + week[0]);
                // String status = null
                int firstDayOfWeek = Integer.parseInt(week[0]);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.set(Calendar.DATE, firstDayOfWeek);
                Date startTime = calendar.getTime();
                //setting the end time of the day as the end time
                int lastDayOfWeek = Integer.parseInt(week[week.length - 1]);
                calendar.set(Calendar.DATE, lastDayOfWeek);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                Date endTime = calendar.getTime();
                double newAmount = 0.0;
                double renewAmount = 0.0;
                if (isRenewDataNeeded) {
                    renewAmount += programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_RENEWAL,startTime,endTime);
                    renewAmount = Math.round(renewAmount * 100.0) / 100.0;
                    totalMonthlyRevenue += renewAmount;
                    monthlyRevenueView.setWeekId(i + 1);
                    monthlyRevenueView.setWeekName("W" + (i + 1));
                    monthlyRevenueView.setWeekDuration(week[0] + KeyConstants.KEY_HYPHEN + week[week.length - 1]);
                    monthlyRevenueView.setRevenue(renewAmount);
                    monthlyRevenueView.setFormattedRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(renewAmount));
                } else {
                    newAmount += programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_NEW,startTime,endTime);
                    newAmount = Math.round(newAmount * 100.0) / 100.0;
                    totalMonthlyRevenue += newAmount;
                    monthlyRevenueView.setWeekId(i + 1);
                    monthlyRevenueView.setWeekName("W" + (i + 1));
                    monthlyRevenueView.setWeekDuration(week[0] + KeyConstants.KEY_HYPHEN + week[week.length - 1]);
                    monthlyRevenueView.setRevenue(newAmount);
                    monthlyRevenueView.setFormattedRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(newAmount));
                }
                revenueViews.add(monthlyRevenueView);
            }
            monthlyRevenueResponseView.setRevenuePerWeek(revenueViews);
            totalMonthlyRevenue = Math.round(totalMonthlyRevenue * 100.0) / 100.0;
            monthlyRevenueResponseView.setTotalMonthlyRevue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalMonthlyRevenue));
            responseModel.setPayload(monthlyRevenueResponseView);
            log.info("Construct response model for month : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program revenue stats of an instructor ends.");
        return responseModel;
    }

    /**
     * Method to ge the Program Subscription Statistics of an Instructor
     *
     * @param date
     * @param programId
     * @param isRenewDataNeeded
     * @return
     */
    public ResponseModel getProgramSubscriptionStatsOfAnInstructor(String date, long programId, boolean isRenewDataNeeded) throws ParseException {
        log.info("Get program subscription stats of an instructor starts.");
        long apiStartTimeMillis = new Date().getTime();
        boolean isYearDataNeeded = false;
        validationService.validateProgramIdBlocked(programId);

        ResponseModel subscriptionDateResponseModel = getInstructorProgramSubscriptionStats(date, programId);

        if (ValidationUtils.isEmptyString(date)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_DATE, MessageConstants.ERROR);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        String[] RequestDates = date.split("/");
        if (RequestDates.length == 1) {
            isYearDataNeeded = true;
            validationService.validateYear(date);
        } else if (RequestDates.length == 2) {
            validationService.validateMonth(RequestDates[0]);
            validationService.validateYear(RequestDates[1]);
        }
        log.info("Validate date : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_REVENUE_DATA_FETCHED);
        if (isYearDataNeeded) {
            YearSubscriptionsOfNewAndRenewalResponseView yearSubscriptionsOfNewAndRenewalResponseView = (YearSubscriptionsOfNewAndRenewalResponseView) subscriptionDateResponseModel.getPayload();
            YearlySubscriptionResponseView yearlySubscriptionResponseView = new YearlySubscriptionResponseView();
            List<YearlySubscriptionView> yearlySubscriptionViewList = new ArrayList<>();
            int totalSubscriptions = 0;
            for (YearSubscriptionsOfNewAndRenewalView yearSubscriptionsOfNewAndRenewalView : yearSubscriptionsOfNewAndRenewalResponseView.getChartEntryList()) {
                YearlySubscriptionView yearlySubscriptionView = new YearlySubscriptionView();
                yearlySubscriptionView.setMonthId(yearSubscriptionsOfNewAndRenewalView.getEntryId());
                yearlySubscriptionView.setMonthName(yearSubscriptionsOfNewAndRenewalView.getEntryName());
                int subscriptionCount;
                if (!isRenewDataNeeded) {
                    subscriptionCount = yearSubscriptionsOfNewAndRenewalView.getNewSubscriptionCount();
                } else {
                    subscriptionCount = yearSubscriptionsOfNewAndRenewalView.getRenewalSubscriptionCount();
                }
                yearlySubscriptionView.setMonthSubscriptionCount(subscriptionCount);
                totalSubscriptions += subscriptionCount;
                yearlySubscriptionViewList.add(yearlySubscriptionView);
            }
            yearlySubscriptionResponseView.setMonthlySubscriptionSplits(yearlySubscriptionViewList);
            yearlySubscriptionResponseView.setTotalYearlySubscriptions(totalSubscriptions);
            responseModel.setPayload(yearlySubscriptionResponseView);
            log.info("Get program subscription stats for year : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            MonthSubscriptionsOfNewAndRenewalResponseView monthSubscriptionsOfNewAndRenewalResponseView = (MonthSubscriptionsOfNewAndRenewalResponseView) subscriptionDateResponseModel.getPayload();
            MonthlySubscriptionResponseView monthlySubscriptionResponseView = new MonthlySubscriptionResponseView();
            List<MonthlySubscriptionView> monthlySubscriptionViewList = new ArrayList<>();

            int totalSubscriptions = 0;
            for (MonthSubscriptionsOfNewAndRenewalView monthSubscriptionsOfNewAndRenewalView : monthSubscriptionsOfNewAndRenewalResponseView.getChartEntryList()) {
                MonthlySubscriptionView monthlySubscriptionView = new MonthlySubscriptionView();
                monthlySubscriptionView.setWeekId(monthSubscriptionsOfNewAndRenewalView.getEntryId());
                monthlySubscriptionView.setWeekName(monthSubscriptionsOfNewAndRenewalView.getEntryName());
                monthlySubscriptionView.setWeekDuration(monthSubscriptionsOfNewAndRenewalView.getWeekDuration());
                int subscriptionCount;
                if (!isRenewDataNeeded) {
                    subscriptionCount = monthSubscriptionsOfNewAndRenewalView.getNewSubscriptionCount();
                } else {
                    subscriptionCount = monthSubscriptionsOfNewAndRenewalView.getRenewalSubscriptionCount();
                }
                monthlySubscriptionView.setSubscriptionCount(subscriptionCount);
                totalSubscriptions += subscriptionCount;

                monthlySubscriptionViewList.add(monthlySubscriptionView);
            }

            monthlySubscriptionResponseView.setSubscriptionsPerWeek(monthlySubscriptionViewList);
            monthlySubscriptionResponseView.setTotalMonthlySubscriptions(totalSubscriptions);

            responseModel.setPayload(monthlySubscriptionResponseView);
            log.info("Get program subscription stats for month : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program subscription stats of an instructor ends.");
        return responseModel;
    }

    /**
     * to get Program Analytics.
     *
     * @param programId the program id
     * @return ResponseModel
     */
    public ResponseModel getProgramAnalytics(long programId) {
        log.info("Get program analytics starts");
        long profilingStart = new Date().getTime();
        Programs program = validationService.validateProgramIdBlocked(programId);
        log.info("Program validation : time taken in millis : "+(new Date().getTime()-profilingStart));

        long activeSubscriptionCount = subscriptionService.getActiveSubscriptionCountOfProgram(program.getProgramId());

        BigDecimal rating = fitwiseUtils.getProgramRating(program.getProgramId());
        double revenue = 0.0;


        ProgramAnalyticsResponseView programAnalyticsResponse = new ProgramAnalyticsResponseView();
        long tempTime = new Date().getTime();
        revenue += programRevenueJpa.getAnalyticsProgramRevenue(programId);
        log.info("Time taken for querying : time taken in millis : "+(new Date().getTime()-tempTime));

        revenue = Math.round(revenue * 100.0) / 100.0;
        tempTime = new Date().getTime();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        programAnalyticsResponse.setProgramTitle(program.getTitle());
        programAnalyticsResponse.setRating(rating);
        programAnalyticsResponse.setRevenue(revenue);
        programAnalyticsResponse.setRevenueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(revenue));
        programAnalyticsResponse.setTotalSubscriptions((int) activeSubscriptionCount);
        log.info("Time taken for Response construction : time taken in millis : "+(new Date().getTime()-tempTime));
        log.info("Time taken by program analytics api: time taken in millis : "+(new Date().getTime()-profilingStart));
        log.info("Get program analytics stops");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, programAnalyticsResponse);
    }


    public ResponseModel getProgramClientDemographics(long programId, String graphBasis, String startDateString, String endDateString) {
        log.info("Get program client demographics starts.");
        long apiStartTimeMillis = new Date().getTime();

        validationService.validateProgramIdBlocked(programId);
        log.info("Validate program id is blocked or not : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        log.info("Get date range : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        HashMap<String, ProgramClientDemographicsGraph> graphMap = loadClientDemographicsGraph();

        ProgramClientDemographicsGraph programClientDemographicsGraph = graphMap.get(graphBasis);
        log.info("Load and get program client demographics graph : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        if (programClientDemographicsGraph == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_GRAPH_NOT_AVAILABLE, MessageConstants.ERROR);
        }
        ResponseModel responseModel = programClientDemographicsGraph.getProgramClientDemographics(programId, dateRange);
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program client demographics ends.");
        return responseModel;

    }

    private HashMap<String, ProgramClientDemographicsGraph> loadClientDemographicsGraph() {
        HashMap<String, ProgramClientDemographicsGraph> graphMap = new HashMap<>();
        graphMap.put("gender", programClientDemographicsByGender);
        graphMap.put("age", programClientDemographicsByAge);
        return graphMap;
    }

    public ResponseModel getSubscriptionByPlatform(String startDateString, String endDateString, long programId) {
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        List<PlatformType> platformTypeList = platformTypeRepository.findAll();
        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;
        for (PlatformType platformType : platformTypeList) {
            ChartEntry chartEntry = new ChartEntry();
            int count = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramIdAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, statusList, programId, platformType.getPlatformTypeId(), startDate, endDate);
            chartEntry.setEntryName(platFormDisplayName.getProperty(platformType.getPlatform(), platformType.getPlatform()));
            chartEntry.setCount(count);
            chartEntryList.add(chartEntry);
            totalCount += count;
        }
        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            List<String> chartEntryNames = platformTypeList.stream()
                    .map(platformType -> platFormDisplayName.getProperty(platformType.getPlatform().toLowerCase(), platformType.getPlatform()))
                    .collect(Collectors.toList());
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }
        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);
        ChartData chartData = new ChartData();
        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        return response;
    }

    /**
     * get discarded workout feedback stats based on following params.
     *
     * @param programId
     * @return
     */
    public ResponseModel getDiscardWorkoutFeedBackStats(Long programId, String startDateString, String endDateString) {
        log.info("Get discard workout feedback starts");
        long start = new Date().getTime();
        long profilingStart;
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Getting date range : time taken in millis : "+(new Date().getTime()-start));

        profilingStart = new Date().getTime();
        Programs program = validationService.validateProgramIdBlocked(programId);
        log.info("Program validation : time taken in millis : "+(new Date().getTime()-profilingStart));
        profilingStart = new Date().getTime();
        List<DiscardWorkoutReasons> discardWorkoutReasons = discardWorkoutReasonsRepository.findAll();
        log.info("Query : time taken in millis : "+(new Date().getTime()-profilingStart));
        List<Long> workoutScheduleIds = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
        profilingStart = new Date().getTime();
        List<DiscardWorkoutFeedbackStatsResponse> discardWorkoutFeedbackStatsResponses = new ArrayList<>();
        for (DiscardWorkoutReasons discardWorkoutReason : discardWorkoutReasons) {
            DiscardWorkoutFeedbackStatsResponse discardWorkoutFeedbackStatsResponse = new DiscardWorkoutFeedbackStatsResponse();
            discardWorkoutFeedbackStatsResponse.setDiscardReason(discardWorkoutReason.getDiscardReason());
            int discardFeedbackCount = workoutDiscardFeedbackRepository.countByWorkoutScheduleWorkoutScheduleIdInAndWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReasonAndCreatedDateBetween(workoutScheduleIds, discardWorkoutReason.getDiscardReason(), startDate, endDate);
            discardWorkoutFeedbackStatsResponse.setDiscardCout(discardFeedbackCount);
            discardWorkoutFeedbackStatsResponses.add(discardWorkoutFeedbackStatsResponse);
        }
        log.info("Response construction : time taken in millis : "+(new Date().getTime()-profilingStart));
        log.info("Getting Discard workout feedback : total time taken in millis : "+(new Date().getTime()-start));
        log.info("Get discard workout feedback ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DISCARD_WORKOUT_FEEDBACK_STATS, discardWorkoutFeedbackStatsResponses);
    }

    /**
     * get discarded workout feedback based on following params
     *
     * @param pageNo
     * @param pageSize
     * @param programId
     * @return
     */
    public ResponseModel getAllOtherDiscardWorkoutFeedBack(int pageNo, int pageSize, Long programId) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        Programs program = validationService.validateProgramIdBlocked(programId);
        List<Long> workoutScheduleIds = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
        List<DiscardWorkoutFeedbackResponse> discardWorkoutFeedbackResponses = new ArrayList<>();
        Page<WorkoutDiscardFeedback> workoutDiscardFeedbackPage =
                workoutDiscardFeedbackRepository.findByWorkoutScheduleWorkoutScheduleIdInAndWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReason(workoutScheduleIds, DBConstants.OTHERS, PageRequest.of(pageNo - 1, pageSize));
        for (WorkoutDiscardFeedback workoutDiscardFeedback : workoutDiscardFeedbackPage) {
            DiscardWorkoutFeedbackResponse discardWorkoutFeedbackResponse = new DiscardWorkoutFeedbackResponse();
            String customReason = "";
            for (WorkoutDiscardFeedbackMapping discardFeedbackMapping : workoutDiscardFeedback.getWorkoutDiscardFeedbackMapping()) {
                if (DBConstants.OTHERS.equals(discardFeedbackMapping.getDiscardWorkoutReason().getDiscardReason())) {
                    customReason = discardFeedbackMapping.getCustomReason();
                    break;
                }
            }
            discardWorkoutFeedbackResponse.setDescription(customReason);
            if (workoutDiscardFeedback.getUser() != null) {
                UserProfile userProfile = userProfileRepository.findByUserUserId(workoutDiscardFeedback.getUser().getUserId());
                discardWorkoutFeedbackResponse.setUserName(userProfile.getFirstName() + " " + userProfile.getLastName());
                if (userProfile.getProfileImage() != null) {
                    discardWorkoutFeedbackResponse.setImage(userProfile.getProfileImage().getImagePath());
                }
            } else {
                discardWorkoutFeedbackResponse.setUserName(KeyConstants.KEY_ANONYMOUS);
            }
            discardWorkoutFeedbackResponses.add(discardWorkoutFeedbackResponse);
        }

        if (workoutDiscardFeedbackPage.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, MessageConstants.ERROR);
        }
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_TOTAL_COUNT, workoutDiscardFeedbackPage.getTotalElements());
        response.put(KeyConstants.KEY_FEEDBACKS, discardWorkoutFeedbackResponses);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DISCARD_WORKOUT_FEEDBACK, response);
    }


    /**
     * get normal feedback for workouts of program based on following params.
     *
     * @param programId
     * @return
     */
    public ResponseModel getWorkoutNormalFeedback(Long programId, String startDateString, String endDateString) {
        log.info("Get workout normal feedback starts");
        long start = new Date().getTime();
        long profilingStart;

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));

        profilingStart = new Date().getTime();
        Programs program = validationService.validateProgramIdBlocked(programId);
        log.info("Validate program : Time taken in millis : "+(new Date().getTime() - profilingStart));
        List<Long> workoutScheduleIds = program.getWorkoutSchedules().stream().map(WorkoutSchedule::getWorkoutScheduleId).collect(Collectors.toList());
        profilingStart = new Date().getTime();
        List<FeedbackTypes> feedbackTypeList = feedbackTypesRepository.findAll();
        Collections.reverse(feedbackTypeList);
        log.info("Querying data and reversing : Time taken in millis : "+(new Date().getTime() - profilingStart));
        List<WorkoutNormalFeedbackResponse> workoutNormalFeedbackResponses = new ArrayList<>();
        profilingStart = new Date().getTime();
        for (FeedbackTypes feedbackType : feedbackTypeList) {
            int count = workoutFeedbackRepository.countByWorkoutScheduleWorkoutScheduleIdInAndFeedbackTypeFeedbackTypeAndCreatedDateBetween(workoutScheduleIds, feedbackType.getFeedbackType(), startDate, endDate);
            WorkoutNormalFeedbackResponse workoutNormalFeedbackResponse = new WorkoutNormalFeedbackResponse();
            workoutNormalFeedbackResponse.setFeedbackCount(count);
            workoutNormalFeedbackResponse.setFeedbackName(feedbackType.getFeedbackType());
            workoutNormalFeedbackResponses.add(workoutNormalFeedbackResponse);
        }
        log.info("Response construction : Time taken in millis : "+(new Date().getTime() - profilingStart));
        log.info("Get Workout normal feedback : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get workout normal feedback ends");

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_NORMAL_FEEDBACK, workoutNormalFeedbackResponses);
    }


    /**
     * get program stats of an instructor based on following params
     *
     * @param date
     * @param programId
     * @return
     * @throws ParseException
     */
    public ResponseModel getInstructorProgramSubscriptionStats(String date, Long programId) throws ParseException {
        log.info("Get instructor program subscription stats starts.");
        long apiStartTimeMillis = new Date().getTime();
        userComponents.getUser();
        boolean isYearDataNeeded = false;
        if (ValidationUtils.isEmptyString(date)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_DATE, MessageConstants.ERROR);
        }
        validationService.validateProgramIdBlocked(programId);
        String[] RequestDates = date.split("/");
        if (RequestDates.length == 1) {
            isYearDataNeeded = true;
            validationService.validateYear(date);
        } else if (RequestDates.length == 2) {
            validationService.validateMonth(RequestDates[0]);
            validationService.validateYear(RequestDates[1]);
        }
        log.info(StringConstants.LOG_FIELD_VALIDATION + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_INSTRUCTOR_PROGRAM_SUBSCRIPTION_COUNT_DATA_FETCHED);
        List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        if (isYearDataNeeded) {
            List<YearSubscriptionsOfNewAndRenewalView> yearSubscriptionsOfNewAndRenewalViews = new ArrayList<>();
            YearSubscriptionsOfNewAndRenewalResponseView yearSubscriptionsOfNewAndRenewalResponseView = new YearSubscriptionsOfNewAndRenewalResponseView();
            int totalCountForYearOfNew = 0;
            int totalCountForYearOfRenewal = 0;
            int month = 1;
            int year = Integer.parseInt(RequestDates[0]);
            for (int i = 0; i < 12; i++) {
                int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year, month);
                LocalDateTime startLocalDate = LocalDateTime.of(year, month, 1, 0, 0, 0);
                LocalDateTime endLocalDate = LocalDateTime.of(year, month, noOfDaysInTheMonth, 23, 59, 59);
                Date startDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                month++;
                /*
                 * YearlyRevenueView - Per month view
                 */
                YearSubscriptionsOfNewAndRenewalView yearSubscriptionsOfNewAndRenewalView = new YearSubscriptionsOfNewAndRenewalView();
                yearSubscriptionsOfNewAndRenewalView.setEntryId(i + 1);
                yearSubscriptionsOfNewAndRenewalView.setEntryName(ValidationUtils.months[i]);
                int subscriptionCountForRenewal = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_RENEWAL, programId, statusList);
                int subscriptionCountForNew = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_NEW, programId, statusList);
                yearSubscriptionsOfNewAndRenewalView.setNewSubscriptionCount(subscriptionCountForNew);
                yearSubscriptionsOfNewAndRenewalView.setRenewalSubscriptionCount(subscriptionCountForRenewal);
                totalCountForYearOfNew = totalCountForYearOfNew + subscriptionCountForNew;
                totalCountForYearOfRenewal = totalCountForYearOfRenewal + subscriptionCountForRenewal;
                yearSubscriptionsOfNewAndRenewalViews.add(yearSubscriptionsOfNewAndRenewalView);
            }
            yearSubscriptionsOfNewAndRenewalResponseView.setTotalCount(totalCountForYearOfNew + totalCountForYearOfRenewal);
            yearSubscriptionsOfNewAndRenewalResponseView.setChartEntryList(yearSubscriptionsOfNewAndRenewalViews);
            responseModel.setPayload(yearSubscriptionsOfNewAndRenewalResponseView);
            log.info("Construct year subscription of new and renewal response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        } else {
            List<MonthSubscriptionsOfNewAndRenewalView> monthSubscriptionsOfNewAndRenewalViews = new ArrayList<>();
            MonthSubscriptionsOfNewAndRenewalResponseView monthSubscriptionsOfNewAndRenewalResponseView = new MonthSubscriptionsOfNewAndRenewalResponseView();
            int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(Integer.parseInt(RequestDates[1]), Integer.parseInt(RequestDates[0]));
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
            int firstWeekStartDate = 0;
            int firstWeekEndDate = 0;
            int secondWeekStartDate = 0;
            int secondWeekEndDate = 0;
            int thirdWeekStartDate = 0;
            int thirdWeekEndDate = 0;
            int fourthWeekStartDate = 0;
            int fourthWeekEndDate = 0;
            /*
             * Setting the date limits to the weeks by parsing through the weekSplit array
             */
            for (int i = 0; i < weekSplit.length; i++) {
                if (i == 0) {
                    String dayLimit = weekSplit[i];
                    String[] dates = dayLimit.split("-");
                    firstWeekStartDate = Integer.parseInt(dates[0]);
                    firstWeekEndDate = Integer.parseInt(dates[1]);
                } else if (i == 1) {
                    String dayLimit = weekSplit[i];
                    String[] dates = dayLimit.split("-");
                    secondWeekStartDate = Integer.parseInt(dates[0]);
                    secondWeekEndDate = Integer.parseInt(dates[1]);
                } else if (i == 2) {
                    String dayLimit = weekSplit[i];
                    String[] dates = dayLimit.split("-");
                    thirdWeekStartDate = Integer.parseInt(dates[0]);
                    thirdWeekEndDate = Integer.parseInt(dates[1]);
                } else if (i == 3) {
                    String dayLimit = weekSplit[i];
                    String[] dates = dayLimit.split("-");
                    fourthWeekStartDate = Integer.parseInt(dates[0]);
                    fourthWeekEndDate = Integer.parseInt(dates[1]);
                }
            }
            int[] startEndDateArrays = {firstWeekStartDate, firstWeekEndDate, secondWeekStartDate,
                    secondWeekEndDate, thirdWeekStartDate, thirdWeekEndDate, fourthWeekStartDate, fourthWeekEndDate};
            int count = 0;
            int totalCountForMonthOfNew = 0;
            int totalCountForMonthOfRenewal = 0;
            for (int i = 0; i < 4; i++) {
                LocalDateTime startLocalDate = LocalDateTime.of(Integer.parseInt(RequestDates[1]), Integer.parseInt(RequestDates[0]), startEndDateArrays[count], 0, 0, 0);
                LocalDateTime endLocalDate = LocalDateTime.of(Integer.parseInt(RequestDates[1]), Integer.parseInt(RequestDates[0]), startEndDateArrays[count + 1], 23, 59, 59);
                Date startDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                count = count + 2;
                /*
                 * Per week view
                 */
                MonthSubscriptionsOfNewAndRenewalView monthSubscriptionsOfNewAndRenewalView = new MonthSubscriptionsOfNewAndRenewalView();
                monthSubscriptionsOfNewAndRenewalView.setEntryId(i + 1);
                int subscriptionCountOfRenewal = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_RENEWAL, programId, statusList);
                int subscriptionCountOfNew = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_NEW, programId, statusList);
                monthSubscriptionsOfNewAndRenewalView.setNewSubscriptionCount(subscriptionCountOfNew);
                monthSubscriptionsOfNewAndRenewalView.setRenewalSubscriptionCount(subscriptionCountOfRenewal);
                totalCountForMonthOfNew = totalCountForMonthOfNew + subscriptionCountOfNew;
                totalCountForMonthOfRenewal = totalCountForMonthOfRenewal + subscriptionCountOfRenewal;
                monthSubscriptionsOfNewAndRenewalView.setEntryName(ValidationUtils.weeks[i]);
                monthSubscriptionsOfNewAndRenewalView.setWeekDuration(weekSplit[i]);
                monthSubscriptionsOfNewAndRenewalViews.add(monthSubscriptionsOfNewAndRenewalView);
            }
            monthSubscriptionsOfNewAndRenewalResponseView.setTotalCount(totalCountForMonthOfNew + totalCountForMonthOfRenewal);
            monthSubscriptionsOfNewAndRenewalResponseView.setChartEntryList(monthSubscriptionsOfNewAndRenewalViews);
            responseModel.setPayload(monthSubscriptionsOfNewAndRenewalResponseView);
            log.info("Construct month subscription of new and renewal response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get instructor program subscription stats ends.");
        return responseModel;
    }

    /**
     * get client acquisition funnel based on following params.
     *
     * @param programId
     * @return
     */
    public ResponseModel getClientAcquisition(Long programId, String startDateString, String endDateString) {
        log.info("Get client acquisition starts");
        long start = new Date().getTime();
        long profilingStart = new Date().getTime();
        validationService.validateProgramIdBlocked(programId);
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Program validation and Getting date range : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        long programViewsCount = programViewsAuditJPA.getUniqueUsersForProgramBetweenDate(programId, startDate, endDate);
        log.info("Unique program view count : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        long programPromoCompletionCount = programPromoViewsJPA.getUniqueUsersForProgramBetweenDate(programId, startDate, endDate);
        log.info("Unique promo completion count : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        List<String> statusList = Arrays.asList(KeyConstants.KEY_TRIAL);
        long programTrailCount = subscriptionAuditJPA.getUniqueUsersForProgramSubscriptionBetweenDate(KeyConstants.KEY_PROGRAM, statusList, KeyConstants.KEY_NEW, programId, startDate, endDate);
        log.info("Unique program trial count : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
        long subscriptionCount = subscriptionAuditJPA.getUniqueUsersForProgramSubscriptionBetweenDate(KeyConstants.KEY_PROGRAM, statusList, KeyConstants.KEY_NEW, programId, startDate, endDate);
        log.info("Unique program subscription count : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        long renewalSubscriptionCount = subscriptionAuditJPA.getUniqueUsersForProgramSubscriptionBetweenDate(KeyConstants.KEY_PROGRAM, statusList, KeyConstants.KEY_RENEWAL, programId, startDate, endDate);
        log.info("Unique program renewal count : Time taken in millis : " + (new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();
        ClientAcquisitionViewResponse clientAcquisitionViewResponse = new ClientAcquisitionViewResponse();
        clientAcquisitionViewResponse.setProgramVisits(Math.toIntExact(programViewsCount));
        clientAcquisitionViewResponse.setPromoViews(Math.toIntExact(programPromoCompletionCount));
        clientAcquisitionViewResponse.setTrails(Math.toIntExact(programTrailCount));
        clientAcquisitionViewResponse.setSubscribers(Math.toIntExact(subscriptionCount));
        clientAcquisitionViewResponse.setReSubscribers(Math.toIntExact(renewalSubscriptionCount));
        log.info("Client acquisition response construction : Time taken in millis : " + (new Date().getTime() - profilingStart));
        log.info("Get client acquisition : Total Time taken in millis : " + (new Date().getTime() - start));
        log.info("Get client acquisition ends");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CLIENT_ACQUISITION_FUNNEL, clientAcquisitionViewResponse);
    }

    /**
     * get client acquisition users based on following params.
     *
     * @param pageNo
     * @param pageSize
     * @param programId
     * @param clientAcquisitionKey
     * @return
     */
    public ResponseModel getClientAcquisitionUsers(final int pageNo, final int pageSize, Long programId, String clientAcquisitionKey, String startDateString, String endDateString) {
        log.info("Client acquisition starts");
        long start = new Date().getTime();
        long profilingStart;
        long profilingEnd;

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        PageRequest pageRequest = PageRequest.of(pageNo-1,pageSize);

        if (!(clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_PROGRAM_VISITS) || clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_PROMO_VIEWS) ||
                clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_TRAILS) || clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_SUBSCRIBERS) ||
                clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_RE_SUBSCRIBERS))) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_WRONG_ACQUISITION_KEY, null);
        }
        validationService.validateProgramIdBlocked(programId);
        List<ClientAcquisitionUsersResponse> clientAcquisitionUsersResponses = null;
        long totalCount;
        profilingStart = new Date().getTime();
        Specification<UserProfile> finalSpec = UserProfileSpecifications.getUserProfilesOrderByName(SearchConstants.ORDER_ASC);
        if (clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_PROGRAM_VISITS)) {
            Specification<UserProfile> profileSpec = UserProfileSpecifications.getProgramViewsGroupByUsers(programId,startDate,endDate);
            finalSpec = finalSpec.and(profileSpec);
        } else if (clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_PROMO_VIEWS)) {
            Specification<UserProfile> promoViewsSpecification = UserProfileSpecifications.getPromoViewsGroupByUsers(programId,startDate,endDate);
            finalSpec = finalSpec.and(promoViewsSpecification);
        } else if (clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_TRAILS)) {
                List<String> statusList = Arrays.asList(KeyConstants.KEY_TRIAL);
                Specification<UserProfile> subscriptionAuditSpec = UserProfileSpecifications.geSubscriptionAuditsForProgramByRenewalStatusAndBetweenAndGroupByUsers(
                        programId,KeyConstants.KEY_PROGRAM,KeyConstants.KEY_NEW,statusList,startDate,endDate);
                finalSpec = finalSpec.and(subscriptionAuditSpec);
        } else if (clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_SUBSCRIBERS)) {
                List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
                Specification<UserProfile> subscriptionAuditSpec = UserProfileSpecifications.geSubscriptionAuditsForProgramByRenewalStatusAndBetweenAndGroupByUsers(
                        programId,KeyConstants.KEY_PROGRAM,KeyConstants.KEY_NEW,statusList,startDate,endDate);
                finalSpec = finalSpec.and(subscriptionAuditSpec);
        } else if (clientAcquisitionKey.equalsIgnoreCase(KeyConstants.KEY_RE_SUBSCRIBERS)) {
                List<String> statusList = Arrays.asList(KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING);
                Specification<UserProfile> subscriptionAuditSpec = UserProfileSpecifications.geSubscriptionAuditsForProgramByRenewalStatusAndBetweenAndGroupByUsers(
                        programId,KeyConstants.KEY_PROGRAM,KeyConstants.KEY_RENEWAL,statusList,startDate,endDate);
                finalSpec = finalSpec.and(subscriptionAuditSpec);
        }
        Page<UserProfile> userProfilePage = userProfileRepository.findAll(finalSpec,pageRequest);
        profilingEnd = new Date().getTime();
        log.info("Client acquisition query : time taken in millis : "+(profilingEnd-profilingStart));
        totalCount = userProfilePage.getTotalElements();
        if (userProfilePage.isEmpty()) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CLIENT_ACQUISITION_USERS, new ArrayList<>());
        }
        profilingStart = new Date().getTime();
        clientAcquisitionUsersResponses = getClientAcquisitionUsers(userProfilePage);
        profilingEnd = new Date().getTime();
        log.info("Client acquisition response construction : time taken in millis : "+(profilingEnd-profilingStart));
        Map<String, Object> map = new HashMap<>();
        map.put(KeyConstants.KEY_TOTAL_COUNT, totalCount);
        map.put(KeyConstants.KEY_PROGRAM_VIEWED_USERS, clientAcquisitionUsersResponses);
        ResponseModel responseModel = new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_CLIENT_ACQUISITION_USERS, map);
        profilingEnd = new Date().getTime();
        log.info("Client acquisition  overall time taken in millis : "+(profilingEnd-start));
        log.info("Client acquisition ends");
        return responseModel;
    }

    public List<ClientAcquisitionUsersResponse> getClientAcquisitionUsers(Page<UserProfile> userProfiles) {
        List<ClientAcquisitionUsersResponse> clientAcquisitionUsersResponses = new ArrayList<>();
        for (UserProfile userProfile : userProfiles) {
            ClientAcquisitionUsersResponse clientAcquisitionUsersResponse = new ClientAcquisitionUsersResponse();
            clientAcquisitionUsersResponse.setUserId(userProfile.getUser().getUserId());
            clientAcquisitionUsersResponse.setUserName(userProfile.getFirstName() + " " + userProfile.getLastName());
            try {
                clientAcquisitionUsersResponse.setImageUrl(userProfile.getProfileImage().getImagePath());
            } catch (Exception exception) {
                clientAcquisitionUsersResponse.setImageUrl(null);
            }
            clientAcquisitionUsersResponses.add(clientAcquisitionUsersResponse);
        }
        return clientAcquisitionUsersResponses;
    }

    /**
     * Get revenu data for year -for a program
     *
     * @param startDateString
     * @param endDateString
     * @param programId
     * @return
     * @throws ParseException
     */
    public ResponseModel getRevenueDataForYear(String startDateString, String endDateString, long programId) throws ParseException {
        log.info("Get revenue Data for year starts");
        long start = new Date().getTime();
        List<SplitGraphEntryWithValue<Double>> splitGraphEntryList = new ArrayList<>();
        validationService.validateProgramIdBlocked(programId);
        log.info("Time taken for validation: Time taken in millis : " + (new Date().getTime() - start));
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        userComponents.getUser();
        double totalRevenue = 0.0;
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Period diff = Period.between(startLocalDate, endLocalDate);
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        int years = diff.getYears();
        int noOfMonths = diff.getMonths();
        noOfMonths = noOfMonths + (years * 12);
        int month = 1;
        int year = Integer.parseInt(startDateString);
        long temp = new Date().getTime();
        //Getting each month's last date data
        for (int i = 0; i <= noOfMonths; i++) {
            int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year, month);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat(StringConstants.PATTERN_DATE_YYYY_MM_DD);
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
            double newAmount = 0;
            double renewAmount = 0;
            double totalAmount = newAmount + renewAmount;
            long temp1 = new Date().getTime();
            newAmount += programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_NEW,startTime,endTime);
            newAmount = Math.round(newAmount * 100.0) / 100.0;
            totalAmount += newAmount;
            renewAmount += programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_RENEWAL,startTime,endTime);
            renewAmount = Math.round(renewAmount * 100.0) / 100.0;
            totalAmount += renewAmount;
            log.info("Time taken for Querying: Time taken in millis : " + (new Date().getTime() - temp1));
            SplitGraphEntryWithValue<Double> splitGraphEntry = new SplitGraphEntryWithValue<>();
            splitGraphEntry.setPeriodId(i + 1);
            String monthName = ValidationUtils.months[i];
            splitGraphEntry.setPeriodName(monthName);
            splitGraphEntry.setFirstEntryName("New");
            splitGraphEntry.setFirstEntryValue(newAmount);
            splitGraphEntry.setFirstValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(newAmount));
            splitGraphEntry.setSecondEntryName("Renew");
            splitGraphEntry.setSecondEntryValue(renewAmount);
            splitGraphEntry.setSecondValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(renewAmount));
            splitGraphEntry.setTotalValue(totalAmount);
            splitGraphEntry.setTotalValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalAmount));
            if (totalAmount != 0.0) {
                String firstEntryPercentString = decimalFormat.format(newAmount * 100 / totalAmount);
                float firstEntryPercent = Float.parseFloat(firstEntryPercentString);
                String secondEntryPercentString = decimalFormat.format(renewAmount * 100 / totalAmount);
                float secondEntryPercent = Float.parseFloat(secondEntryPercentString);
                splitGraphEntry.setFirstEntryPercent(firstEntryPercent);
                splitGraphEntry.setSecondEntryPercent(secondEntryPercent);
            }
            splitGraphEntryList.add(splitGraphEntry);
            totalRevenue += totalAmount;
        }
        SplitGraphDataWithValue<Double> splitGraphData = new SplitGraphDataWithValue<>();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        splitGraphData.setOverAllValue(totalRevenue);
        splitGraphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));
        splitGraphData.setGraphEntryList(splitGraphEntryList);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(splitGraphData);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - temp));
        log.info("Get revenue Data for Year api : Time taken in millis : " + (new Date().getTime() - start));
        log.info("Get revenue Data for year stops");
        return response;
    }

    public ResponseModel getRevenueDataForMonth(String startDateString, String endDateString, long programId) {
        log.info("Get revenue Data for month starts");
        long start = new Date().getTime();
        
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        validationService.validateProgramIdBlocked(programId);

        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        userComponents.getUser();
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        Period diff = Period.between(startLocalDate, endLocalDate);

        if (diff.getYears() > 0 || diff.getMonths() > 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_PROVIDE_DATES_WITHIN_SAME_MONTH, null);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        int noOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String[] weekSplit = ValidationUtils.getWeekSplit(noOfDaysInMonth);
        Date tempDate = new Date(startDate.getTime());
        List<SplitGraphEntryWithValue<Double>> splitGraphEntryList = new ArrayList<>();
        double overAllValue = 0.0;
        //Getting each week's last date data
        long temp = new Date().getTime();
        for (int i = 0; i < weekSplit.length; i++) {
            String[] week = weekSplit[i].split("-");
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
            double newAmount = 0.0;
            double renewAmount = 0.0;
            double totalAmount = newAmount + renewAmount;
            long temp1 = new Date().getTime();
            newAmount += programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_NEW,startTime,endTime);
            newAmount = Math.round(newAmount * 100.0) / 100.0;
            totalAmount += newAmount;
            renewAmount += programRevenueJpa.getAnalyticsProgramRevenue(programId,KeyConstants.KEY_RENEWAL,startTime,endTime);
            renewAmount = Math.round(renewAmount * 100.0) / 100.0;
            totalAmount += renewAmount;
            log.info("Time taken for Querying: Time taken in millis : " + (new Date().getTime() - temp1));
            SplitGraphEntryWithValue<Double> splitGraphEntry = new SplitGraphEntryWithValue<>();
            splitGraphEntry.setPeriodId(i + 1);
            splitGraphEntry.setPeriodName("W" + (i + 1));
            splitGraphEntry.setFirstEntryName("New");
            splitGraphEntry.setFirstEntryValue(newAmount);
            splitGraphEntry.setFirstValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(newAmount));
            splitGraphEntry.setSecondEntryName("Renew");
            splitGraphEntry.setSecondEntryValue(renewAmount);
            splitGraphEntry.setSecondValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(renewAmount));
            splitGraphEntry.setTotalValue(totalAmount);
            splitGraphEntry.setTotalValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalAmount));
            if (totalAmount != 0.0) {
                String firstEntryPercentString = decimalFormat.format(newAmount * 100 / totalAmount);
                float firstEntryPercent = Float.parseFloat(firstEntryPercentString);
                String secondEntryPercentString = decimalFormat.format(renewAmount * 100 / totalAmount);
                float secondEntryPercent = Float.parseFloat(secondEntryPercentString);
                splitGraphEntry.setFirstEntryPercent(firstEntryPercent);
                splitGraphEntry.setSecondEntryPercent(secondEntryPercent);
            }
            splitGraphEntryList.add(splitGraphEntry);
            overAllValue += totalAmount;
        }
        SplitGraphDataWithValue<Double> splitGraphData = new SplitGraphDataWithValue<>();
        overAllValue = Math.round(overAllValue * 100.0) / 100.0;
        splitGraphData.setOverAllValue(overAllValue);
        splitGraphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(overAllValue));
        splitGraphData.setGraphEntryList(splitGraphEntryList);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(splitGraphData);
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - temp));
        log.info("Get revenue Data for month api : Time taken in millis : " + (new Date().getTime() - start));
        log.info("Get revenue Data for month stops");
        return response;
    }

    public ResponseModel populateHistoryDataForSubscriptionAudit() {
        List<SubscriptionAudit> subscriptionAudits = subscriptionAuditRepo.findAll();
        for (SubscriptionAudit subscriptionAudit : subscriptionAudits) {
            if (subscriptionAudit.getProgramSubscriptionPaymentHistory() == null) {
                ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
                PlatformWiseTaxDetail platformWiseTaxDetail = platformWiseTaxDetailRepository.findByActiveAndPlatformType(true, subscriptionAudit.getProgramSubscription().getSubscribedViaPlatform());
                double taxAmount;
                if (subscriptionAudit.getProgramSubscription().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.ANDROID) || subscriptionAudit.getProgramSubscription().getSubscribedViaPlatform().getPlatform().equalsIgnoreCase(DBConstants.WEB)) {
                    double creditCardTax = platformWiseTaxDetail.getCreditCardTaxPercentage();
                    double creditCardFixedCharge = platformWiseTaxDetail.getCreditCardFixedCharges();
                    taxAmount = ((creditCardTax / 100) * subscriptionAudit.getProgramSubscription().getProgram().getProgramPrices().getPrice()) + creditCardFixedCharge;
                    programSubscriptionPaymentHistory.setTaxCharges(Math.round(taxAmount * 100.0) / 100.0);
                } else {
                    double appStoreTax = platformWiseTaxDetail.getAppStoreTaxPercentage();
                    taxAmount = (appStoreTax / 100) * subscriptionAudit.getProgramSubscription().getProgram().getProgramPrices().getPrice();
                    programSubscriptionPaymentHistory.setTaxCharges(Math.round(taxAmount * 100.0) / 100.0);
                }
                User user = null;
                String subscriptionType = "";
                double trainnrTax = 0;
                if(subscriptionAudit.getSubscriptionType().getName().equalsIgnoreCase(KeyConstants.KEY_PROGRAM)){
                    InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(user, true);
                    trainnrTax = 15.0;
                    if (instructorTierDetails != null) {
                        trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getProgramsFees();
                    }
                    user = subscriptionAudit.getProgramSubscription().getProgram().getOwner();
                    subscriptionType = KeyConstants.KEY_PROGRAM;
                }else if(subscriptionAudit.getSubscriptionType().getName().equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR)){
                    user = subscriptionAudit.getInstructorSubscription().getInstructor();
                    subscriptionType = KeyConstants.KEY_INSTRUCTOR;
                }else if(subscriptionAudit.getSubscriptionType().getName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_PACKAGE)){
                    InstructorTierDetails instructorTierDetails = instructorTierDetailsRepository.findByUserAndActive(user, true);
                    trainnrTax = 15.0;
                    if (instructorTierDetails != null) {
                        trainnrTax = instructorTierDetails.getTier().getTierTypeDetails().getPackagesFees();
                    }
                    user = subscriptionAudit.getPackageSubscription().getSubscriptionPackage().getOwner();
                    subscriptionType = KeyConstants.KEY_SUBSCRIPTION_PACKAGE;
                }
                double trainnrTaxAmount = (trainnrTax / 100) * subscriptionAudit.getProgramSubscription().getProgram().getProgramPrices().getPrice();
                programSubscriptionPaymentHistory.setTrainnrRevenue(Math.round(trainnrTaxAmount * 100.0) / 100.0);
                double instructorShare = subscriptionAudit.getProgramSubscription().getProgram().getProgramPrices().getPrice() - (taxAmount + trainnrTaxAmount);
                programSubscriptionPaymentHistory.setProgramPrice(subscriptionAudit.getProgramSubscription().getProgram().getProgramPrices().getPrice());
                programSubscriptionPaymentHistory.setInstructorShare(Math.round(instructorShare * 100.0) / 100.0);
                subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);


                subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
                subscriptionAuditRepo.save(subscriptionAudit);
            }
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, null);

    }

    public ResponseModel getProgramRevenueByPlatform(long programId, String startDateString, String endDateString) {
        log.info("Get program revenue by platform starts");
        long start = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        userComponents.getUser();
        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);
        List<ChartEntryWithValue<Double>> chartEntryList = new ArrayList<>();
        double totalInstructorShare = 0.0;
        long temp = new Date().getTime();
        List<RevenueByPlatform> revenueByPlatformList = programRevenueJpa.getProgramRevenue(programId,startDate,endDate);
        log.info("Time taken for Querying: Time taken in millis : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        for(RevenueByPlatform revenueByPlatform : revenueByPlatformList){
            double instructorShare; 
            if(revenueByPlatform!=null && revenueByPlatform.getRevenue()!=null){
                instructorShare = revenueByPlatform.getRevenue();
            }else{
                instructorShare = 0;
            }
            String platformTypeName = revenueByPlatform.getPlatformName();
            ChartEntryWithValue<Double> chartEntry = new ChartEntryWithValue<>();
            chartEntry.setEntryName(platFormDisplayName.getProperty(platformTypeName, platformTypeName));
            instructorShare = Math.round(instructorShare * 100.0) / 100.0;
            chartEntry.setValue(instructorShare);
            chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorShare));
            totalInstructorShare += instructorShare;

            chartEntryList.add(chartEntry);
        }

        new ChartDataUtils().populationEntryPercentages(chartEntryList, totalInstructorShare);

        ChartDataWithValue<Double> chartData = new ChartDataWithValue<>();
        totalInstructorShare = Math.round(totalInstructorShare * 100.0) / 100.0;
        chartData.setTotalValue(totalInstructorShare);
        chartData.setFormattedTotalValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalInstructorShare));
        chartData.setChartEntryList(chartEntryList);


        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Time takn for response construction: Time taken in millis : " + (new Date().getTime() - temp));
        
        log.info("Get program revenue by platform API: Total Time taken in millis : " + (new Date().getTime() - start));
        log.info("Get program revenue by platform stops");
        return response;

    }

}

package com.fitwise.service.instructor;

import com.fitwise.components.UserComponents;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.PaymentConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.constants.payments.appleiap.NotificationConstants;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.ProgramRating;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.exception.ApplicationException;
import com.fitwise.instructor.graph.ClientDemographicsByAge;
import com.fitwise.instructor.graph.ClientDemographicsByExpertise;
import com.fitwise.instructor.graph.ClientDemographicsByGender;
import com.fitwise.instructor.graph.ClientDemographicsGraph;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.ProgramRatingRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.repository.payments.stripe.connect.StripeAccountAndUserMappingRepository;
import com.fitwise.repository.qbo.QboVendorBillPaymentRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.service.SubscriptionService;
import com.fitwise.service.qbo.QBOService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.jpa.InstructorPaymentJPA;
import com.fitwise.specifications.jpa.InstructorRevenueJPA;
import com.fitwise.specifications.jpa.PlatformTypeJPA;
import com.fitwise.specifications.jpa.ProgramRatingJPA;
import com.fitwise.specifications.jpa.QboVendorBillPaymentJPA;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.specifications.jpa.dao.RevenueByPlatform;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.GraphUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.InstructorProgramsPerformanceViewResponse;
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
import com.fitwise.view.instructor.InstructorAnalyticsResponseView;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Balance;
import com.stripe.net.RequestOptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorAnalyticsService {

    /**
     * The user components.
     */
    @Autowired
    private UserComponents userComponents;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    ValidationService validationService;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    ClientDemographicsByGender clientDemographicsByGender;

    @Autowired
    ClientDemographicsByAge clientDemographicsByAge;

    @Autowired
    ClientDemographicsByExpertise clientDemographicsByExpertise;

    @Autowired
    PlatformTypeRepository platformTypeRepository;

    @Autowired
    ProgramRatingRepository programRatingRepository;

    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    GraphUtils graphUtils;

    @Autowired
    private QboVendorBillPaymentRepository qboVendorBillPaymentRepository;

    @Autowired
    private OrderManagementRepository orderManagementRepository;

    @Autowired
    private InstructorPaymentRepository instructorPaymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StripeAccountAndUserMappingRepository stripeAccountAndUserMappingRepository;

    @Autowired
    private StripeProperties stripeProperties;

    @Autowired
    StripePaymentRepository stripePaymentRepository;

    @Autowired
    private SubscriptionAuditJPA subscriptionAuditJPA;

    @Autowired
    private QBOService qboService;


    private final PlatformTypeJPA platformTypeJPA;
    private final InstructorPaymentJPA instructorPaymentJPA;
    private final InstructorRevenueJPA instructorRevenueJPA;
    private final QboVendorBillPaymentJPA qboVendorBillPaymentJPA;
    private final ProgramRatingJPA programRatingJPA;


    String[] dummyWeekDuration = {"1-7", "8-14", "15-21", "22-31"};
    int[] dummyWeekRevenueData = {0, 0, 0, 0};
    int[] dummyWeekRevenueData2 = {0, 0, 0, 0};
    int[] dummyYearRevenueData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] dummyYearRevenueData2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] dummyWeeklySubscriptionCountData = {40, 50, 10, 100};
    int[] dummyWeeklySubscriptionCountData2 = {10, 30, 100, 80};
    int[] dummyYearlySubscriptionCountData = {40, 50, 10, 100, 40, 50, 10, 100, 40, 50, 10, 100};
    int[] dummyYearlySubscriptionCountData2 = {10, 80, 50, 100, 10, 90, 40, 37, 24, 80, 86, 50};

    /**
     * Method to get instructor stats overview
     *
     * @return
     */
/*    public ResponseModel getInstructorStatsOverview() {

        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());

        OverviewInstructorStatsResponseView statsResponseView = new OverviewInstructorStatsResponseView();
        statsResponseView.setUserName(userProfile.getFirstName() + " " + userProfile.getLastName());

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OVERVIEW_STATS_FETCHED, statsResponseView);
    }*/

    /**
     * Method used to get the top header data in instructor app
     *
     * @return
     */
  /*  public ResponseModel getProgramStatsOverviewOfAnInstructor() {

        User user = userComponents.getUser();
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());

        OverviewInstructorProgramStatsResponseView statsResponseView = new OverviewInstructorProgramStatsResponseView();
        statsResponseView.setUserName(userProfile.getFirstName() + " " + userProfile.getLastName());

        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_OVERVIEW_STATS_FETCHED, statsResponseView);
    }*/


    /**
     * Method used to get the instructor revenue data.
     *
     * @param date
     * @param isRenewDataNeeded
     * @return
     */
    public ResponseModel getInstructorRevenueData(String date, boolean isRenewDataNeeded, Long userId) throws ParseException {
        log.info("Get instructor revenue data starts");
        long apiStartTimeMillis = new Date().getTime(); 
        boolean isYearDataNeeded = false;

        User user = getUser(userId);

        /*
         * YearlyRevenueResponseView - Splitted by means of months (Jan to Dec)
         */
        YearlyRevenueResponseView yearlyRevenueResponseView = new YearlyRevenueResponseView();

        double totalYearlyRevenue = 0.0;
        double totalMonthlyRevenue = 0.0;


        /*
         * YearlyRevenueResponseView - Splitted by means of weeks (W1 to W4)
         */
        MonthlyRevenueResponseView monthlyRevenueResponseView = new MonthlyRevenueResponseView();
        monthlyRevenueResponseView.setTotalMonthlyRevue("$0.00");

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        if (ValidationUtils.isEmptyString(date)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_DATE, MessageConstants.ERROR);
        }

        String RequestDates[] = date.split("/");

        if (RequestDates.length == 1) {
            isYearDataNeeded = true;
            validationService.validateYear(date);

        } else if (RequestDates.length == 2) {
            isYearDataNeeded = false;
            validationService.validateMonth(RequestDates[0]);
            validationService.validateYear(RequestDates[1]);
        }
        log.info("Validation time : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long temp = new Date().getTime();
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


                double newAmount = 0;
                double renewAmount = 0;
                double totalAmount = newAmount + renewAmount;

                /*
                 * YearlyRevenueView - Per month view
                 */
                YearlyRevenueView yearlyRevenueView = new YearlyRevenueView();
                yearlyRevenueView.setMonthId(i + 1);
                yearlyRevenueView.setMonthName(ValidationUtils.months[i]);
                if (isRenewDataNeeded == true) {
                    renewAmount += instructorRevenueJPA.getInstructorRevenue(startTime, endTime, user.getUserId(), KeyConstants.KEY_RENEWAL, statusList);
                    renewAmount = Math.round(renewAmount * 100.0) / 100.0;
                    totalYearlyRevenue += renewAmount;
                    yearlyRevenueView.setMonthRevenue(renewAmount);
                    yearlyRevenueView.setFormattedMonthRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(renewAmount));
                } else {
                    newAmount += instructorRevenueJPA.getInstructorRevenue(startTime, endTime, user.getUserId(), KeyConstants.KEY_NEW, statusList);
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
        } else {
            List<MonthlyRevenueView> revenueViews = new ArrayList<>();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date tempDate = formatter.parse(Integer.parseInt(RequestDates[1]) + "-" + Integer.parseInt(RequestDates[0]) + "-" + "1");
            Calendar cal = Calendar.getInstance();
            cal.setTime(tempDate);
            int noOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            String[] weekSplit = ValidationUtils.getWeekSplit(noOfDaysInMonth);

            /*
             * Setting the date limits to the weeks by parsing through the weekSplit array
             */
            for (int i = 0; i < weekSplit.length; i++) {
                String week[] = weekSplit[i].split("-");

                MonthlyRevenueView monthlyRevenueView = new MonthlyRevenueView();
               // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                Date startDate = formatter.parse(Integer.parseInt(RequestDates[1]) + "-" + Integer.parseInt(RequestDates[0]) + "-" + week[0]);
                Date endDate = formatter.parse(Integer.parseInt(RequestDates[1]) + "-" + Integer.parseInt(RequestDates[0]) + "-" + week[week.length - 1]);


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
                double totalAmount = newAmount + renewAmount;

                if (isRenewDataNeeded == true) {
                    renewAmount += instructorRevenueJPA.getInstructorRevenue(startTime, endTime, user.getUserId(), KeyConstants.KEY_RENEWAL, statusList);
                    renewAmount = Math.round(renewAmount * 100.0) / 100.0;
                    totalMonthlyRevenue += renewAmount;
                    monthlyRevenueView.setWeekId(i + 1);
                    monthlyRevenueView.setWeekName("W" + (i + 1));
                    monthlyRevenueView.setWeekDuration(week[0] + KeyConstants.KEY_HYPHEN + week[week.length - 1]);
                    monthlyRevenueView.setRevenue(renewAmount);
                    monthlyRevenueView.setFormattedRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(renewAmount));
                    revenueViews.add(monthlyRevenueView);

                } else {
                    newAmount += instructorRevenueJPA.getInstructorRevenue(startTime, endTime, user.getUserId(), KeyConstants.KEY_NEW, statusList);
                    newAmount = Math.round(newAmount * 100.0) / 100.0;
                    totalMonthlyRevenue += newAmount;
                    monthlyRevenueView.setWeekId(i + 1);
                    monthlyRevenueView.setWeekName("W" + (i + 1));
                    monthlyRevenueView.setWeekDuration(week[0] + KeyConstants.KEY_HYPHEN + week[week.length - 1]);
                    monthlyRevenueView.setRevenue(newAmount);
                    monthlyRevenueView.setFormattedRevenue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(newAmount));
                    revenueViews.add(monthlyRevenueView);
                }

            }

            monthlyRevenueResponseView.setRevenuePerWeek(revenueViews);
            totalMonthlyRevenue = Math.round(totalMonthlyRevenue * 100.0) / 100.0;
            monthlyRevenueResponseView.setTotalMonthlyRevue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalMonthlyRevenue));
            responseModel.setPayload(monthlyRevenueResponseView);
        }
        log.info("Time taken for querying and response construction : Time taken in millis : " + (new Date().getTime() - temp));
        
        log.info("Get instructor revenue Api : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get instructor revenue data stops");
        return responseModel;
    }


    /**
     * @param date
     * @param isRenewDataNeeded
     * @return
     */
    public ResponseModel getInstructorSubscriptionStatsCount(String date, boolean isRenewDataNeeded, Long userId) throws ParseException {
        log.info("getInstructorSubscriptionStatsCount starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        User user = getUser(userId);

        boolean isYearDataNeeded = false;
        /**
         * YearlyRevenueResponseView - Splitted by means of months (Jan to Dec)
         */
        YearlySubscriptionResponseView yearlySubscriptionResponseView = new YearlySubscriptionResponseView();
        // yearlySubscriptionResponseView.setTotalYearlySubscriptions(60);


        /**
         * YearlyRevenueResponseView - Splitted by means of weeks (W1 to W4)
         */
        MonthlySubscriptionResponseView monthlySubscriptionResponseView = new MonthlySubscriptionResponseView();
        // monthlySubscriptionResponseView.setTotalMonthlySubscriptions(20);


        if (ValidationUtils.isEmptyString(date)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_DATE, MessageConstants.ERROR);
        }

        String RequestDates[] = date.split("/");

        if (RequestDates.length == 1) {
            isYearDataNeeded = true;
            validationService.validateYear(date);
        } else if (RequestDates.length == 2) {
            isYearDataNeeded = false;
            validationService.validateMonth(RequestDates[0]);
            validationService.validateYear(RequestDates[1]);
        }

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_INSTRUCTOR_SUBSCRIPTION_COUNT_DATA_FETCHED);

        if (isYearDataNeeded) {
            List<YearlySubscriptionView> subscriptionViews = new ArrayList<>();
            int totalCountForYear = 0;
            int month = 1;
            int year = Integer.parseInt(RequestDates[0]);
            for (int i = 0; i < 12; i++) {

                int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year, month);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                LocalDateTime startLocalDate = LocalDateTime.of(year, month, 1, 0, 0, 0);
                LocalDateTime endLocalDate = LocalDateTime.of(year, month, noOfDaysInTheMonth, 23, 59, 59);

                Date startDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                month++;
                /*
                 * YearlyRevenueView - Per month view
                 */
                YearlySubscriptionView yearlySubscriptionView = new YearlySubscriptionView();
                yearlySubscriptionView.setMonthId(i + 1);
                yearlySubscriptionView.setMonthName(ValidationUtils.months[i]);
                List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
                String renewalStatus;

                if (isRenewDataNeeded == true) {
                    renewalStatus = KeyConstants.KEY_RENEWAL;
                } else {
                    renewalStatus = KeyConstants.KEY_NEW;
                }
                int subscriptionCount = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, renewalStatus, user.getUserId(), statusList);
                yearlySubscriptionView.setMonthSubscriptionCount(subscriptionCount);
                totalCountForYear = totalCountForYear + subscriptionCount;
                subscriptionViews.add(yearlySubscriptionView);
            }
            yearlySubscriptionResponseView.setTotalYearlySubscriptions(totalCountForYear);
            yearlySubscriptionResponseView.setMonthlySubscriptionSplits(subscriptionViews);
            responseModel.setPayload(yearlySubscriptionResponseView);
        } else {
            List<MonthlySubscriptionView> revenueViews = new ArrayList<>();

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

            int firstWeekStartDate = 0, firstWeekEndDate = 0, secondWeekStartDate = 0, secondWeekEndDate = 0,
                    thirdWeekStartDate = 0, thirdWeekEndDate = 0, fourthWeekStartDate = 0, fourthWeekEndDate = 0;
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

            int startEndDateArrays[] = {firstWeekStartDate, firstWeekEndDate, secondWeekStartDate,
                    secondWeekEndDate, thirdWeekStartDate, thirdWeekEndDate, fourthWeekStartDate, fourthWeekEndDate};
            int count = 0;
            int totalCountForMonth = 0;

            for (int i = 0; i < 4; i++) {

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                LocalDateTime startLocalDate = LocalDateTime.of(Integer.parseInt(RequestDates[1]), Integer.parseInt(RequestDates[0]), startEndDateArrays[count], 0, 0, 0);
                LocalDateTime endLocalDate = LocalDateTime.of(Integer.parseInt(RequestDates[1]), Integer.parseInt(RequestDates[0]), startEndDateArrays[count + 1], 23, 59, 59);

                Date startDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(endLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                // String status = null;
                count = count + 2;
                /*
                 * Per week view
                 */
                MonthlySubscriptionView monthlySubscriptionView = new MonthlySubscriptionView();
                monthlySubscriptionView.setWeekId(i + 1);
                List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
                String renewalStatus;

                if (isRenewDataNeeded == true) {
                    renewalStatus = KeyConstants.KEY_RENEWAL;
                } else {
                    renewalStatus = KeyConstants.KEY_NEW;
                }
                int subscriptionCount = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, renewalStatus, user.getUserId(), statusList);
                monthlySubscriptionView.setSubscriptionCount(subscriptionCount);
                totalCountForMonth = totalCountForMonth + subscriptionCount;
                monthlySubscriptionView.setWeekName(ValidationUtils.weeks[i]);
                monthlySubscriptionView.setWeekDuration(weekSplit[i]);
                revenueViews.add(monthlySubscriptionView);
            }

            monthlySubscriptionResponseView.setTotalMonthlySubscriptions(totalCountForMonth);
            monthlySubscriptionResponseView.setSubscriptionsPerWeek(revenueViews);
            responseModel.setPayload(monthlySubscriptionResponseView);
        }

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info("getInstructorSubscriptionStatsCount() duration : Time taken in millis : " + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getInstructorSubscriptionStatsCount ends.");

        return responseModel;
    }

    /**
     * @param date
     * @return response model
     */
    public ResponseModel getMonthAndYearSubscriptionCount(String date, Long userId) throws ParseException {
        log.info("getMonthAndYearSubscriptionCount starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        String RequestDates[] = date.split("/");

        ResponseModel responseModel = null;
        if (RequestDates.length == 1) {
            long profilingStartTimeMillis = System.currentTimeMillis();
            ResponseModel responseModelForRenewal = getInstructorSubscriptionStatsCount(date, true, userId);
            long profilingEndTimeMillis = System.currentTimeMillis();
            log.info("Renew SubscriptionStatsCount : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            profilingStartTimeMillis = System.currentTimeMillis();
            ResponseModel responseModelForNew = getInstructorSubscriptionStatsCount(date, false, userId);
            profilingEndTimeMillis = System.currentTimeMillis();
            log.info("New SubscriptionStatsCount : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            profilingStartTimeMillis = System.currentTimeMillis();

            YearlySubscriptionResponseView yearlySubscriptionResponseForNew = (YearlySubscriptionResponseView) responseModelForNew.getPayload();
            YearlySubscriptionResponseView yearlySubscriptionResponseForRenewal = (YearlySubscriptionResponseView) responseModelForRenewal.getPayload();

            YearSubscriptionsOfNewAndRenewalResponseView yearSubscriptionsOfNewAndRenewalResponseView = new YearSubscriptionsOfNewAndRenewalResponseView();
            yearSubscriptionsOfNewAndRenewalResponseView.setTotalCount(yearlySubscriptionResponseForNew.getTotalYearlySubscriptions() + yearlySubscriptionResponseForRenewal.getTotalYearlySubscriptions());
            List<YearSubscriptionsOfNewAndRenewalView> yearSubscriptionsOfNewAndRenewalViews = new ArrayList<>();

            for (int index = 1; index <= 12; index++) {
                int finalIndex = index;
                YearlySubscriptionView yearlySubscriptionViewForNew = yearlySubscriptionResponseForNew.getMonthlySubscriptionSplits().stream().filter(month -> month.getMonthId() == finalIndex).findAny().get();
                YearlySubscriptionView yearlySubscriptionViewForRenewal = yearlySubscriptionResponseForRenewal.getMonthlySubscriptionSplits().stream().filter(month -> month.getMonthId() == finalIndex).findAny().get();

                YearSubscriptionsOfNewAndRenewalView yearSubscriptionsOfNewAndRenewalView = new YearSubscriptionsOfNewAndRenewalView();
                yearSubscriptionsOfNewAndRenewalView.setEntryId(yearlySubscriptionViewForNew.getMonthId());
                yearSubscriptionsOfNewAndRenewalView.setEntryName(yearlySubscriptionViewForNew.getMonthName());
                yearSubscriptionsOfNewAndRenewalView.setNewSubscriptionCount(yearlySubscriptionViewForNew.getMonthSubscriptionCount());
                yearSubscriptionsOfNewAndRenewalView.setRenewalSubscriptionCount(yearlySubscriptionViewForRenewal.getMonthSubscriptionCount());
                yearSubscriptionsOfNewAndRenewalViews.add(yearSubscriptionsOfNewAndRenewalView);
            }
            yearSubscriptionsOfNewAndRenewalResponseView.setChartEntryList(yearSubscriptionsOfNewAndRenewalViews);
            responseModel = new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_SUBSCRIPTION_COUNT_DATA_FETCHED, yearSubscriptionsOfNewAndRenewalResponseView);

            profilingEndTimeMillis = System.currentTimeMillis();
            log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        } else if (RequestDates.length == 2) {
            long profilingStartTimeMillis = System.currentTimeMillis();
            ResponseModel responseModelForRenewal = getInstructorSubscriptionStatsCount(date, true, userId);
            long profilingEndTimeMillis = System.currentTimeMillis();
            log.info("Renew SubscriptionStatsCount : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

            profilingStartTimeMillis = System.currentTimeMillis();
            ResponseModel responseModelForNew = getInstructorSubscriptionStatsCount(date, false, userId);
            profilingEndTimeMillis = System.currentTimeMillis();
            log.info("New SubscriptionStatsCount : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));


            profilingStartTimeMillis = System.currentTimeMillis();

            MonthlySubscriptionResponseView monthlySubscriptionResponseViewForNew = (MonthlySubscriptionResponseView) responseModelForNew.getPayload();
            MonthlySubscriptionResponseView monthlySubscriptionResponseViewForRenewal = (MonthlySubscriptionResponseView) responseModelForRenewal.getPayload();

            MonthSubscriptionsOfNewAndRenewalResponseView monthSubscriptionsOfNewAndRenewalResponseView = new MonthSubscriptionsOfNewAndRenewalResponseView();
            monthSubscriptionsOfNewAndRenewalResponseView.setTotalCount(monthlySubscriptionResponseViewForNew.getTotalMonthlySubscriptions() + monthlySubscriptionResponseViewForRenewal.getTotalMonthlySubscriptions());
            List<MonthSubscriptionsOfNewAndRenewalView> monthSubscriptionsOfNewAndRenewalViews = new ArrayList<>();

            for (int index = 1; index <= 4; index++) {
                int finalIndex = index;
                MonthlySubscriptionView monthlySubscriptionViewForNew = monthlySubscriptionResponseViewForNew.getSubscriptionsPerWeek().stream().filter(week -> week.getWeekId() == finalIndex).findAny().get();
                MonthlySubscriptionView monthlySubscriptionViewForRenewal = monthlySubscriptionResponseViewForRenewal.getSubscriptionsPerWeek().stream().filter(week -> week.getWeekId() == finalIndex).findAny().get();
                MonthSubscriptionsOfNewAndRenewalView monthSubscriptionsOfNewAndRenewalView = new MonthSubscriptionsOfNewAndRenewalView();
                monthSubscriptionsOfNewAndRenewalView.setEntryId(monthlySubscriptionViewForNew.getWeekId());
                monthSubscriptionsOfNewAndRenewalView.setEntryName(monthlySubscriptionViewForNew.getWeekName());
                monthSubscriptionsOfNewAndRenewalView.setWeekDuration(monthlySubscriptionViewForNew.getWeekDuration());
                monthSubscriptionsOfNewAndRenewalView.setNewSubscriptionCount(monthlySubscriptionViewForNew.getSubscriptionCount());
                monthSubscriptionsOfNewAndRenewalView.setRenewalSubscriptionCount(monthlySubscriptionViewForRenewal.getSubscriptionCount());
                monthSubscriptionsOfNewAndRenewalViews.add(monthSubscriptionsOfNewAndRenewalView);
            }
       /* Map<String, Object> monthAndYearSubscriptionCount = new HashMap<>();
        monthAndYearSubscriptionCount.put(KeyConstants.KEY_NEW , responseModelForNew.getPayload());
        monthAndYearSubscriptionCount.put(KeyConstants.KEY_RENEWAL , responseModelForRenewal.getPayload());*/
            monthSubscriptionsOfNewAndRenewalResponseView.setChartEntryList(monthSubscriptionsOfNewAndRenewalViews);
            responseModel =  new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_INSTRUCTOR_SUBSCRIPTION_COUNT_DATA_FETCHED, monthSubscriptionsOfNewAndRenewalResponseView);

            profilingEndTimeMillis = System.currentTimeMillis();
            log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        }

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getMonthAndYearSubscriptionCount ends.");

        if (responseModel != null) {
            return responseModel;
        }
        throw new ApplicationException(Constants.ERROR_STATUS, ValidationMessageConstants.MSG_INVALID_DATE, null);
    }

    /**
     * Get Instructor Analytics.
     *
     * @return ResponseModel
     */
    public ResponseModel getInstructorAnalytics(Long userId) {
        log.info("Get instructor analytics starts");
        long startTime = System.currentTimeMillis();
        User user = getUser(userId);
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId());
        InstructorAnalyticsResponseView instructorAnalyticsResponse = new InstructorAnalyticsResponseView();
        long programCountLong = programRepository.countByOwnerUserIdAndStatus(user.getUserId(), InstructorConstant.PUBLISH);
        log.info("Get program List : Time taken in millis : "+(System.currentTimeMillis() - startTime));
        
        int programCount = Math.toIntExact(programCountLong);
        double totalRevenue = 0.0;
        double upcomingPayment = 0.0;
        long temp = System.currentTimeMillis();
        int subscriptionCount = (int) subscriptionService.getActiveSubscripionCountOfAnInstructor(user.getUserId());
        log.info("Get subscriptionCount : Time taken in millis : "+(System.currentTimeMillis() - temp));
        
        BigDecimal overallProgramRating = calculateAverageProgramRating(userId);

        instructorAnalyticsResponse.setTotalPrograms(programCount);
        instructorAnalyticsResponse.setTotalSubscriptions(subscriptionCount);
        instructorAnalyticsResponse.setOverallRating(overallProgramRating);
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        //Total revenue- Instructor share till date
        temp = System.currentTimeMillis();
        Double instructorShareByOwnerUserId = subscriptionAuditJPA.getInstructorShareByOwnerUserId(KeyConstants.KEY_PROGRAM, statusList, user.getUserId());
        log.info("Get instructor share : Time taken in millis : "+(System.currentTimeMillis() - temp));
        if (instructorShareByOwnerUserId != null)
            totalRevenue = instructorShareByOwnerUserId;

        instructorAnalyticsResponse.setOverallRevenue(totalRevenue);

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        instructorAnalyticsResponse.setOverAllRevenueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));

        //Upcoming payment - from month's first day to till date - instructor share
        LocalDateTime localDateTime = LocalDateTime.now();

        int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(localDateTime.getYear(), localDateTime.getMonthValue());

        List<String> subscriptionTypeList = Arrays.asList(new String[]{KeyConstants.KEY_PROGRAM, KeyConstants.KEY_SUBSCRIPTION_PACKAGE});
        upcomingPayment = calculateOutstandingPaymentOfAnInstructor(user.getUserId(), subscriptionTypeList);
        instructorAnalyticsResponse.setUpcomingPayment(upcomingPayment);

        instructorAnalyticsResponse.setUpcomingPaymentFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));
        if (!ValidationUtils.isEmptyString(userProfile.getFirstName()) && !ValidationUtils.isEmptyString(userProfile.getLastName())) {
            instructorAnalyticsResponse.setName(userProfile.getFirstName() + " " + userProfile.getLastName());

        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - startTime));
        log.info("Get instructor analytics stops");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_DATA_RETRIEVED, instructorAnalyticsResponse);
    }

    /**
     * @param instructorId
     * @return
     */
    private BigDecimal calculateAverageProgramRating(Long instructorId) {

        List<Double> ratingList = programRatingJPA.getAverageProgramRatingOfInstructor(instructorId, InstructorConstant.PUBLISH);
        double rating = ratingList.stream()
                .filter(avgRating -> avgRating != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        BigDecimal overAllRating = new BigDecimal(rating).setScale(2, RoundingMode.HALF_UP);
        return overAllRating;
    }

    public ResponseModel getClientDemographics(String startDateString, String endDateString, String graphBasis, Long userId) {
        log.info("getClientDemographics starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);

        User instructor = getUser(userId);

        HashMap<String, ClientDemographicsGraph> graphMap = loadClientDemographicsGraph();

        ClientDemographicsGraph clientDemographicsGraph = graphMap.get(graphBasis);
        if (clientDemographicsGraph == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_GRAPH_NOT_AVAILABLE, MessageConstants.ERROR);
        }
        ResponseModel responseModel = clientDemographicsGraph.getClientDemographics(instructor, dateRange);

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getClientDemographics ends.");
        return responseModel;

    }

    private HashMap<String, ClientDemographicsGraph> loadClientDemographicsGraph() {
        HashMap<String, ClientDemographicsGraph> graphMap = new HashMap<>();
        graphMap.put("gender", clientDemographicsByGender);
        graphMap.put("age", clientDemographicsByAge);
        graphMap.put("expertise", clientDemographicsByExpertise);
        return graphMap;
    }

    public ResponseModel getSubscriptionByPlatform(String startDateString, String endDateString, Long userId) {
        log.info("Get Instructor's subscription by platform starts");
        long startTime = System.currentTimeMillis();

        User instructor = getUser(userId);
        log.info("Get user : Time taken in millis : "+(System.currentTimeMillis() - startTime));
        long temp = System.currentTimeMillis();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date range : Time taken in millis : "+(System.currentTimeMillis() - temp));

        temp = System.currentTimeMillis();
        List<PlatformType> platformTypeList = platformTypeRepository.findAll();

        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);
        log.info("Platform type query and platform display names : Time taken in millis : "+(System.currentTimeMillis() - temp));

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;
        temp = System.currentTimeMillis();
        for (PlatformType platformType : platformTypeList) {
            ChartEntry chartEntry = new ChartEntry();

            int count = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserIdAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, statusList, instructor.getUserId(), platformType.getPlatformTypeId(), startDate, endDate);
            chartEntry.setEntryName(platFormDisplayName.getProperty(platformType.getPlatform(), platformType.getPlatform()));
            chartEntry.setCount(count);

            chartEntryList.add(chartEntry);
            totalCount += count;
        }


        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
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
        log.info("Query and response construction : Time taken in millis : "+(System.currentTimeMillis() - temp));
        log.info("Get Instructor's subscription by platform : Time taken in millis : "+(System.currentTimeMillis() - startTime));
        log.info("Get Instructor's subscription by platform ends");

        return response;
    }


    /**
     * get instructor programs performance stats.
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    public ResponseModel getInstructorProgramsPerformanceStats(int pageNo, int pageSize, Long userId) {
        log.info("Get instructor programs performance stats starts.");
        long apiStartTimeMillis = new Date().getTime();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        User user = getUser(userId);
        List<Programs> programsList = programRepository.findByOwnerUserIdAndStatus(user.getUserId(), InstructorConstant.PUBLISH);
        List<InstructorProgramsPerformanceViewResponse> instructorProgramsPerformanceViewResponses = new ArrayList<>();
        log.info("Query to get programs list : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        for (Programs program : programsList) {
            //List<ProgramFeedback> programFeedbacks = programFeedbackRepository.findByProgram(program);
            List<ProgramRating> programRatingList = programRatingRepository.findByProgram(program);

            double percentage = calculateConversionRateOfAnProgram(program);

            /**
             * calculating rating for each program
             */
            double totalRating = 0.0;
            int count = 0;
            /*for (ProgramFeedback programFeedback : programFeedbacks) {

                if (programFeedback.getFeedback().getRate() != null){
                    totalRating = totalRating + programFeedback.getFeedback().getRate();
            }
                    count++;
            }
            double rating = count == 0 ? 0.0 : totalRating / count;*/

            for (ProgramRating programRating : programRatingList) {
                if (programRating != null) {
                    totalRating = totalRating + programRating.getProgramRating();
                    count++;
                }
            }
            double rating = count == 0 ? 0.0 : totalRating / count;

            InstructorProgramsPerformanceViewResponse instructorProgramsPerformanceViewResponse = new InstructorProgramsPerformanceViewResponse();
            instructorProgramsPerformanceViewResponse.setProgramTitle(program.getTitle());
            instructorProgramsPerformanceViewResponse.setPercentage(percentage);
            instructorProgramsPerformanceViewResponse.setRating(new BigDecimal(rating).setScale(2, RoundingMode.HALF_UP));
            instructorProgramsPerformanceViewResponses.add(instructorProgramsPerformanceViewResponse);
        }
        log.info("Construct instructor program performance view responses : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        List<InstructorProgramsPerformanceViewResponse> instructorProgramsPerformanceViewResponseList = instructorProgramsPerformanceViewResponses.stream().sorted(Comparator.comparing(InstructorProgramsPerformanceViewResponse::getPercentage).thenComparing(InstructorProgramsPerformanceViewResponse::getRating)).collect(Collectors.toList());
        log.info("Sort instructorProgramsPerformanceViewResponses : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        int fromIndex = (pageNo - 1) * pageSize;
        if (instructorProgramsPerformanceViewResponses == null || instructorProgramsPerformanceViewResponses.size() < fromIndex) {
            return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_PERFORMANCES_STATS, new ArrayList<>());
        }
        Collections.reverse(instructorProgramsPerformanceViewResponseList);
        log.info("Reverse sorting instructorProgramsPerformanceViewResponses : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();
        Map<String, Object> response = new HashMap<>();
        response.put(KeyConstants.KEY_TOTAL_COUNT, instructorProgramsPerformanceViewResponseList.size());
        response.put(KeyConstants.KEY_PROGRAM_PERFORMANCES, instructorProgramsPerformanceViewResponseList.subList(fromIndex, Math.min(fromIndex + pageSize, instructorProgramsPerformanceViewResponseList.size())));
        log.info("Response construction : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get instructor programs performance stats ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_PROGRAM_PERFORMANCES_STATS, response);
    }


    public ResponseModel getRevenueByPlatform(String startDateString, String endDateString, Long userId) {
        log.info("Get Instructor revenue by platform starts");
        long start = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        long temp = new Date().getTime();
        User user = getUser(userId);
        log.info("Get user : Time taken in millis : " + (new Date().getTime() - temp));

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);

        temp = new Date().getTime();
        List<RevenueByPlatform> revenueByPlatformList = platformTypeJPA.getRevenueByPlatform(user.getUserId(), startDate, endDate);
        log.info("Query : Time taken in millis : " + (new Date().getTime() - temp));

        temp = new Date().getTime();
        List<ChartEntryWithValue<Double>> chartEntryList = new ArrayList<>();
        double totalInstructorShare = 0.0;
        for (RevenueByPlatform revenueByPlatform : revenueByPlatformList) {
            double instructorShare = revenueByPlatform.getRevenue() == null ? 0.0 : revenueByPlatform.getRevenue().doubleValue();
            String platformTypeName = revenueByPlatform.getPlatformName();

            ChartEntryWithValue<Double> chartEntry = new ChartEntryWithValue<Double>();
            chartEntry.setEntryName(platFormDisplayName.getProperty(platformTypeName, platformTypeName));

            instructorShare = Math.round(instructorShare * 100.0) / 100.0;
            chartEntry.setValue(instructorShare);
            chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorShare));
            totalInstructorShare += instructorShare;

            chartEntryList.add(chartEntry);
        }
        log.info("Data construction : Time taken in millis : " + (new Date().getTime() - temp));

        temp = new Date().getTime();
        new ChartDataUtils().populationEntryPercentages(chartEntryList, totalInstructorShare);
        log.info("Populate percentage : Time taken in millis : " + (new Date().getTime() - temp));

        temp = new Date().getTime();
        ChartDataWithValue<Double> chartData = new ChartDataWithValue<>();
        totalInstructorShare = Math.round(totalInstructorShare * 100.0) / 100.0;
        chartData.setTotalValue(totalInstructorShare);
        chartData.setFormattedTotalValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalInstructorShare));
        chartData.setChartEntryList(chartEntryList);
        log.info("Chart data final response construction : Time taken in millis : " + (new Date().getTime() - temp));


        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Get instructor revenue by platform : Total Time taken in millis : " + (new Date().getTime() - start));
        log.info("Get instructor revenue by platform ends");

        return response;
    }

    public ResponseModel getRevenueDataForYear(String startDateString, String endDateString, Long userId) throws ParseException {
        log.info("Get Revenue data for year starts");
        long start = System.currentTimeMillis();
        List<SplitGraphEntryWithValue<Double>> splitGraphEntryList = new ArrayList<>();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(System.currentTimeMillis() - start));

        long temp = System.currentTimeMillis();
        User user = getUser(userId);
        log.info("Get user : Time taken in millis : "+(System.currentTimeMillis() - temp));


        double totalRevenue = 0.0;

        temp = System.currentTimeMillis();
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);


        int years = diff.getYears();
        int noOfMonths = diff.getMonths();
        noOfMonths = noOfMonths + (years * 12);
        log.info("Get year and months : Time taken in millis : "+(System.currentTimeMillis() - temp));


        int month = 1;
        int year = Integer.parseInt(startDateString);
        //Getting each month's last date data
        temp = System.currentTimeMillis();
        for (int i = 0; i <= noOfMonths; i++) {
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

            double newAmount = 0;
            double renewAmount = 0;
            double totalAmount = newAmount + renewAmount;

            newAmount = instructorPaymentJPA.getInstructorRevenueBasedOnRenewalStatus(user.getUserId(),KeyConstants.KEY_NEW, KeyConstants.KEY_PROGRAM,startTime,endTime );
            newAmount = Math.round(newAmount * 100.0) / 100.0;
            totalAmount += newAmount;

            renewAmount = instructorPaymentJPA.getInstructorRevenueBasedOnRenewalStatus(user.getUserId(),KeyConstants.KEY_RENEWAL, KeyConstants.KEY_PROGRAM,startTime,endTime );
            renewAmount = Math.round(renewAmount * 100.0) / 100.0;
            totalAmount += renewAmount;
            totalAmount = Math.round(totalAmount * 100.0) / 100.0;

            SplitGraphEntryWithValue<Double> splitGraphEntry = new SplitGraphEntryWithValue<Double>();
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
        log.info("Get revenue data for all months and response construction for graph entry : Time taken in millis : "+(System.currentTimeMillis() - temp));


        SplitGraphDataWithValue<Double> splitGraphData = new SplitGraphDataWithValue<Double>();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        splitGraphData.setOverAllValue(totalRevenue);

        splitGraphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));

        splitGraphData.setGraphEntryList(splitGraphEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(splitGraphData);
        log.info("Get instructor revenue data for year : Total Time taken in millis : "+(System.currentTimeMillis() - start));
        log.info("Get instructor revenue data for year ends");

        return response;

    }

    public ResponseModel getRevenueDataForMonth(String startDateString, String endDateString, Long userId) {
        log.info("Get Instructor revenue data for month starts");
        long start = System.currentTimeMillis();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(System.currentTimeMillis() - start));
        long temp = System.currentTimeMillis();
        User user = getUser(userId);
        log.info("Get user : Time taken in millis : "+(System.currentTimeMillis() - temp));

        double totalRevenue = 0.0;
        temp = System.currentTimeMillis();
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
        log.info("Get difference and week split : Time taken in millis : "+(System.currentTimeMillis() - temp));


        Date tempDate = new Date(startDate.getTime());
        List<SplitGraphEntryWithValue<Double>> splitGraphEntryList = new ArrayList<>();
        double overAllValue = 0.0;


        List<SplitGraphEntryWithValue<Double>> graphEntryWithValues = new ArrayList<>();
        //Getting each week's last date data
        temp = System.currentTimeMillis();
        for (int i = 0; i < weekSplit.length; i++) {

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

            double newAmount = 0.0;
            double renewAmount = 0.0;
            double totalAmount = newAmount + renewAmount;
            newAmount = instructorPaymentJPA.getInstructorRevenueBasedOnRenewalStatus(user.getUserId(),KeyConstants.KEY_NEW, KeyConstants.KEY_PROGRAM,startTime,endTime );
            newAmount = Math.round(newAmount * 100.0) / 100.0;
            totalAmount += newAmount;

            renewAmount = instructorPaymentJPA.getInstructorRevenueBasedOnRenewalStatus(user.getUserId(),KeyConstants.KEY_RENEWAL, KeyConstants.KEY_PROGRAM,startTime,endTime );
            renewAmount = Math.round(renewAmount * 100.0) / 100.0;
            totalAmount += renewAmount;
            totalAmount = Math.round(totalAmount * 100.0) / 100.0;

            SplitGraphEntryWithValue<Double> splitGraphEntry = new SplitGraphEntryWithValue<Double>();
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
        log.info("Get revenue data for all weeks and response construction for graph entry : Time taken in millis : "+(System.currentTimeMillis() - temp));

        SplitGraphDataWithValue<Double> splitGraphData = new SplitGraphDataWithValue<Double>();
        overAllValue = Math.round(overAllValue * 100.0) / 100.0;
        splitGraphData.setOverAllValue(overAllValue);

        splitGraphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(overAllValue));

        splitGraphData.setGraphEntryList(splitGraphEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(splitGraphData);
        log.info("Get instructor revenue data for month : Total Time taken in millis : "+(System.currentTimeMillis() - start));
        log.info("Get instructor revenue data for month ends");
        return response;

    }

    public double calculateOutstandingPaymentOfAnInstructor(long instructorId, List<String> subscriptionTypeList) {
        long apiStartTimeMillis = System.currentTimeMillis();
        /**
         * Checking whether the instructor has onboarded into Stripe account and if yes, adding the pending balance
         * from Instructor connected account to the outstanding balance calculation
         */

        double outstandingBalance = 0.0;

        StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructorId);
        if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
            double stripePendingBalance = 0;
            Stripe.apiKey = stripeProperties.getApiKey();
            RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(stripeAccountAndUserMapping.getStripeAccountId()).build();
            try {
                Balance balance = Balance.retrieve(requestOptions);
                //Getting the total pending balance
                for (Balance.Money money : balance.getPending()) {
                    stripePendingBalance += money.getAmount();
                }
            } catch (StripeException e) {
                e.printStackTrace();
            }
            stripePendingBalance = stripePendingBalance / 100;

            //Getting instructor shares for apple orders for which top up are not initiated
            PlatformType iOSPlatform = platformTypeRepository.findByPlatformTypeId(NotificationConstants.APPLE_PLATFORM);
            List<Long> platformIdList = Arrays.asList(new Long[]{iOSPlatform.getPlatformTypeId()});
            Double applePendingRevenue = instructorPaymentJPA.getInstructorPendingRevenue(instructorId, false, false, platformIdList, KeyConstants.KEY_REFUNDED);
            applePendingRevenue = applePendingRevenue == null ? 0 : applePendingRevenue;

            outstandingBalance = stripePendingBalance + applePendingRevenue;

        } else {

            double settledAmount = qboVendorBillPaymentJPA.getSettledAmountForInstructor(instructorId).doubleValue();
            double totalInstructorShare = instructorPaymentJPA.getInstructorTotalRevenue(instructorId);
            double iOSRefundInstructorShare = instructorPaymentJPA.getInstructorIOSRefundRevenue(instructorId);

            //Calculating refund instructor share for stripe orders
            List<StripePayment> stripePaymentList = stripePaymentRepository.findByOrderManagementProgramOwnerUserIdAndTransactionStatusAndOrderManagementModeOfPayment(instructorId, KeyConstants.KEY_REFUND, PaymentConstants.MODE_OF_PAYMENT_STRIPE);
            List<Integer> orderManagementIdList = stripePaymentList.stream().map(stripePay -> stripePay.getOrderManagement().getId()).collect(Collectors.toList());
            List<InstructorPayment> instructorPaymentList = instructorPaymentRepository.findByOrderManagementIdIn(orderManagementIdList);
            HashMap<Integer, StripeAndInstructorPayment> stripeAndInstructorPaymentMap = constructStripeAndInstructorPaymentMap(orderManagementIdList, stripePaymentList, instructorPaymentList);

            double stripeRefundInstructorShare = 0.0;
            for (Map.Entry<Integer, StripeAndInstructorPayment> entry : stripeAndInstructorPaymentMap.entrySet()) {
                StripeAndInstructorPayment stripeAndInstructorPayment = entry.getValue();
                stripeRefundInstructorShare += qboService.getRefundAmount(stripeAndInstructorPayment.getInstructorPayment(), stripeAndInstructorPayment.getStripePayment());
            }
            double refundInstructorShare = iOSRefundInstructorShare + stripeRefundInstructorShare;

            outstandingBalance = Math.round((totalInstructorShare - (settledAmount + refundInstructorShare)) * 100.0) / 100.0;
        }

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info("calculateOutstandingPaymentOfAnInstructor duration : Time taken in millis : " + (apiEndTimeMillis - apiStartTimeMillis));

        return outstandingBalance;
    }

    private HashMap<Integer, StripeAndInstructorPayment> constructStripeAndInstructorPaymentMap(List<Integer> orderManagementIdList, List<StripePayment> stripePaymentList, List<InstructorPayment> instructorPaymentList) {
        HashMap<Integer, StripePayment> stripePaymentMap = new HashMap<>();
        for (StripePayment stripePayment : stripePaymentList) {
            stripePaymentMap.put(stripePayment.getOrderManagement().getId(), stripePayment);
        }

        HashMap<Integer, InstructorPayment> instructorPaymentMap = new HashMap<>();
        for (InstructorPayment instructorPayment : instructorPaymentList) {
            instructorPaymentMap.put(instructorPayment.getOrderManagement().getId(), instructorPayment);
        }

        HashMap<Integer, StripeAndInstructorPayment> stripeAndInstructorPaymentMap = new HashMap<>();
        for (Integer orderId : orderManagementIdList) {
            StripePayment stripePayment = stripePaymentMap.get(orderId);
            InstructorPayment instructorPayment = instructorPaymentMap.get(orderId);
            if (stripePayment != null && instructorPayment != null) {
                stripeAndInstructorPaymentMap.put(orderId, new StripeAndInstructorPayment(stripePayment, instructorPayment));
            }
        }
        return stripeAndInstructorPaymentMap;
    }

    @Data
    @AllArgsConstructor
    private class StripeAndInstructorPayment {
        private StripePayment stripePayment;
        private InstructorPayment instructorPayment;
    }

    public double calculateConversionRateOfAnProgram(Programs program) {
        int totalTrialTaken = 0;
        int totalSubscriptionFromTrial = 0;
        double percentage = 0.0;

        List<SubscriptionAudit> subscriptionAudits = subscriptionAuditRepo.findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramId(KeyConstants.KEY_PROGRAM, KeyConstants.KEY_TRIAL, program.getProgramId());
        totalTrialTaken = subscriptionAudits.size();

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        for(SubscriptionAudit subscriptionAudit : subscriptionAudits){
            SubscriptionAudit subscriptionAudit1 = subscriptionAuditRepo.findBySubscriptionTypeNameAndProgramSubscriptionAndSubscriptionStatusSubscriptionStatusNameInAndRenewalStatus
                    (KeyConstants.KEY_PROGRAM,subscriptionAudit.getProgramSubscription(),statusList,KeyConstants.KEY_NEW);
            if(subscriptionAudit1 != null && subscriptionAudit1.getSubscriptionDate().after(subscriptionAudit.getSubscriptionDate())){
                totalSubscriptionFromTrial ++;
            }

        }
        if (totalTrialTaken != 0) {
            percentage = (totalSubscriptionFromTrial * 100 / totalTrialTaken);
            percentage = Math.round(percentage * 100.0) / 100.0;
        }
        return percentage;
    }

    public User getUser(Long userId) {
        User user = null;
        if (userId == null || userId == 0) {
            user = userComponents.getUser();
        } else {
            user = userRepository.findByUserId(userId);
        }
        if (user == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, MessageConstants.ERROR);
        }
        return user;
    }

    /**
     * Net revenue of instructor for a packages alone
     * @param instructorId
     * @return
     */
    public double calculateNetRevenueOfAnInstructor(long instructorId, List<String> subscriptionTypeList) {
        double totalInstructorShare = 0.0;

        if (subscriptionTypeList != null) {
            Double instructorShare = instructorRevenueJPA.getNetRevenue(subscriptionTypeList,instructorId);
            if (instructorShare != null)
                totalInstructorShare += instructorShare;
        }

        totalInstructorShare = Math.round((totalInstructorShare) * 100.0) / 100.0;
        return totalInstructorShare;
    }
}

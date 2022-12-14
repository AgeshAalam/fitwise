package com.fitwise.service.admin;

/*
 * Created by Vignesh Gunasekar on 09/03/20
 */

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.BlockedUser;
import com.fitwise.entity.DeletedUserAudit;
import com.fitwise.entity.DiscardWorkoutReasons;
import com.fitwise.entity.FeedbackTypes;
import com.fitwise.entity.FlaggedVideoReason;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.WorkoutDiscardFeedback;
import com.fitwise.entity.WorkoutDiscardFeedbackMapping;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.BlockedUserRepository;
import com.fitwise.repository.DeletedUserAuditRepository;
import com.fitwise.repository.FlaggedExerciseRepository;
import com.fitwise.repository.FlaggedVideoReasonsRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.feedback.DiscardWorkoutReasonsRepository;
import com.fitwise.repository.feedback.FeedbackTypesRepository;
import com.fitwise.repository.feedback.WorkoutDiscardFeedbackRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.response.DiscardWorkoutFeedbackResponse;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.specifications.jpa.PlatformTypeJPA;
import com.fitwise.specifications.jpa.ProgramTypeJPA;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.specifications.jpa.WorkoutFeedbackJPA;
import com.fitwise.specifications.jpa.dao.InstructorPayout;
import com.fitwise.specifications.jpa.dao.RevenueByPlatform;
import com.fitwise.specifications.jpa.dao.SubscriptionCountByProgramType;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.utils.GraphUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.DeletedOrBlockedUserView;
import com.fitwise.view.MonthSubscriptionsOfNewAndRenewalResponseView;
import com.fitwise.view.MonthSubscriptionsOfNewAndRenewalView;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.YearSubscriptionsOfNewAndRenewalResponseView;
import com.fitwise.view.YearSubscriptionsOfNewAndRenewalView;
import com.fitwise.view.graph.ChartData;
import com.fitwise.view.graph.ChartDataWithValue;
import com.fitwise.view.graph.ChartEntry;
import com.fitwise.view.graph.ChartEntryWithValue;
import com.fitwise.view.graph.GraphData;
import com.fitwise.view.graph.GraphDataWithValue;
import com.fitwise.view.graph.GraphEntry;
import com.fitwise.view.graph.GraphEntryWithValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDashBoardPlatformService {

    @Autowired
    private ValidationService validationService;

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    PlatformTypeRepository platformTypeRepository;

    @Autowired
    FlaggedVideoReasonsRepository flaggedVideoReasonsRepository;

    @Autowired
    FlaggedExerciseRepository flaggedExerciseRepository;

    @Autowired
    DiscardWorkoutReasonsRepository discardWorkoutReasonsRepository;

    @Autowired
    WorkoutDiscardFeedbackRepository workoutDiscardFeedbackRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private DeletedUserAuditRepository deletedUserAuditRepository;

    @Autowired
    private BlockedUserRepository blockedUserRepository;
    @Autowired
    FeedbackTypesRepository feedbackTypesRepository;
    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    GraphUtils graphUtils;

    @Autowired
    private SubscriptionAuditJPA subscriptionAuditJPA;

    @Autowired
    private PlatformTypeJPA platformTypeJPA;

    @Autowired
    private ProgramTypeJPA programTypeJPA;

    private final WorkoutFeedbackJPA workoutFeedbackJPA;

    /**
     * Get SubscriptionByProgramType for Admin dashboard graph on Platform tab.
     *
     * @param startDateString
     * @param endDateString
     * @return
     * @throws ApplicationException
     */
    public ResponseModel getSubscriptionByProgramType(String startDateString, String endDateString) throws ApplicationException {
        long startTime = new Date().getTime();
        log.info("Get subscription by program type started");
        long temp = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        log.info("Validate and chart date range construction " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        List<SubscriptionCountByProgramType> subscriptionCountByProgramTypeList = programTypeJPA.getSubscriptionCountByProgramType(startDate, endDate);
        log.info("Program type list " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        int totalCount = 0;
        List<ChartEntry> chartEntryList = new ArrayList<>();
        for (SubscriptionCountByProgramType subscriptionCountByProgramType : subscriptionCountByProgramTypeList) {
            ChartEntry chartEntry = new ChartEntry();
            int count = Math.toIntExact(subscriptionCountByProgramType.getSubscriptionCount());
            chartEntry.setCount(count);
            chartEntry.setEntryName(subscriptionCountByProgramType.getProgramType());
            chartEntryList.add(chartEntry);
            totalCount += count;
        }
        log.info("Count for program types " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            List<String> chartEntryNames = subscriptionCountByProgramTypeList.stream().map(SubscriptionCountByProgramType::getProgramType).collect(Collectors.toList());
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }
        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);
        log.info("Populate chart data " + (new Date().getTime() - temp));
        ChartData chartData = new ChartData();
        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Get subscription by type completed " + (new Date().getTime() - startTime));
        return response;
    }

    /**
     * Get All subscriptions of new and renewal
     *
     * @param date
     * @return responseModel
     * @throws ApplicationException
     */
    public ResponseModel getAllSubscriptionsOfNewAndRenewal(String date) throws ParseException {
        log.info("Get all subscription of new and renewal starts.");
        long apiStartTimeMillis = new Date().getTime();
        boolean isYearDataNeeded = false;

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
        log.info("Validate date : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();

        ResponseModel responseModel = new ResponseModel();
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_INSTRUCTOR_SUBSCRIPTION_COUNT_DATA_FETCHED);

        List<String> subscriptionStatusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        if (isYearDataNeeded) {
            List<YearSubscriptionsOfNewAndRenewalView> yearSubscriptionsOfNewAndRenewalViews = new ArrayList<>();
            YearSubscriptionsOfNewAndRenewalResponseView yearSubscriptionsOfNewAndRenewalResponseView = new YearSubscriptionsOfNewAndRenewalResponseView();
            int totalCountForYearOfNew = 0;
            int totalCountForYearOfRenewal = 0;
            int month = 1;
            int year = Integer.parseInt(RequestDates[0]);
            for (int i = 0; i < 12; i++) {

                int noOfDaysInTheMonth = ValidationUtils.getNumberOfDaysInTheMonth(year, month);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = formatter.parse(year + "-" + month + "-" + 1);
                Date endDate = formatter.parse(year + "-" + month + "-" + noOfDaysInTheMonth);
                month++;
                /*
                 * YearlyRevenueView - Per month view
                 */
                YearSubscriptionsOfNewAndRenewalView yearSubscriptionsOfNewAndRenewalView = new YearSubscriptionsOfNewAndRenewalView();
                yearSubscriptionsOfNewAndRenewalView.setEntryId(i + 1);
                yearSubscriptionsOfNewAndRenewalView.setEntryName(ValidationUtils.months[i]);
                int subscriptionCountForRenewal = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_RENEWAL, subscriptionStatusList);
                int subscriptionCountForNew = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_NEW, subscriptionStatusList);
                yearSubscriptionsOfNewAndRenewalView.setNewSubscriptionCount(subscriptionCountForNew);
                yearSubscriptionsOfNewAndRenewalView.setRenewalSubscriptionCount(subscriptionCountForRenewal);
                totalCountForYearOfNew = totalCountForYearOfNew + subscriptionCountForNew;
                totalCountForYearOfRenewal = totalCountForYearOfRenewal + subscriptionCountForRenewal;
                yearSubscriptionsOfNewAndRenewalViews.add(yearSubscriptionsOfNewAndRenewalView);
            }
            yearSubscriptionsOfNewAndRenewalResponseView.setTotalCount(totalCountForYearOfNew + totalCountForYearOfRenewal);
            yearSubscriptionsOfNewAndRenewalResponseView.setChartEntryList(yearSubscriptionsOfNewAndRenewalViews);
            responseModel.setPayload(yearSubscriptionsOfNewAndRenewalResponseView);
            log.info("Get year subscriptions of new and renewal : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
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
            int totalCountForMonthOfNew = 0;
            int totalCountForMonthOfRenewal = 0;

            for (int i = 0; i < 4; i++) {

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = formatter.parse(Integer.parseInt(RequestDates[1]) + "-" + Integer.parseInt(RequestDates[0]) + "-" + startEndDateArrays[count]);
                Date endDate = formatter.parse(Integer.parseInt(RequestDates[1]) + "-" + Integer.parseInt(RequestDates[0]) + "-" + startEndDateArrays[count + 1]);
                count = count + 2;
                /*
                 * Per week view
                 */
                MonthSubscriptionsOfNewAndRenewalView monthSubscriptionsOfNewAndRenewalView = new MonthSubscriptionsOfNewAndRenewalView();
                monthSubscriptionsOfNewAndRenewalView.setEntryId(i + 1);

                int subscriptionCountOfRenewal = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_RENEWAL, subscriptionStatusList);
                int subscriptionCountOfNew = subscriptionAuditRepo.countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndSubscriptionStatusSubscriptionStatusNameIn(KeyConstants.KEY_PROGRAM,startDate, endDate, KeyConstants.KEY_NEW, subscriptionStatusList);

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
            log.info("Get month subscription of new and renewal : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get all subscription of new and renewal ends.");
        return responseModel;
    }

    public ResponseModel getSubscriptionByPlatform(String startDateString, String endDateString) {
        log.info("Get subscription by platform starts");
        long start = new Date().getTime();
        long profilingStart;

        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));

        profilingStart = new Date().getTime();
        List<PlatformType> platformTypeList = platformTypeRepository.findAll();

        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);
        log.info("Platform type query and platform display names : Time taken in millis : "+(new Date().getTime() - profilingStart));


        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        profilingStart = new Date().getTime();
        for (PlatformType platformType : platformTypeList) {
            ChartEntry chartEntry = new ChartEntry();

            int count = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, statusList, platformType.getPlatformTypeId(), startDate, endDate);
            chartEntry.setEntryName(platFormDisplayName.getProperty(platformType.getPlatform(), platformType.getPlatform()));
            chartEntry.setCount(count);

            chartEntryList.add(chartEntry);
            totalCount += count;
        }
        log.info("Construct chart entry list(count query for each type) : Time taken in millis : "+(new Date().getTime() - profilingStart));


        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            List<String> chartEntryNames = platformTypeList.stream()
                    .map(platformType -> platFormDisplayName.getProperty(platformType.getPlatform().toLowerCase(), platformType.getPlatform()))
                    .collect(Collectors.toList());
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }

        profilingStart = new Date().getTime();
        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);
        log.info("Populate percentage : Time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();
        ChartData chartData = new ChartData();
        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);
        log.info("Chart data response construction : Time taken in millis : "+(new Date().getTime() - profilingStart));


        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);

        log.info("Get Subscription by platform : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get subscription by platform ends");
        return response;

    }

    public ResponseModel getPostWorkoutFeedbackAnalytics(String startDateString, String endDateString) {
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        List<Tuple> tuples = workoutFeedbackJPA.getWorkoutFeedbackCountByType(startDate, endDate);
        List<FeedbackTypes> feedbackTypes = feedbackTypesRepository.findAll();
        Collections.reverse(feedbackTypes);
        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;
        for(FeedbackTypes feedbackType : feedbackTypes){
            ChartEntry chartEntry = new ChartEntry();
            Long count = 0l;
            for(Tuple tuple : tuples){
                if((long)tuple.get(0) == feedbackType.getFeedbackTypeId().longValue()){
                    count = (Long)tuple.get(1);
                    break;
                }
            }
            chartEntry.setEntryName(feedbackType.getFeedbackType());
            chartEntry.setCount(count.intValue());
            chartEntryList.add(chartEntry);
            totalCount += count.intValue();
        }
        if (totalCount == 0) {
            List<String> chartEntryNames = chartEntryList.stream().map(chartEntry -> chartEntry.getEntryName()).collect(Collectors.toList());
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

    public ResponseModel getWorkoutDiscardFeedbackAnalytics(String startDateString, String endDateString) {
        long startTime = new Date().getTime();
        log.info("getWorkoutDiscardFeedbackAnalytics started");
        long temp = new Date().getTime();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Validate date and date range " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<DiscardWorkoutReasons> discardWorkoutReasons = discardWorkoutReasonsRepository.findAll();
        log.info("Get all workout reasons " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;
        for (DiscardWorkoutReasons discardWorkoutReason : discardWorkoutReasons) {
            ChartEntry chartEntry = new ChartEntry();
            int count = workoutDiscardFeedbackRepository.countByWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReasonAndCreatedDateBetween(discardWorkoutReason.getDiscardReason(), startDate, endDate);
            chartEntry.setEntryName(discardWorkoutReason.getDiscardReason());
            chartEntry.setCount(count);
            chartEntryList.add(chartEntry);
            totalCount += count;
        }
        log.info("Get count for all workout reasons " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            String[] discardReasons = {"Hard", "Easy", "Just Previewing", "Don't have equipment", "Others"};
            List<String> chartEntryNames = Arrays.asList(discardReasons);
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }
        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);
        log.info("Populate chart data " + (new Date().getTime() - temp));
        ChartData chartData = new ChartData();
        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);
        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("getWorkoutDiscardFeedbackAnalytics completed " + (new Date().getTime() - startTime));
        return response;
    }

    /**
     * Method to get list of ppl who reported workout discard feedback as Others.
     * @param pageNo
     * @param pageSize
     * @param startDateString
     * @param endDateString
     * @return
     */
    public ResponseModel getAllOtherDiscardWorkoutFeedBack(int pageNo, int pageSize, String startDateString, String endDateString) {
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAGINATION_ERROR, null);
        }

        List<DiscardWorkoutFeedbackResponse> discardWorkoutFeedbackResponses = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        Page<WorkoutDiscardFeedback> workoutDiscardFeedbackPage = workoutDiscardFeedbackRepository.findByWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReasonAndCreatedDateBetween(DBConstants.OTHERS, startDate, endDate, pageRequest);
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

    public ChartData getFlaggingReasonDistribution(String startDateString, String endDateString) {
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        List<FlaggedVideoReason> FlaggedVideoReasonList = flaggedVideoReasonsRepository.findAll();

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;
        for (FlaggedVideoReason flaggedVideoReason : FlaggedVideoReasonList) {
            ChartEntry chartEntry = new ChartEntry();

            int count = flaggedExerciseRepository.countByFlaggedVideoReasonAndCreatedDateBetween(flaggedVideoReason, startDate, endDate);
            chartEntry.setEntryName(flaggedVideoReason.getFeedbackReason());
            chartEntry.setCount(count);

            chartEntryList.add(chartEntry);
            totalCount += count;
        }

        ChartData chartData = new ChartData();

        if (totalCount == 0) {
            //TODO : Remove dummy data and return null once Front End development is completed
            List<FlaggedVideoReason> flaggedVideoReasonList = flaggedVideoReasonsRepository.findAll();
            List<String> chartEntryNames = flaggedVideoReasonList.stream().map(flaggedVideoReason -> flaggedVideoReason.getFeedbackReason()).collect(Collectors.toList());
            ResponseModel responseModel = new ChartDataUtils().getChartDummyData(chartEntryNames);
            chartData = (ChartData) responseModel.getPayload();
        } else {
            new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);
            chartData.setTotalCount(totalCount);
            chartData.setChartEntryList(chartEntryList);
        }
        return chartData;

    }

    public GraphData getFlaggedVideoTrendForYear(String startDateString, String endDateString) {
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Period diff = Period.between(startLocalDate, endLocalDate);

        int years = diff.getYears();
        int noOfMonths = diff.getMonths();
        noOfMonths = noOfMonths + (years * 12);

        Date tempDate = new Date(startDate.getTime());

        List<GraphEntry> graphEntryList = new ArrayList<>();
        int totalCount = 0;

        //Getting each month's last date data
        for (int i = 0; i <= noOfMonths; i++) {
            int monthOffset = 1;
            //data fetch starting from startDate month's first day
            if (i == 0) {
                monthOffset = 0;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(tempDate);
            cal.add(Calendar.MONTH, monthOffset);
            cal.set(Calendar.DATE, 1);
            tempDate = cal.getTime();

            Date startTime = new Date(tempDate.getTime());
            //If a month's last day is AFTER the startDate param, endDate is considered as the day for getting data
            //occurs on first iteration of loop
            if (startTime.before(startDate)) {
                startTime = startDate;
            }

            //setting the end time of the month as the end time
            cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endTime = cal.getTime();
            //If a month's last day is AFTER the endDate param, endDate is considered as the day for getting data
            //occurs on last iteration of loop
            if (endTime.after(endDate)) {
                endTime = endDate;
            }

            int flagCount = flaggedExerciseRepository.countByCreatedDateBetween(startTime, endTime);

            GraphEntry graphEntry = new GraphEntry();
            graphEntry.setPeriodId(i + 1);
            String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            graphEntry.setPeriodName(monthName);
            graphEntry.setEntryCount(flagCount);

            graphEntryList.add(graphEntry);
            totalCount += flagCount;
        }

        GraphData graphData = new GraphData();
        graphData.setTotalCount(totalCount);
        graphData.setGraphEntryList(graphEntryList);

        return graphData;
    }

    public GraphData getFlaggedVideoTrendForMonth(String startDateString, String endDateString) {
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

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

        List<GraphEntry> graphEntryList = new ArrayList<>();
        int totalCount = 0;

        //Getting each week's last date data
        for (int i = 0; i < weekSplit.length; i++) {
            String week[] = weekSplit[i].split("-");
            int firstDayOfWeek = Integer.parseInt(week[0]);
            int lastDayOfWeek = Integer.parseInt(week[1]);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.set(Calendar.DATE, firstDayOfWeek);
            Date startTime = calendar.getTime();

            //If the first day of this week is BEFORE the startDate param, we skip the week
            //occurs on first iteration of loop
            if (startTime.before(startDate)) {
                startTime = startDate;
            }

            //setting the end time of the day as the end time
            calendar.set(Calendar.DATE, lastDayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endTime = calendar.getTime();
            //If the last day of this week is BEFORE the startDate param, we skip the week
            //occurs on first iteration of loop
            if (endTime.before(startDate)) {
                continue;
            }
            //If a week's last day is AFTER the endDate param, endDate is considered as the day for getting data
            //occurs on last iteration of loop
            if (endTime.after(endDate)) {
                endTime = endDate;
            }


            int flagCount = flaggedExerciseRepository.countByCreatedDateBetween(startTime, endTime);

            GraphEntry graphEntry = new GraphEntry();
            graphEntry.setPeriodId(i + 1);
            graphEntry.setPeriodName("W" + (i + 1));
            graphEntry.setEntryCount(flagCount);
            graphEntryList.add(graphEntry);

            totalCount += flagCount;
        }

        GraphData graphData = new GraphData();
        graphData.setTotalCount(totalCount);
        graphData.setGraphEntryList(graphEntryList);

        return graphData;

    }


    public ResponseModel getRevenueSplitByPlatform(String startDateString, String endDateString) {
        log.info("Get Revenue split by platform starts");
        long start = System.currentTimeMillis();
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();

        Properties platFormDisplayName = new Properties();
        platFormDisplayName.setProperty(DBConstants.ANDROID, KeyConstants.KEY_ANDROID_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.IOS, KeyConstants.KEY_IOS_DISPLAY_NAME);
        platFormDisplayName.setProperty(DBConstants.WEB, KeyConstants.KEY_WEB_DISPLAY_NAME);

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        Long temp = new Date().getTime();
        List<RevenueByPlatform> revenueByPlatformList = platformTypeJPA.getRevenueByPlatformAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, startDate, endDate);
        log.info("Query : Time taken in millis : " + (new Date().getTime() - temp));

        List<ChartEntryWithValue<Double>> chartEntryList = new ArrayList<>();
        double totalRevenue = 0.0;

        temp = System.currentTimeMillis();
        for (RevenueByPlatform revenueByPlatform : revenueByPlatformList) {
            double price = 0.0;
            if (revenueByPlatform.getRevenue() != null)
                price = revenueByPlatform.getRevenue();
            String platformTypeName = revenueByPlatform.getPlatformName();

            ChartEntryWithValue<Double> chartEntry = new ChartEntryWithValue<>();
            chartEntry.setEntryName(platFormDisplayName.getProperty(platformTypeName, platformTypeName));

            price = Math.round(price * 100.0) / 100.0;
            chartEntry.setValue(price);
            chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR  + decimalFormat.format(price));
            totalRevenue += price;

            chartEntryList.add(chartEntry);
        }

        new ChartDataUtils().populationEntryPercentages(chartEntryList, totalRevenue);

        ChartDataWithValue<Double> chartData = new ChartDataWithValue<>();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        chartData.setTotalValue(totalRevenue);
        chartData.setFormattedTotalValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));
        chartData.setChartEntryList(chartEntryList);
        log.info("Query and response construction : Time taken in millis : "+(System.currentTimeMillis() - temp));


        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Get revenue split by platform : Total Time taken in millis : "+(System.currentTimeMillis() - start));
        log.info("Get Revenue split by platform ends");

        return response;
    }


    public ResponseModel getRevenueSplitByPayout(String startDateString, String endDateString) {
        log.info("Get Revenue split by payout starts");
        long start = new Date().getTime();
        long profilingStart;
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        List<ChartEntryWithValue<Double>> chartEntryList = new ArrayList<>();

        double totalRevenue = 0.0;
        double instructorShare = 0.0;
        double trainnrRevenue = 0.0;
        double tax = 0.0;
        profilingStart = new Date().getTime();
        log.info("Subscription audit query : Time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();

        InstructorPayout instructorPayout = subscriptionAuditJPA.getInstructorPayoutBySubscriptionType(startDate, endDate);
        if (instructorPayout.getTotalRevenue() != null)
            totalRevenue = instructorPayout.getTotalRevenue();
        if (instructorPayout.getTrainnrRevenue() != null)
            trainnrRevenue = instructorPayout.getTrainnrRevenue();
        if (instructorPayout.getTax() != null)
            tax = instructorPayout.getTax();
        if (instructorPayout.getInstructorShare() != null)
            instructorShare = instructorPayout.getInstructorShare();

        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        trainnrRevenue = Math.round(trainnrRevenue * 100.0) / 100.0;
        tax = Math.round(tax * 100.0) / 100.0;
        instructorShare = Math.round(instructorShare * 100.0) / 100.0;
        log.info("Get Revenue details from instructor payment : Time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();
        List<String> chartEntryNames = Arrays.asList(new String[]{KeyConstants.KEY_TRAINNR_REVENUE, KeyConstants.KEY_INSTRUCTOR_SHARE, KeyConstants.KEY_TAX});
        for (String chartEntryName : chartEntryNames) {
            ChartEntryWithValue<Double> chartEntry = new ChartEntryWithValue<>();

            if (chartEntryName.equalsIgnoreCase(KeyConstants.KEY_TRAINNR_REVENUE)) {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setValue(trainnrRevenue);
                chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(trainnrRevenue));
            } else if (chartEntryName.equalsIgnoreCase(KeyConstants.KEY_INSTRUCTOR_SHARE)) {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setValue(instructorShare);
                chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorShare));
            } else {
                chartEntry.setEntryName(chartEntryName);
                chartEntry.setValue(tax);
                chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(tax));
            }
            chartEntryList.add(chartEntry);

        }

        long temp  = new Date().getTime();
        new ChartDataUtils().populationEntryPercentages(chartEntryList, totalRevenue);
        log.info("Get percentage for chart entry : Time taken in millis : "+(new Date().getTime() - temp));


        ChartDataWithValue<Double> chartData = new ChartDataWithValue<>();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        chartData.setTotalValue(totalRevenue);
        chartData.setFormattedTotalValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));
        chartData.setChartEntryList(chartEntryList);
        log.info("Construct chart response : Time taken in millis : "+(new Date().getTime() - profilingStart));



        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);
        log.info("Get revenue split by payout : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get revenue split by payout ends");
        return response;
    }

    public ResponseModel getAdminDashboardOverviewData() {
        log.info("Get admin dashboard overview starts");
        long start = new Date().getTime();
        Map<String, Object> overview = new HashMap<>();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        double totalRevenue = 0.0;
        double trainnrShare = 0.0;
        double instructorPayout = 0.0;
        double totalRevenueForLastMonth = 0.0;
        double trainnrShareForLastMonth = 0.0;
        double instructorPayoutForLastMonth = 0.0;

        long profilingStart = new Date().getTime();
        Calendar cal = Calendar.getInstance();
        LocalDate date = LocalDate.now();
        String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

        //calculating previous month's last date - for growth rate
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date lastMonthLastDate = cal.getTime();

        //calculating previous month's first date - for growth rate
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date lastMonthFirstDate = cal.getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date firstDayOfTheMonth = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date lastDayOfTheMonth = calendar.getTime();
        log.info("Getting previous and current month's first and last dates : Time taken in millis : "+(new Date().getTime() - profilingStart));

        profilingStart = new Date().getTime();
        InstructorPayout instructorPayoutForCurrentMonth = subscriptionAuditJPA.getInstructorPayoutBySubscriptionType(firstDayOfTheMonth, lastDayOfTheMonth);
        if (instructorPayoutForCurrentMonth.getTotalRevenue() != null)
            totalRevenue = instructorPayoutForCurrentMonth.getTotalRevenue();
        if (instructorPayoutForCurrentMonth.getTrainnrRevenue() != null)
            trainnrShare = instructorPayoutForCurrentMonth.getTrainnrRevenue();
        if (instructorPayoutForCurrentMonth.getInstructorShare() != null)
            instructorPayout = instructorPayoutForCurrentMonth.getInstructorShare();

        log.info("Getting current month's revenue details : Time taken in millis : "+(new Date().getTime() - profilingStart));

        profilingStart = new Date().getTime();
        InstructorPayout instructorPayoutForLastMonthSpec = subscriptionAuditJPA.getInstructorPayoutBySubscriptionType(lastMonthFirstDate, lastMonthLastDate);
        if (instructorPayoutForLastMonthSpec.getTotalRevenue() != null)
            totalRevenueForLastMonth = instructorPayoutForLastMonthSpec.getTotalRevenue();
        if (instructorPayoutForLastMonthSpec.getTrainnrRevenue() != null)
            trainnrShareForLastMonth = instructorPayoutForLastMonthSpec.getTrainnrRevenue();
        if (instructorPayoutForLastMonthSpec.getInstructorShare() != null)
            instructorPayoutForLastMonth = instructorPayoutForLastMonthSpec.getInstructorShare();

        log.info("Getting previous month's revenue details : Time taken in millis : "+(new Date().getTime() - profilingStart));

        //Current month and year
        overview.put(KeyConstants.KEY_MONTH, monthName + " " + date.getYear());


        //Net Revenue in fiwise platform
        profilingStart = new Date().getTime();
        Map<String, Object> netRevenue = new HashMap<>();
        netRevenue.put(KeyConstants.KEY_REVENUE, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));
        if(totalRevenue >= totalRevenueForLastMonth){
            String formattedDiffPercent = getGrowthPercentageForRevenue(totalRevenueForLastMonth, totalRevenue);
            netRevenue.put("isGrowth", true);
            netRevenue.put("diffPercent", formattedDiffPercent);
        }else{
            String formattedDiffPercent = getGrowthPercentageForRevenue(totalRevenueForLastMonth, totalRevenue);
            netRevenue.put("isGrowth", false);
            netRevenue.put("diffPercent", formattedDiffPercent);
        }
        overview.put(KeyConstants.KEY_NET_REVENUE, netRevenue);
        log.info("Getting growth percentage for total revenue : Time taken in millis : "+(new Date().getTime() - profilingStart));


        //Instructor payout
        profilingStart = new Date().getTime();
        Map<String, Object> instructorPayOut = new HashMap<>();
        instructorPayOut.put(KeyConstants.KEY_INSTRUCTOR_PAID_AMOUNT, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(instructorPayout));
        if(instructorPayout >= instructorPayoutForLastMonth){
            instructorPayOut.put("isGrowth", true);
            String formattedDiffPercent = getGrowthPercentageForRevenue(instructorPayoutForLastMonth, instructorPayout);
            instructorPayOut.put("diffPercent", formattedDiffPercent);
        }else{
            String formattedDiffPercent = getGrowthPercentageForRevenue(instructorPayoutForLastMonth, instructorPayout);
            instructorPayOut.put("isGrowth", false);
            instructorPayOut.put("diffPercent", formattedDiffPercent);
        }
        overview.put(KeyConstants.KEY_INSTRUCTOR_PAYOUT, instructorPayOut);
        log.info("Getting growth percentage for instructor payout : Time taken in millis : "+(new Date().getTime() - profilingStart));


        //Trainer Revenue
        profilingStart = new Date().getTime();
        Map<String, Object> trainerRevenue = new HashMap<>();
        trainerRevenue.put(KeyConstants.KEY_REVENUE, KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(trainnrShare));
        if(trainnrShare >= trainnrShareForLastMonth){
            String formattedDiffPercent = getGrowthPercentageForRevenue(trainnrShareForLastMonth, trainnrShare);
            trainerRevenue.put("isGrowth", true);
            trainerRevenue.put("diffPercent", formattedDiffPercent);
        }else{
            String formattedDiffPercent = getGrowthPercentageForRevenue(trainnrShareForLastMonth, trainnrShare);
            trainerRevenue.put("isGrowth", false);
            trainerRevenue.put("diffPercent", formattedDiffPercent);
        }
        overview.put(KeyConstants.KEY_TRAINER_REVENUE, trainerRevenue);
        log.info("Getting growth percentage for trainnr revenue : Time taken in millis : "+(new Date().getTime() - profilingStart));


        //Total Member Count
        profilingStart = new Date().getTime();
        UserRole memberRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_MEMBER);
        if (memberRole == null) {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.ERROR, null);
        }
        List<User> membersList = userRepository.findByUserRoleMappingsUserRole(memberRole);
        log.info("Getting users based on role query : time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();
        List<DeletedUserAudit> deletedUserAudits = deletedUserAuditRepository.findAll();
        List<BlockedUser> blockedUsers = blockedUserRepository.findAll();
        log.info("Getting Deleted and blocked users - query : time taken in millis : "+(new Date().getTime() - profilingStart));
        List<DeletedOrBlockedUserView> deletedUsers = new ArrayList<>();

        List<DeletedOrBlockedUserView> blockedUsersList = new ArrayList<>();
        List<DeletedOrBlockedUserView> deletedAndBlockedUsersList = new ArrayList<>();


        //Getting deleted users
        profilingStart = new Date().getTime();
        for (DeletedUserAudit deletedUserAudit : deletedUserAudits) {
            DeletedOrBlockedUserView deletedOrBlockedUsersView = new DeletedOrBlockedUserView();
            deletedOrBlockedUsersView.setUserId(deletedUserAudit.getUser().getUserId());
            deletedOrBlockedUsersView.setRoleId(deletedUserAudit.getUserRole().getRoleId());
            deletedUsers.add(deletedOrBlockedUsersView);
        }
        log.info("Getting Deleted users : time taken in millis : "+(new Date().getTime() - profilingStart));


        //Getting blocked users
        profilingStart = new Date().getTime();
        for (BlockedUser blockedUser : blockedUsers) {
            DeletedOrBlockedUserView deletedOrBlockedUsersView = new DeletedOrBlockedUserView();
            deletedOrBlockedUsersView.setUserId(blockedUser.getUser().getUserId());
            deletedOrBlockedUsersView.setRoleId(blockedUser.getUserRole().getRoleId());
            blockedUsersList.add(deletedOrBlockedUsersView);
        }
        log.info("Getting Blocked users : time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();
        for (DeletedOrBlockedUserView deletedOrBlockedUserView : blockedUsersList) {
            for (DeletedOrBlockedUserView deletedOrBlockedUser : deletedUsers) {
                if (deletedOrBlockedUserView.getUserId() == deletedOrBlockedUser.getUserId() && deletedOrBlockedUserView.getRoleId() == deletedOrBlockedUser.getRoleId()) {
                    deletedAndBlockedUsersList.add(deletedOrBlockedUserView);
                    break;
                }
            }
        }
        log.info("Getting both blocked and deleted users : time taken in millis : "+(new Date().getTime() - profilingStart));

        List<DeletedOrBlockedUserView> deletedOrBlockedMembers = new ArrayList<>();
        List<DeletedOrBlockedUserView> deletedOrBlockedInstructors = new ArrayList<>();

        profilingStart = new Date().getTime();
        for (DeletedOrBlockedUserView deletedOrBlockedUserView : deletedAndBlockedUsersList) {
            if (deletedOrBlockedUserView.getRoleId() == memberRole.getRoleId()) {
                deletedOrBlockedMembers.add(deletedOrBlockedUserView);
            } else {
                deletedOrBlockedInstructors.add(deletedOrBlockedUserView);
            }
        }
        log.info("Getting Deleted and blocked users based on role : time taken in millis : "+(new Date().getTime() - profilingStart));

        List<BlockedUser> blockedMembers = new ArrayList<>();
        List<BlockedUser> blockedInstructors = new ArrayList<>();

        profilingStart = new Date().getTime();
        for (BlockedUser blockedUser : blockedUsers) {
            if (blockedUser.getUserRole().getRoleId() == memberRole.getRoleId()) {
                blockedMembers.add(blockedUser);
            } else {
                blockedInstructors.add(blockedUser);
            }
        }
        int totalMembersCount = membersList.size() - (blockedMembers.size() - deletedOrBlockedMembers.size());
        log.info("Getting member count : time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();
        int totalMembersUntilLastMonth = userRepository.countByUserRoleMappingsUserRoleAndCreatedDateLessThan(memberRole, lastMonthLastDate);
        log.info("Getting members count until last month : time taken in millis : "+(new Date().getTime() - profilingStart));

        String memberDiffPercentStr = "-";

        profilingStart = new Date().getTime();
        boolean isMemberGrowth = false;
        if (totalMembersCount >= totalMembersUntilLastMonth) {
            isMemberGrowth = true;
        }
        if (totalMembersUntilLastMonth > 0) {
            int diff = Math.abs(totalMembersCount - totalMembersUntilLastMonth);
            float diffPercent = ((((float) diff) * 100) / (float) totalMembersUntilLastMonth);
            memberDiffPercentStr = decimalFormat.format(diffPercent) + "%";
        }

        Map<String, Object> memberCount = new HashMap<>();
        memberCount.put(KeyConstants.KEY_TOTAL_COUNT, totalMembersCount);
        memberCount.put("isGrowth", isMemberGrowth);
        memberCount.put("diffPercent", memberDiffPercentStr);
        overview.put(KeyConstants.KEY_MEMBER_COUNT, memberCount);
        log.info("Getting Member count growth percentage : time taken in millis : "+(new Date().getTime() - profilingStart));


        //Total Instructor Count
        profilingStart = new Date().getTime();
        UserRole instructorRole = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
        if (instructorRole == null) {
            throw new ApplicationException(Constants.NOT_FOUND, MessageConstants.ERROR, null);
        }
        List<User> instructorsList = userRepository.findByUserRoleMappingsUserRole(instructorRole);
        int totalInstructorsCount = instructorsList.size() - (blockedInstructors.size() - deletedOrBlockedInstructors.size());
        log.info("Getting total instructor count : time taken in millis : "+(new Date().getTime() - profilingStart));

        profilingStart = new Date().getTime();
        int totalInstructorsUntilLastMonth = userRepository.countByUserRoleMappingsUserRoleAndCreatedDateLessThan(instructorRole, lastMonthLastDate);
        log.info("Getting instructor count until last month : time taken in millis : "+(new Date().getTime() - profilingStart));

        String insDiffPercentStr = "-";

        profilingStart = new Date().getTime();
        boolean isInsGrowth = false;
        if (totalInstructorsCount >= totalInstructorsUntilLastMonth) {
            isInsGrowth = true;
        }
        if (totalInstructorsUntilLastMonth > 0) {
            int diff = Math.abs(totalInstructorsCount - totalInstructorsUntilLastMonth);
            float diffPercent = ((((float) diff) * 100) / (float) totalInstructorsUntilLastMonth);
            insDiffPercentStr = decimalFormat.format(diffPercent) + "%";
        }

        Map<String, Object> instructorCount = new HashMap<>();
        instructorCount.put(KeyConstants.KEY_TOTAL_COUNT, totalInstructorsCount);
        instructorCount.put("isGrowth", isInsGrowth);
        instructorCount.put("diffPercent", insDiffPercentStr);
        overview.put(KeyConstants.KEY_INSTRUCTOR_COUNT, instructorCount);
        log.info("Getting instructor count growth percentage : time taken in millis : "+(new Date().getTime() - profilingStart));


        //Total published programs count
        profilingStart = new Date().getTime();
        int totalProgramCount = programRepository.findByStatus(InstructorConstant.PUBLISH).size();
        int totalProgramsUntilLastMonth = programRepository.countByStatusAndModifiedDateLessThan(KeyConstants.KEY_PUBLISH, lastMonthLastDate);
        log.info("Getting program count query : time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();
        String programDiffPercentStr = "-";

        boolean isProgramGrowth = false;
        if (totalProgramCount >= totalProgramsUntilLastMonth) {
            isProgramGrowth = true;
        }
        if (totalProgramsUntilLastMonth > 0) {
            int diff = Math.abs(totalProgramCount - totalProgramsUntilLastMonth);
            float diffPercent = ((((float) diff) * 100) / (float) totalProgramsUntilLastMonth);
            programDiffPercentStr = decimalFormat.format(diffPercent) + "%";
        }

        Map<String, Object> programCount = new HashMap<>();
        programCount.put(KeyConstants.KEY_TOTAL_COUNT, totalProgramCount);
        programCount.put("isGrowth", isProgramGrowth);
        programCount.put("diffPercent", programDiffPercentStr);
        overview.put(KeyConstants.KEY_PROGRAM_COUNT, programCount);
        log.info("Getting program count growth percentage : time taken in millis : "+(new Date().getTime() - profilingStart));


        profilingStart = new Date().getTime();
        //App rating
        Map<String, Object> appRating = new HashMap<>();
        appRating.put(KeyConstants.KEY_ANDROID_APP_RATING, 4.5);
        appRating.put(KeyConstants.KEY_IOS_APP_RATING, 3.5);
        overview.put(KeyConstants.KEY_APP_RATING, appRating);
        log.info("Getting Rating : time taken in millis : "+(new Date().getTime() - profilingStart));


        ResponseModel responseModel = new ResponseModel();
        responseModel.setPayload(overview);
        responseModel.setStatus(Constants.SUCCESS_STATUS);
        responseModel.setMessage(MessageConstants.MSG_REVENUE_AND_STATISTICS_FETCHED);
        log.info("Get admin dashboard overview : total time taken in millis : "+(new Date().getTime() - start));
        log.info("Get admin dashboard overview ends");
        return responseModel;
    }



    public ResponseModel getRevenueEarnedForYear(String startDateString, String endDateString) throws ParseException {
        log.info("Get revenue earned for year starts");
        long start = new Date().getTime();
        long profilingStart;
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        double totalRevenue = 0.0;

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
            double revenue = 0.0;
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
            Double totalSpentBySubscriptionType = subscriptionAuditJPA.getTotalSpentBySubscriptionType(KeyConstants.KEY_PROGRAM, statusList, startTime, endTime);
            if (totalSpentBySubscriptionType != null)
                revenue = totalSpentBySubscriptionType;
            revenue = Math.round(revenue * 100.0) / 100.0;
            totalRevenue += revenue;
            graphEntryWithValue.setValue(revenue);
            graphEntryWithValue.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR +  decimalFormat.format(revenue));
            graphEntryWithValues.add(graphEntryWithValue);
        }
        log.info("Get revenue for each month of a year : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();

        GraphDataWithValue<Double> graphData = new GraphDataWithValue<>();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        graphData.setOverAllValue(totalRevenue);
        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));

        graphData.setGraphEntryList(graphEntryWithValues);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);
        log.info("Calculating total revenue and making up the response model : Time taken in millis : "+(new Date().getTime() - profilingStart));

        log.info("Get revenue earned for year : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get revenue earned for year ends");

        return response;

    }


    public ResponseModel getRevenueEarnedForMonth(String startDateString, String endDateString) {
        log.info("Get revenue earned for month starts");
        long start = new Date().getTime();
        long profilingStart;
        DateRange dateRange = graphUtils.getDateRangeForChart(startDateString, endDateString);
        Date startDate = dateRange.getStartDate();
        Date endDate = dateRange.getEndDate();
        log.info("Get Date Range : Time taken in millis : "+(new Date().getTime() - start));
        profilingStart = new Date().getTime();

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        double totalRevenue = 0.0;

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
            double revenue = 0.0;
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
            Double totalSpentBySubscriptionType = subscriptionAuditJPA.getTotalSpentBySubscriptionType(KeyConstants.KEY_PROGRAM, statusList, startTime, endTime);
            if (totalSpentBySubscriptionType != null)
                revenue = totalSpentBySubscriptionType;

            revenue = Math.round(revenue * 100.0) / 100.0;
            totalRevenue += revenue;
            graphEntryWithValue.setValue(revenue);
            graphEntryWithValue.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(revenue));
            graphEntryWithValues.add(graphEntryWithValue);

        }
        log.info("Get revenue for each week of a month : Time taken in millis : "+(new Date().getTime() - profilingStart));
        profilingStart = new Date().getTime();

        GraphDataWithValue<Double> graphData = new GraphDataWithValue<>();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;
        graphData.setOverAllValue(totalRevenue);
        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalRevenue));

        graphData.setGraphEntryList(graphEntryWithValues);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);
        log.info("Calculating total revenue and making up the response model : Time taken in millis : "+(new Date().getTime() - profilingStart));

        log.info("Get revenue earned for year : Total Time taken in millis : "+(new Date().getTime() - start));
        log.info("Get revenue earned for year ends");

        return response;
    }

    public String getGrowthPercentageForRevenue(double lastMonthRevenue, double currentMonthRevenue){
        String formattedDiffPercent;
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        if(currentMonthRevenue >= lastMonthRevenue){
            double diff = currentMonthRevenue - lastMonthRevenue;
            float diffPercent = ((((float) diff) * 100) / (float) lastMonthRevenue);
            formattedDiffPercent = decimalFormat.format(diffPercent) + "%";
        }else{
            double diff = lastMonthRevenue - currentMonthRevenue;
            float diffPercent = ((((float) diff) * 100) / (float) lastMonthRevenue);
            formattedDiffPercent = decimalFormat.format(diffPercent) + "%";
        }
        if(lastMonthRevenue == 0.0){
            formattedDiffPercent = "-";
        }
        return formattedDiffPercent;
    }

}
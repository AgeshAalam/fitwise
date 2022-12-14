package com.fitwise.program.graph;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.entity.Gender;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.repository.GenderRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.graph.ChartData;
import com.fitwise.view.graph.ChartEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 17/03/20
 */

@Component
@Slf4j
public class ProgramClientDemographicsByGender implements ProgramClientDemographicsGraph {

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    GenderRepository genderRepository;

    @Autowired
    private SubscriptionAuditRepo subscriptionAuditRepo;

    /**
     * Get Client Demographics data Of a program By User Gender
     *
     * @param programId
     * @return
     */
    @Override
    public ResponseModel getProgramClientDemographics(long programId, DateRange dateRange) {
        log.info("Get program client demographics for gender starts.");
        long apiStartTimeMillis = new Date().getTime();
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        List<SubscriptionAudit> subscriptionAudits = subscriptionAuditRepo.findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramIdAndSubscriptionDateBetween
                (KeyConstants.KEY_PROGRAM, statusList, programId, dateRange.getStartDate(), dateRange.getEndDate());
        log.info("Query to get subscription audit list : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();


        List<User> userList = subscriptionAudits.stream()
                .filter(subscriptionAudit -> subscriptionAudit.getUser() != null)
                .map(subscriptionAudit -> subscriptionAudit.getUser())
                .collect(Collectors.toList());

        List<UserProfile> userProfileList = userProfileRepository.findByUserIn(userList);
        log.info("Extract users and query to get user profile list : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        Map<String, Integer> genderMap = new HashMap<>();

        for (UserProfile userProfile : userProfileList) {
            Gender gender = userProfile.getGender();
            String genderType;
            if (gender == null) {
                genderType = DBConstants.PREFER_NOT_TO_SAY;
            } else {
                genderType = gender.getGenderType();
            }
            int count = genderMap.get(genderType) == null ? 1 : genderMap.get(genderType) + 1;
            genderMap.put(genderType, count);
        }
        log.info("Get gender stats : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        ChartData chartData = new ChartData();

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        for (Map.Entry<String, Integer> entry : genderMap.entrySet()) {
            ChartEntry chartEntry = new ChartEntry();
            chartEntry.setEntryName(entry.getKey());
            chartEntry.setCount(entry.getValue());

            chartEntryList.add(chartEntry);
            totalCount += entry.getValue();
        }
        log.info("Construct chart entry : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        profilingEndTimeMillis = new Date().getTime();

        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
            List<Gender> genderList = genderRepository.findAll();
            List<String> chartEntryNames = genderList.stream().map(gender -> gender.getGenderType()).collect(Collectors.toList());
            log.info("Query to get gender list and get chart entry names : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
            log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
            log.info("Get program client demographics for gender ends.");
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
        log.info("Populate entry percentage and construct response model : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get program client demographics for gender ends.");

        return response;

    }
}

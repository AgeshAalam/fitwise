package com.fitwise.program.graph;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.graph.ChartData;
import com.fitwise.view.graph.ChartEntry;
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
public class ProgramClientDemographicsByAge implements ProgramClientDemographicsGraph {

    @Autowired
    ProgramSubscriptionRepo programSubscriptionRepo;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    private SubscriptionAuditRepo subscriptionAuditRepo;

    /**
     * Get Client Demographics data Of a program By User Age
     *
     * @param programId
     * @return
     */
    @Override
    public ResponseModel getProgramClientDemographics(long programId, DateRange dateRange) {
        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});
        List<SubscriptionAudit> subscriptionAudits = subscriptionAuditRepo.findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramIdAndSubscriptionDateBetween
                (KeyConstants.KEY_PROGRAM, statusList, programId, dateRange.getStartDate(), dateRange.getEndDate());

        List<User> userList = subscriptionAudits.stream()
                .filter(subscriptionAudit -> subscriptionAudit.getUser() != null)
                .map(subscriptionAudit -> subscriptionAudit.getUser())
                .collect(Collectors.toList());

        List<UserProfile> userProfileList = userProfileRepository.findByUserIn(userList);

        Map<String, Integer> AgeMap = new HashMap<>();

        for (UserProfile userProfile : userProfileList) {
            String dob = userProfile.getDob();
            int age = ValidationUtils.getAgeFromDob(dob);
            String ageGroup = "Others";
            if (age > 17 && age < 25) {
                ageGroup = "18-24";
            } else if (age > 24 && age < 45) {
                ageGroup = "25-44";
            } else if (age > 44 && age < 60) {
                ageGroup = "45-60";
            }

            int count = AgeMap.get(ageGroup) == null ? 1 : AgeMap.get(ageGroup) + 1;
            AgeMap.put(ageGroup, count);
        }

        ChartData chartData = new ChartData();

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        for (Map.Entry<String, Integer> entry : AgeMap.entrySet()) {
            ChartEntry chartEntry = new ChartEntry();
            chartEntry.setEntryName(entry.getKey());
            chartEntry.setCount(entry.getValue());

            chartEntryList.add(chartEntry);
            totalCount += entry.getValue();
        }

        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
            List<String> chartEntryNames = Arrays.asList(new String[]{"Others", "18-24", "25-44", "45-60"});
            return new ChartDataUtils().getChartDummyData(chartEntryNames);
        }

        new ChartDataUtils().populationEntryPercentage(chartEntryList, totalCount);

        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);


        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);

        return response;
    }
}

package com.fitwise.instructor.graph;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.utils.ChartDataUtils;
import com.fitwise.utils.DateRange;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.graph.ChartData;
import com.fitwise.view.graph.ChartEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/*
 * Created by Vignesh G on 12/03/20
 */
@Component
@Slf4j
public class ClientDemographicsByAge implements ClientDemographicsGraph {


    @Autowired
    private SubscriptionAuditJPA subscriptionAuditJPA;

    /**
     * Get Client Demographics Of an instructor By Age Group chart data
     *
     * @param instructor
     * @param dateRange
     * @return
     */
    @Override
    public ResponseModel getClientDemographics(User instructor, DateRange dateRange) {
        long profilingStartTimeMillis = System.currentTimeMillis();

        List<String> statusList = Arrays.asList(new String[]{KeyConstants.KEY_PAID, KeyConstants.KEY_PAYMENT_PENDING});

        List<Long> userIdList = subscriptionAuditJPA.getUserIdListByInstructorId(statusList, instructor.getUserId(), dateRange.getStartDate(), dateRange.getEndDate());
        List<String> dobList = subscriptionAuditJPA.getDOBByUserIdList(userIdList);

        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        Map<String, Integer> AgeMap = new HashMap<>();
        for (String dob : dobList) {
            String ageGroup;
            if (dob == null) {
                ageGroup = "Not specified";
            } else {
                int age = ValidationUtils.getAgeFromDob(dob);
                ageGroup = "Others";
                if (age < 18) {
                    ageGroup = "Under 18";
                } else if (age > 17 && age < 25) {
                    ageGroup = "18-24";
                } else if (age > 24 && age < 45) {
                    ageGroup = "25-44";
                } else if (age > 44 && age <= 60) {
                    ageGroup = "45-60";
                } else if (age > 60) {
                    ageGroup = "Over 60";
                }
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

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        return response;
    }
}

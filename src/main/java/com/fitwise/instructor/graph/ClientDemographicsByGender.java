package com.fitwise.instructor.graph;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.Gender;
import com.fitwise.entity.User;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.repository.GenderRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.subscription.SubscriptionAuditRepo;
import com.fitwise.specifications.jpa.SubscriptionAuditJPA;
import com.fitwise.specifications.jpa.dao.GenderDao;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/*
 * Created by Vignesh G on 11/03/20
 */
@Component
@Slf4j
public class ClientDemographicsByGender implements ClientDemographicsGraph {

    @Autowired
    GenderRepository genderRepository;

    @Autowired
    private SubscriptionAuditJPA subscriptionAuditJPA;


    /**
     * Get Client Demographics Of an instructor By Gender chart data
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

        List<GenderDao> userGenderListNotNull = new ArrayList<>();
        if(!userIdList.isEmpty()){
           userGenderListNotNull = subscriptionAuditJPA.getGenderByUserIdList(userIdList);
        }

        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        Map<String, Integer> genderMap = userGenderListNotNull.stream()
                .collect(Collectors.toMap(genderDao -> genderDao.getGenderType(), genderDao -> Math.toIntExact(genderDao.getCount())));
        if(!userIdList.isEmpty()){
            int genderNullCount = Math.toIntExact(subscriptionAuditJPA.getCountWhereGenderIsNullByUserIdList(userIdList));
            int count = genderMap.get(DBConstants.PREFER_NOT_TO_SAY) == null ? genderNullCount : genderMap.get(DBConstants.PREFER_NOT_TO_SAY) + genderNullCount;
            genderMap.put(DBConstants.PREFER_NOT_TO_SAY, count);
        }
        ChartData chartData = new ChartData();

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        for (Map.Entry<String, Integer> entry : genderMap.entrySet()) {
            if(entry.getValue() != 0){
                ChartEntry chartEntry = new ChartEntry();
                chartEntry.setEntryName(entry.getKey());
                chartEntry.setCount(entry.getValue());

                chartEntryList.add(chartEntry);
                totalCount += entry.getValue();
            }
        }

        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
            List<Gender> genderList = genderRepository.findAll();
            List<String> chartEntryNames = genderList.stream().map(gender -> gender.getGenderType()).collect(Collectors.toList());
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

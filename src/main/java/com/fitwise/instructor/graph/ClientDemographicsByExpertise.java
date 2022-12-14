package com.fitwise.instructor.graph;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.User;
import com.fitwise.repository.ExpertiseLevelRepository;
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
import java.util.List;
import java.util.stream.Collectors;

/*
 * Created by Vignesh G on 12/03/20
 */
@Component
@Slf4j
public class ClientDemographicsByExpertise implements ClientDemographicsGraph {

    @Autowired
    SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    ExpertiseLevelRepository expertiseLevelRepository;

    /**
     * Get Client Demographics Of an instructor By Expertise Level chart data
     *
     * @param instructor
     * @param dateRange
     * @return
     */
    @Override
    public ResponseModel getClientDemographics(User instructor, DateRange dateRange) {
        long profilingStartTimeMillis = System.currentTimeMillis();

        List<ExpertiseLevels> expertiseLevels = expertiseLevelRepository.findAll();

        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));


        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        profilingStartTimeMillis = System.currentTimeMillis();
        for (ExpertiseLevels expertiseLevel : expertiseLevels) {
            if(!expertiseLevel.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)){
                ChartEntry chartEntry = new ChartEntry();
                int count = subscriptionAuditRepo.countBySubscriptionTypeNameIgnoreCaseAndProgramSubscriptionProgramOwnerUserIdAndProgramSubscriptionProgramProgramExpertiseLevelAndSubscriptionDateBetween(KeyConstants.KEY_PROGRAM, instructor.getUserId(), expertiseLevel, dateRange.getStartDate(), dateRange.getEndDate());
                chartEntry.setEntryName(expertiseLevel.getExpertiseLevel());
                chartEntry.setCount(count);

                chartEntryList.add(chartEntry);
                totalCount += count;
            }
        }

        if (totalCount == 0) {
            //TODO : Remove dummy data and throw exception once Front End development is completed
            //throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
            List<String> chartEntryNames = expertiseLevels.stream().filter(expertiseLevels1 -> !expertiseLevels1.getExpertiseLevel().equalsIgnoreCase(KeyConstants.KEY_ALL_LEVELS)).map(expertiseLevel -> expertiseLevel.getExpertiseLevel()).collect(Collectors.toList());
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

        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        return response;
    }
}

package com.fitwise.utils;

import com.fitwise.constants.Constants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.graph.ChartData;
import com.fitwise.view.graph.ChartDataWithValue;
import com.fitwise.view.graph.ChartEntry;
import com.fitwise.view.graph.ChartEntryWithValue;
import com.fitwise.view.graph.GraphDataWithValue;
import com.fitwise.view.graph.GraphEntryWithValue;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Vignesh G on 20/03/20
 */
public class ChartDataUtils {

    public void populationEntryPercentage(List<ChartEntry> chartEntryList, int totalCount) {
        for (ChartEntry chartEntryItem : chartEntryList) {
            float percentage = ((((float) chartEntryItem.getCount()) * 100) / (float) totalCount);
            percentage = (float) (Math.round(percentage * 100.0) / 100.0);
            chartEntryItem.setPercentage(percentage);
        }
    }

    public void populationEntryPercentages(List<ChartEntryWithValue<Double>> chartEntryList, double totalValue) {
        for (ChartEntryWithValue<Double> chartEntryItem : chartEntryList) {
            float percentage = ((( chartEntryItem.getValue().floatValue()) * 100) / (float) totalValue);
            percentage = (float) (Math.round(percentage * 100.0) / 100.0);
            chartEntryItem.setPercentage(percentage);
        }
    }

    public ResponseModel getChartDummyData(List<String> entryListNames) {

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        ChartData chartData = new ChartData();

        List<ChartEntry> chartEntryList = new ArrayList<>();
        int totalCount = 0;

        for (int i = 0; i < entryListNames.size(); i++) {
            ChartEntry chartEntry = new ChartEntry();
            int count = 0;
            chartEntry.setEntryName(entryListNames.get(i));
            chartEntry.setCount(count);
            String percentStr = decimalFormat.format(0.0);
            float percent = Float.parseFloat(percentStr);
            chartEntry.setPercentage(percent);

            chartEntryList.add(chartEntry);
            totalCount += count;
        }

        chartData.setTotalCount(totalCount);
        chartData.setChartEntryList(chartEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);

        return response;

    }

    public ResponseModel getChartWithValueDummyData(List<String> entryListNames) {
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        List<ChartEntryWithValue<Integer>> chartEntryList = new ArrayList<>();
        int totalValue = 0;

        for (int i = 0; i < entryListNames.size(); i++) {
            ChartEntryWithValue<Integer> chartEntry = new ChartEntryWithValue<Integer>();
            int value = 0;
            chartEntry.setEntryName(entryListNames.get(i));
            chartEntry.setValue(value);
            String percentStr = decimalFormat.format(0.0);
            float percent = Float.parseFloat(percentStr);
            chartEntry.setPercentage(percent);

            chartEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(value));

            chartEntryList.add(chartEntry);
            totalValue += value;
        }

        ChartDataWithValue<Integer> chartData = new ChartDataWithValue<Integer>();

        chartData.setTotalValue(totalValue);

        chartData.setFormattedTotalValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(totalValue));

        chartData.setChartEntryList(chartEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(chartData);

        return response;

    }

    public ResponseModel getGraphWithValueDummyDataForYear() {

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        List<GraphEntryWithValue<Integer>> graphEntryList = new ArrayList<>();
        int overAllValue = 0;

        int noOfMonths = 12;

        for (int i = 0; i < noOfMonths; i++) {

            int value = 0;

            GraphEntryWithValue<Integer> graphEntry = new GraphEntryWithValue<Integer>();
            graphEntry.setPeriodId(i + 1);
            String monthName = ValidationUtils.months[i];
            graphEntry.setPeriodName(monthName);

            graphEntry.setValue(value);

            graphEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(value));

            graphEntryList.add(graphEntry);
            overAllValue += value;
        }

        GraphDataWithValue<Integer> graphData = new GraphDataWithValue<Integer>();
        graphData.setOverAllValue(overAllValue);

        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(overAllValue));

        graphData.setGraphEntryList(graphEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);

        return response;

    }

    public ResponseModel getGraphWithValueDummyDataForMonth() {

        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        String[] weekSplit = ValidationUtils.getWeekSplit(31);

        List<GraphEntryWithValue<Integer>> graphEntryList = new ArrayList<>();
        int overAllValue = 0;

        for (int i = 0; i < weekSplit.length; i++) {

            int value = 0;

            GraphEntryWithValue<Integer> graphEntry = new GraphEntryWithValue<Integer>();
            graphEntry.setPeriodId(i + 1);
            graphEntry.setPeriodName("W" + (i + 1));

            graphEntry.setValue(value);

            graphEntry.setFormattedValue(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(value));

            graphEntryList.add(graphEntry);
            overAllValue += value;
        }

        GraphDataWithValue<Integer> graphData = new GraphDataWithValue<Integer>();
        graphData.setOverAllValue(overAllValue);

        graphData.setOverAllValueFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(overAllValue));

        graphData.setGraphEntryList(graphEntryList);

        ResponseModel response = new ResponseModel();
        response.setStatus(Constants.SUCCESS_STATUS);
        response.setMessage(MessageConstants.MSG_DATA_RETRIEVED);
        response.setPayload(graphData);

        return response;
    }

}

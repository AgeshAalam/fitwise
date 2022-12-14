package com.fitwise.model;

import lombok.Data;

import java.util.List;

@Data
public class DeleteFitwiseSchedulesRequestModel {

    private List<Long> fitwiseScheduleIds;
}

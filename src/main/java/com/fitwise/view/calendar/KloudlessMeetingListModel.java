package com.fitwise.view.calendar;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KloudlessMeetingListModel {

    private List<KloudlessMeetingModel> meetings;

    private long totalCount;

}

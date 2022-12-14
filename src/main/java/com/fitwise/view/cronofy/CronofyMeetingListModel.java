package com.fitwise.view.cronofy;

import java.util.List;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronofyMeetingListModel {
	
	private List<CronofyMeetingModel> meetings;

    private long totalCount;
}

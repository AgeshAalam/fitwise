package com.fitwise.response.kloudless;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Availability {

    @SerializedName("end_repeat")
    private String endRepeat;

    @SerializedName("available_times")
    private List<AvailableTime> availableTimes;

}

package com.fitwise.response.kloudless;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Recurring {

    @SerializedName("weekday")
    private String weekday;

    @SerializedName("month")
    private String month;

    @SerializedName("day")
    private String day;

}

package com.fitwise.response.kloudless;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailableTime {

    @SerializedName("start")
    private String start;

    @SerializedName("end")
    private String end;

    @SerializedName("recurring")
    private Recurring recurring;

}

package com.fitwise.view.cronofy;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorSchedulePayload {

      @JsonProperty("objects")
      private List<CronofyschedulePayload> cronofyschedulePayload;

      @JsonProperty("count")
      private Integer count;

      @JsonProperty("page")
      private String page;

      @JsonProperty("next_page")
      private String next_page;

      @JsonProperty("type")
      private String type;

      @JsonProperty("api")
      private String api;
      
      @JsonProperty("unavailable_days")
      private List<String> unavailable_days;
}

package com.fitwise.response.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomAttribute {

    @JsonProperty("key")
    public String key;

    @JsonProperty("name")
    public String name;

    @JsonProperty("value")
    public String value;

}

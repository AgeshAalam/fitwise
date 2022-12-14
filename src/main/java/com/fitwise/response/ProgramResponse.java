package com.fitwise.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class ProgramResponse {

    private long programId;
    private String programName;
    private String instructorName;
    private long subscriptions;
    private BigDecimal rating;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss.000 ", timezone="UTC")
    private Date createdDate;
    private String createdDateFormatted;

    private Date modifiedDate;
    private String modifiedDateFormatted;

    private boolean isBlocked;
    
    private boolean freeToAccess = false;

}

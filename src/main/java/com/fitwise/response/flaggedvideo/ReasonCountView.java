package com.fitwise.response.flaggedvideo;

import lombok.Data;

/*
 * Created by Vignesh G on 04/07/20
 */
@Data
public class ReasonCountView {

    private Long reasonId;

    private String reason;

    private Long count;

}

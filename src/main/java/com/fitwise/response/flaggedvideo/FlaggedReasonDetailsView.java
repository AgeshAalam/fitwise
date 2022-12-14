package com.fitwise.response.flaggedvideo;

import com.fitwise.response.UserNameProfileImgView;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 04/07/20
 */
@Data
public class FlaggedReasonDetailsView {

    private String reason;

    private long totalCount;

    List<UserNameProfileImgView> users;

}

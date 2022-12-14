package com.fitwise.view;


import com.fitwise.entity.VideoManagement;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
public class PromotionResponseView {

    /** The promotion id. */
    private Long promotionId;

    /** The video management. */
    private VideoManagement videoManagement;

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The active. */
    private boolean active;

    /** video standards. */
    private List<VideoStandards> videoStandards;
}

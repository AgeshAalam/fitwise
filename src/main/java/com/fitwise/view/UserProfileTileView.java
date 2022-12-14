package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Class UserProfileTileView.
 */
@Getter
@Setter
public class UserProfileTileView {

    /** The user id. */
    private Long userId;
    
    /** The first name. */
    private String firstName;
    
    /** The last name. */
    private String lastName;
    
    /** The profile image url. */
    private String profileImageUrl;
    
    /** The program types. */
    private List<ExperienceMemberView> instructorExperience;

    private long programCount;

    private long certificateCount;

    private BigDecimal averageRating;
    
    private long packageCount;

}

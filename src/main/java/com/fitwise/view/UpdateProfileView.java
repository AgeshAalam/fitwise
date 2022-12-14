package com.fitwise.view;

import com.fitwise.view.instructor.OtherExpertiseView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateProfileView {
    private String firstName;

    private String lastName;

    private long genderId;

    private String contactNumber;

    private String about;

    private Long userId;

    private List<InstructorExperienceView> expertise;

    private List<OtherExpertiseView> otherExpertise;

    private Long profileImageId;

    private TaxIdView taxIdView;



}

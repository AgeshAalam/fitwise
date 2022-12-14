package com.fitwise.model.member;

import com.fitwise.view.member.Filter;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 28/04/20
 */
@Data
public class MemberInstructorFilterModel {

    private List<Filter> programType;

    //TODO : Experience Filter not included until MVP
    //private List<Filter> yearsOfExperience;

}

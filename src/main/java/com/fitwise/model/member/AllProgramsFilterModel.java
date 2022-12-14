package com.fitwise.model.member;

import com.fitwise.view.member.Filter;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 24/06/20
 */
@Data
public class AllProgramsFilterModel {

    private List<Filter> expertiseLevel;

    private List<Filter> programType;

    private List<Filter> duration;

}

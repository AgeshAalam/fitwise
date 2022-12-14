package com.fitwise.model.member;

import com.fitwise.view.member.Filter;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 23/04/20
 */
@Data
public class MemberProgramsFilterModel {

    private List<Filter> expertiseLevel;

    private List<Filter> duration;

}

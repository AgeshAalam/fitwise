package com.fitwise.view.member;

import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 30/04/20
 */
@Data
public class MemberFilterView {

    private String type;

    private String filterName;

    private List<Filter> filters;

}

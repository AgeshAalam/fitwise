package com.fitwise.model.member;


import com.fitwise.view.member.Filter;
import lombok.Data;

import java.util.List;

@Data
public class PackageFilterModel {

    private List<Filter> session;

    private List<Filter> program;
}

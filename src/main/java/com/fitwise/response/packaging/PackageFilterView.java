package com.fitwise.response.packaging;

import com.fitwise.view.member.Filter;
import lombok.Data;

import java.util.List;

@Data
public class PackageFilterView {

    private String filterName;

    private List<Filter> filters;
}

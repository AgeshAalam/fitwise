package com.fitwise.model.member;

import com.fitwise.view.member.Filter;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 25/05/20
 */
@Data
public class RecommendedAndTrendingFilterModel {

    private List<Filter> duration;

}

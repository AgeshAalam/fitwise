package com.fitwise.program.graph;

import com.fitwise.utils.DateRange;
import com.fitwise.view.ResponseModel;

import java.util.Date;

/*
 * Created by Vignesh G on 17/03/20
 */
public interface ProgramClientDemographicsGraph {

    public ResponseModel getProgramClientDemographics(long programId, DateRange dateRange);

}

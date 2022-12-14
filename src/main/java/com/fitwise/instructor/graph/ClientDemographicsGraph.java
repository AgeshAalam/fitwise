package com.fitwise.instructor.graph;

import com.fitwise.entity.User;
import com.fitwise.utils.DateRange;
import com.fitwise.view.ResponseModel;

/*
 * Created by Vignesh G on 12/03/20
 */
public interface ClientDemographicsGraph {

    public ResponseModel getClientDemographics(User instructor, DateRange dateRange);

}

package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HighestSubscribedPrograms {

    long countOfSubscription;
    long programId;

    public HighestSubscribedPrograms(long countOfSubscription, long programId){

        this.countOfSubscription = countOfSubscription;
        this.programId = programId;
    }
}

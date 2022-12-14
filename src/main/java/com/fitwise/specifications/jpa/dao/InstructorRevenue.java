package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*
 * Created by Vignesh.G on 25/06/21
 */
@Getter
@Setter
@AllArgsConstructor
public class InstructorRevenue {

    private Long userId;

    private Double revenue;

}

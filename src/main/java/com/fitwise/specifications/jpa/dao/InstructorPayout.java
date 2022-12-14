package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InstructorPayout {

    private Double totalRevenue;

    private Double trainnrRevenue;

    private Double tax;

    private Double instructorShare;
}

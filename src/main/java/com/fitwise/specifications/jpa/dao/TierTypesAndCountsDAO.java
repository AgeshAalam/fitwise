package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TierTypesAndCountsDAO {
    private String tierType;
    private Long tierCount;
}

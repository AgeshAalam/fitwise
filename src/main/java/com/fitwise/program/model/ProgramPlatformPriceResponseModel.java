package com.fitwise.program.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramPlatformPriceResponseModel extends PlatformWiseTaxDetailsModel{

    private Long programPriceByPlatformId;

    private Double price;
    
    private Double flatTax;
}

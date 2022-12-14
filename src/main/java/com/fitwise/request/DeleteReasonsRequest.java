package com.fitwise.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DeleteReasonsRequest {

    List<Long> deleteReasons;

}

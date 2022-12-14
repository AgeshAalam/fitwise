package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AdminListResponse<T> {

    long totalSizeOfList ;
    List<T> payloadOfAdmin;
}

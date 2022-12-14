package com.fitwise.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProgramViewedUsersResponse {

    private Long userId;
    private String userName;
    private String imageUrl;
}

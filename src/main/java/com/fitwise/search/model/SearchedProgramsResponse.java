package com.fitwise.search.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchedProgramsResponse {
    long programId;
    String programTitle;
    String imageUrl;
    String expertiseLevel;
    long duration;
}

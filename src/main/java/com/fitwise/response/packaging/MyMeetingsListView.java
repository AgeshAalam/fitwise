package com.fitwise.response.packaging;

import lombok.Data;

import java.util.List;

@Data
public class MyMeetingsListView {

    private  int totalCount;

    private List<MyMeetingsView> meetings;
}

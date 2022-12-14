package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InstructorClientResponseView {
    private List<InstructorClientView> clients;

    private long totalClients;
}

package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public
class ZenDeskWebHookResponseView {
    private int id;
    private String title;
    private String url;
    private String ticketStatus;
    private String assigneeName;
    private String assigneeEmail;
    private String requesterEmail;
    private String userPhoneNumber;
}

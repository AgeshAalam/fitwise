package com.fitwise.request.zenDesk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZenDeskTicketRequest {
    private String subject;
    private Object comment;
    private ZenDeskUserData requester;
}

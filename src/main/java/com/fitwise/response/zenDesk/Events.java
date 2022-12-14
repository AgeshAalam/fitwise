package com.fitwise.response.zenDesk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Events {
    private String audit_id;

    private String[] attachments;

    private String isPublic;

    private String html_body;

    private String plain_body;

    private String id;

    private String type;

    private String author_id;

    private String body;
}

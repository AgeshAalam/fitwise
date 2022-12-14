package com.fitwise.response.zenDesk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Audit {
    private Metadata metadata;

    private String created_at;

    private String id;

    private String ticket_id;

    private String author_id;

    private Events[] events;

    private Via via;

}

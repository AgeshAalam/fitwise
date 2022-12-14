package com.fitwise.response.zenDesk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ticket {
    private String subject;

    private String[] email_cc_ids;

    private String created_at;

    private String description;

    private int external_id;

    private String type;

    private Via via;

    private String allow_attachments;

    private String updated_at;

    private int problem_id;

    private String[] follower_ids;

    private String due_at;

    private String id;

    private String assignee_id;

    private String raw_subject;

    private int forum_topic_id;

    private String[] custom_fields;

    private String allow_channelback;

    private int satisfaction_rating;

    private String submitter_id;

    private int priority;

    private String[] collaborator_ids;

    private String url;

    private String[] tags;

    private String brand_id;

    private String[] sharing_agreement_ids;

    private String group_id;

    private int organization_id;

    private String[] followup_ids;

    private String recipient;

    private String is_public;

    private String has_incidents;

    private String[] fields;

    private String status;

    private String requester_id;
}

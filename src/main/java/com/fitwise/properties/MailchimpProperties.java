package com.fitwise.properties;

import org.springframework.stereotype.Component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Component
@Getter
public class MailchimpProperties {

	/** apikey. */
    @Value("${mailchimp.api.accessKey}")
    private String accessKey;
    
    /** Instructor list id. */
    @Value("${mailchimp.notify.list.id.instructor}")
    private String instructorListId;
    
    /** Member list id. */
    @Value("${mailchimp.notify.list.id.member}")
    private String memberListId;
}

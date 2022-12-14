package com.fitwise.view.admin;

import com.fitwise.request.InviteMember;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipleInvitePayloadview {
    /** The status. */
    private long status;

    /** The message. */
    private String message = "";

    private InviteMember inviteMember;
}

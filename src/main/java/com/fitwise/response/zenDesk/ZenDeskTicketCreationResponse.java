package com.fitwise.response.zenDesk;

import lombok.Getter;
import lombok.Setter;
import com.fitwise.response.zenDesk.Ticket;

@Getter
@Setter
public class ZenDeskTicketCreationResponse {
    private Ticket ticket;
    private Audit audit;
}

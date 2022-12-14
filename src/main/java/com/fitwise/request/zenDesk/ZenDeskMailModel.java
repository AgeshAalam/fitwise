package com.fitwise.request.zenDesk;

import lombok.Data;

/*
 * Created by Vignesh G on 16/07/20
 */
@Data
public class ZenDeskMailModel {
    private String email;
    private String subject;
    private String body;
}

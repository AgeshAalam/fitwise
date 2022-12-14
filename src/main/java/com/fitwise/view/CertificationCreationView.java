package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * The Class CertificationCreationView.
 */
@Getter
@Setter
public class CertificationCreationView {

    /** The user id. */
    private Long userId;

    /** The certificate title. */
    private String certificateTitle;

    /** The academy name. */
    private String academyName;


    private Long certificateImageId;

    /** The issued date. */
    private String issuedDate;



}

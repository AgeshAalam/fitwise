package com.fitwise.view;


import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class CertificationRequestView.
 */
@Getter
@Setter
public class CertificationRequestView {

    /** The user id. */
    private Long userId;

    /** The instructor certificate id. */
    private Long instructorCertificateId;

    /** The certificate title. */
    private String certificateTitle="";

    /** The academy name. */
    private String academyName="";

    /** The issued date. */
    private String issuedDate;


    private Long certificateImageId;

}

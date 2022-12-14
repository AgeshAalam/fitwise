package com.fitwise.view.dynamiclink;

import lombok.Data;

/*
 * Created by Vignesh G on 25/08/20
 */
@Data
public class DynamicLinkInfo {

    String domainUriPrefix;
    String link;
    AndroidInfo androidInfo;
    IOSInfo iosInfo;
    SocialMetaTagInfo socialMetaTagInfo;

}

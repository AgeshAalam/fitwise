package com.fitwise.util.payments.appleiap.updateprice;

import com.fitwise.util.payments.appleiap.VersionUtils;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/*
 * Created by Vignesh.G on 23/04/21
 */
@Getter
@Setter
@XmlRootElement(name = "package")
@XmlType(propOrder = {"version", "xmlns", "provider", "teamId", "vendorId", "appleId", "versionUtil", "subscriptionGroup"})
public class ElementsMapperUpdatePrice {

    @XmlAttribute
    private String version;
    @XmlAttribute
    private String xmlns;
    @XmlElement(name = "provider")
    private String provider;
    @XmlElement(name = "team_id")
    private String teamId;
    @XmlPath("software/vendor_id/text()")
    private String vendorId;
    @XmlPath("software/read_only_info/read_only_value[@key='apple-id']/text()")
    private String appleId;
    @XmlPath("software/software_metadata[@app_platform='ios']/versions/version")
    private VersionUtils versionUtil;

    @XmlPath("software/software_metadata[@app_platform='ios']/in_app_purchases/subscription_group")
    List<SubscriptionGroupUpdatePrice> subscriptionGroup;
}

package com.fitwise.util.payments.appleiap.updateprice;

import com.fitwise.util.payments.appleiap.IAPUpdatePricing;
import com.fitwise.util.payments.appleiap.IntroductoryOffer;
import com.fitwise.util.payments.appleiap.PromotionalOffer;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/*
 * Created by Vignesh.G on 23/04/21
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "subscription_group")
public class SubscriptionGroupUpdatePrice {
    @XmlAttribute(name = "name")
    private String subscriptioGroupname;

    @XmlPath("locales/locale[@name='en-US']/title/text()")
    private String title;

    @XmlPath("in_app_purchase/product_id/text()")
    private String productId;
    @XmlPath("in_app_purchase/reference_name/text()")
    private String referenceName;
    @XmlPath("in_app_purchase/type/text()")
    private String type;
    @XmlPath("in_app_purchase/duration/text()")
    private String duration;
    @XmlPath("in_app_purchase/bonus_duration/text()")
    private String bonusDuration;
    @XmlPath("in_app_purchase/locales/locale[@name='en-US']/title/text()")
    private String subscriptionDisplayName;
    @XmlPath("in_app_purchase/locales/locale[@name='en-US']/description/text()")
    private String description;


    @XmlPath("in_app_purchase/review_screenshot/file_name/text()")
    private String filename;
    @XmlPath("in_app_purchase/review_screenshot/size/text()")
    private String size;
    @XmlPath("in_app_purchase/review_screenshot/checksum[@type='md5']/text()")
    private String checksum;
    @XmlPath("in_app_purchase/review_notes/text()")
    private String reviewNotes;
    @XmlPath("in_app_purchase/prices[@verification_code='1277698375']/price")
    private List<IAPUpdatePricing> iapPricing;
    /*@XmlPath("in_app_purchase/rank/text()")
    private int rank;*/
    //Introductory Offer : For Later enhancements
	/*
	@XmlPath("in_app_purchase/offers/offer")
	private List<IntroductoryOffer> introductoryOffers;
	*/
    @XmlPath("in_app_purchase/cleared_for_sale/text()")
    private Boolean clearedforsale;
    //Introductory Offer
    @XmlPath("in_app_purchase/offers/offer")
    private List<IntroductoryOffer> introductoryOffers;

    @XmlPath("in_app_purchase/offers/text()")
    private String removeIntroductoryOffers;
    //Promotional Offer
    @XmlPath("in_app_purchase/promotional_offers/promotional_offer")
    private List<PromotionalOffer> promotionalOffers;
}

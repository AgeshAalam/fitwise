package com.fitwise.util.payments.appleiap;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="version")
public class VersionUtils {

	@XmlAttribute(name="string")
	private String appVersion;
	@XmlElement(name="title")
	@XmlPath("locales/locale[@name='en-US']/title/text()")
	private String appTitle;
	@XmlElement(name="description")
	@XmlPath("locales/locale[@name='en-US']/description/text()")
	private String appDescription;
	@XmlElement(name="support_url")
	@XmlPath("locales/locale[@name='en-US']/support_url/text()")
	private String supportURL;
}

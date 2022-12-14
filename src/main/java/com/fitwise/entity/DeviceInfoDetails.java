package com.fitwise.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class DeviceInfoDetails {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long deviceInfoDetailId;
	private String deviceName;
	private String deviceModelName;
	private String devicePlatform;
	private String devicePlatformVersion;
	private String deviceUuid;
	private String appVersion;
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = true)
	private User userId;
	private Date CreatedDate;
}

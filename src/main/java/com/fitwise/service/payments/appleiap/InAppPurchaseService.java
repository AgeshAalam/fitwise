package com.fitwise.service.payments.appleiap;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.InstructorConstant;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.constants.payments.appleiap.InAppPurchaseConstants;
import com.fitwise.constants.payments.appleiap.NotificationConstants;
import com.fitwise.entity.AppConfigKeyValue;
import com.fitwise.entity.AppVersionInfo;
import com.fitwise.entity.Programs;
import com.fitwise.entity.UserProfile;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.payments.appleiap.AppInformation;
import com.fitwise.entity.payments.appleiap.IapJobInputs;
import com.fitwise.entity.payments.appleiap.InitialPricing;
import com.fitwise.entity.payments.appleiap.SubscriptionGroup;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.AppleProperties;
import com.fitwise.properties.AwsProperties;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.AppVersionInfoRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.UserProfileRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.payments.appleiap.AppInformationRepository;
import com.fitwise.repository.payments.appleiap.IAPJobInputsRepository;
import com.fitwise.repository.payments.appleiap.InitialPricingRepository;
import com.fitwise.repository.payments.appleiap.SubscriptionGroupRepository;
import com.fitwise.util.payments.appleiap.IAPNewPricing;
import com.fitwise.util.payments.appleiap.IAPUpdatePricing;
import com.fitwise.util.payments.appleiap.IntroductoryOffer;
import com.fitwise.util.payments.appleiap.PromotionalOffer;
import com.fitwise.util.payments.appleiap.VersionUtils;
import com.fitwise.util.payments.appleiap.newprice.ElementsMapperNewPrice;
import com.fitwise.util.payments.appleiap.newprice.SubscriptionGroupNewPrice;
import com.fitwise.util.payments.appleiap.updateprice.ElementsMapperUpdatePrice;
import com.fitwise.util.payments.appleiap.updateprice.SubscriptionGroupUpdatePrice;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.MailSender;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InAppPurchaseService {
	@Autowired
	AppInformationRepository appInformationRepository;
	
	@Autowired
	ProgramRepository programRepository;
	
	@Autowired
	InitialPricingRepository initialPricingRepository;
	
	@Autowired
	IAPJobInputsRepository iapJobInputsRepository;
	
	@Autowired
	UserProfileRepository userProfileRepository;
	
	@Autowired
	SubscriptionGroupRepository sGroupRepository;
	
	@Autowired
	AppConfigKeyValueRepository appConfigKeyValueRepository;

	@Autowired
	AppVersionInfoRepository appVersionInfoRepository;
	
	@Autowired
	MailSender mailSender;
	
	@Autowired
	AppleProperties appleProperties;
	
	@Autowired
	DiscountOfferMappingRepository discountOfferMappingRepository;
	
	@Autowired
    FitwiseUtils fitwiseUtils;

	private final AwsProperties awsProperties;

	// Un used method
	//@Scheduled(cron=InAppPurchaseConstants.SCHEDULAR)
	public ResponseModel publishPrograms() throws IOException {
		return uploadXML(null,InstructorConstant.PUBLISH);
	}

	public ResponseModel unPublishPrograms(List<Long> programId) throws IOException {
		log.info("List of programId {}", programId);	
		programId = programId.stream().distinct().collect(Collectors.toList());
		return uploadXML(programId,InstructorConstant.UNPUBLISH);
	}

	public ResponseModel uploadXML(List<Long> programid,String programStatus) throws IOException {
		log.info("------------ Method :: buildMetadata (Generate XML) ------------");
		AppInformation appInformation = appInformationRepository.findByVendorId(InAppPurchaseConstants.VENDOR_ID);
		AppConfigKeyValue appConfigKeyValue=appConfigKeyValueRepository.findByKeyString(Constants.FAQ_URL);
			if (appInformation != null) {
				/** App level information from AppStore/iTune */
				ElementsMapperNewPrice eMapperUtils = new ElementsMapperNewPrice();

				eMapperUtils.setXmlns(appInformation.getITunepackage());
				eMapperUtils.setVersion(appInformation.getSoftwareVersion());
				/** Provider Id */
				eMapperUtils.setProvider(appInformation.getProviderId());
				/** Team Id */
				eMapperUtils.setTeamId(appInformation.getTeamId());
				/** Apple ID */
				eMapperUtils.setAppleId(appInformation.getAppleId());
				/** SKU */
				eMapperUtils.setVendorId(appInformation.getVendorId());
				/** Set App Version **/
				VersionUtils vUtils=new VersionUtils();
				AppVersionInfo version=appVersionInfoRepository.findByIsLatestVersionAndAppPlatformAndApplication(true, DBConstants.IOS, KeyConstants.KEY_MEMBER);
				vUtils.setAppVersion(version.getAppVersion());
				/** Display name for App */
				vUtils.setAppTitle(appInformation.getAppTitle());

				/** Support URL */
				vUtils.setSupportURL(appConfigKeyValue.getValueString());
				/** Description*/
				vUtils.setAppDescription(appInformation.getDescription());
				eMapperUtils.setVersionUtil(vUtils);
				/**
				 * List of all Published Programs. Each program has mapped into each Subscription Group
				 */
				List<SubscriptionGroupNewPrice> suGroupUtils;


				List<Programs> programs = new ArrayList<>();
				if(programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
					/**
					 * Fetch published program which are published after of last created Job Input File
					 */
					IapJobInputs iapJobInputs = iapJobInputsRepository.findTop1ByStatusOrderByIdDesc(InstructorConstant.PUBLISH);
					if (iapJobInputs != null) {
						log.info("Row Id{} and Last Date :{}", iapJobInputs.getId(), iapJobInputs.getModifiedDate());
						programs = programRepository.findByStatusAndModifiedDateAfterOrderByProgramIdAsc(InstructorConstant.PUBLISH, iapJobInputs.getModifiedDate());
					} else {
						programs = programRepository.findByStatus(InstructorConstant.PUBLISH);
					}
				 }else if(programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {

					 programs=programRepository.findAllById(programid);
				 }
				log.info("List of Published Programs from DB :{}"+programs.size());
				//
				if(!programs.isEmpty()) {
					ArrayList<ArrayList<Programs>> mainArrayList = new ArrayList<>();
					ArrayList<Programs> subArrayList = new ArrayList<>();

					// App store allows only 40 programs to upload at a time. We split List into
					// sublist which has 40 programs
					if(programs.size()<=40) {
						for (int i = 0; i < programs.size(); i++) {
							subArrayList.add(programs.get(i));
						}
						mainArrayList.add(subArrayList);
					}else {
						for (int i = 0; i < programs.size(); i++) {
							subArrayList.add(programs.get(i));
							if (subArrayList.size() == 40) {
								 mainArrayList.add(subArrayList);
								subArrayList = new ArrayList<>();
							}
						}
					}

					log.info("Size of sublist in Main Arraylist :{}" + mainArrayList.size());
					for(ArrayList<Programs> subListPrograms:mainArrayList) {
						suGroupUtils = new ArrayList<>();
						if (!subListPrograms.isEmpty()) {
							//Email Notification Variables
							String productId;
							AppConfigKeyValue idConfigKeyValue=appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
							productId=idConfigKeyValue.getValueString();
							Map<String, String> notificationMap=new LinkedHashMap<>();
							//
							log.info(" Count of PUBLISHED programs :{}", subListPrograms.size());
							List<Programs> programEntity = new ArrayList<>();
							List<File> inputFilesToPack = new ArrayList<>();
							final String SIMPLEDATE = "yyyyMMddHHmmss";
							Format f = new SimpleDateFormat(SIMPLEDATE);
							String dateVal = f.format(new Date());
							final String tempDirectory = Files.createTempDirectory(InAppPurchaseConstants.DIR_PREFIX) + File.separator;
							String zipFileName ="";
							if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
								zipFileName = InAppPurchaseConstants.JOB_PUBLISH.concat(dateVal).concat(InAppPurchaseConstants.ZIP_EXTN);
							} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
								zipFileName = InAppPurchaseConstants.JOB_UNPUBLISH.concat(dateVal).concat(InAppPurchaseConstants.ZIP_EXTN);
							}

							final String zipFilePath = tempDirectory + zipFileName;
							log.info(" zipFilePath path : {}", zipFilePath);
							int count = 0;
							for (Programs program : subListPrograms) {
							//Email Notification
							notificationMap.put(
									productId.concat(".").concat(
											program.getProgramId().toString().concat(" - ").concat(program.getTitle())),
									program.getProgramPrice().toString().concat(" USD"));

								count++;
								SubscriptionGroupNewPrice suGroupUtil = new SubscriptionGroupNewPrice();

								/** Subscription Group Name */
								log.info(" User id of {} is {} :", program.getTitle(),program.getOwner().getUserId());

								/** Forming SubscriptionGroup Entity */
								SubscriptionGroup sGroup = new SubscriptionGroup();
								if (program.getSubscriptionGroup() != null && program.getSubscriptionGroup().getSubscriptionGroupName()!=null && program.getSubscriptionGroup().getSubscriptionGroupName().length() > 0) {
									/**
									 * If upload failed subscriptionGroupName is already there in table. We can use
									 * that else go for iteration to find
									 */
									suGroupUtil.setSubscriptioGroupname(program.getSubscriptionGroup().getSubscriptionGroupName());
								} else {
									String subsGroupName = findSubcriptionGrpName(program.getOwner().getUserId(),program);
									suGroupUtil.setSubscriptioGroupname(subsGroupName);
									sGroup.setSubscriptionGroupName(subsGroupName);
									sGroup.setSubscriptionDisplayName(program.getTitle());
									programEntity.add(program);
								}
								program.setSubscriptionGroup(sGroup);

								/** Localization Title ->Program Title */
								suGroupUtil.setTitle(program.getTitle());
								/** Product Id -> Program Id */
								AppConfigKeyValue configKeyValue = appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
								suGroupUtil.setProductId(configKeyValue.getValueString().concat(".").concat(String.valueOf(program.getProgramId())));
								/** Reference Name -> Program Title */
								suGroupUtil.setReferenceName(program.getTitle());
								/** Program Type */
								suGroupUtil.setType(InAppPurchaseConstants.PROGRAM_TYPE);
								/** Program Duration */
								suGroupUtil.setDuration(InAppPurchaseConstants.ONE_MONTH);
								/** Subscription Display Name */
								suGroupUtil.setSubscriptionDisplayName(program.getTitle());
								/** Program Description */
								suGroupUtil.setDescription(program.getShortDescription());
								/** Cleared For Sale */
								if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
									suGroupUtil.setClearedforsale(true);
								} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
									suGroupUtil.setClearedforsale(false);
								}

								/** Review Screenshot */
								if (program.getImage() != null) {
									String imagepath = program.getImage().getImagePath();
									String extension;

									//String filename =program.getImage().getFileName();
									//String filename ="";
									//if(filename==null || filename.isEmpty()) {
										if(imagepath.contains("vimeo")) {
											//https://i.vimeocdn.com/video/856397504_640x360.jpg?r=pad
											if(imagepath.contains("?")) {
												//filename = imagepath.substring(imagepath.lastIndexOf('/') + 1, imagepath.lastIndexOf('?'));
												extension=imagepath.substring(imagepath.lastIndexOf('.')+ 1, imagepath.lastIndexOf('?'));
											}else {
												//filename = imagepath.substring(imagepath.lastIndexOf('/') + 1, imagepath.length());
												extension=imagepath.substring(imagepath.lastIndexOf('.')+ 1, imagepath.length());
											}
										}else {
										    // filename = imagepath.substring(imagepath.lastIndexOf('/') + 1, imagepath.length());
											extension=imagepath.substring(imagepath.lastIndexOf('.')+ 1, imagepath.length());
										}
									//}
									String uniqueFileName="Img" + program.getProgramId() + "" + count + "."+extension;
									String destImgPath = imagedownload(imagepath, tempDirectory, extension,uniqueFileName);
									String checksumMD5 = DigestUtils.md5DigestAsHex(Files.newInputStream(Paths.get(destImgPath)));
									File file = new File(destImgPath);
									String bytes = String.valueOf(file.length());
									log.info("Screenshot Name :{} ", uniqueFileName);
									log.info("Screenshot MD5 :{} ", checksumMD5);
									log.info("Screenshot Size :{} ", bytes);
									/** File Name */
									suGroupUtil.setFilename(uniqueFileName);
									/** File size */
									suGroupUtil.setSize(bytes);
									/** File checksum */
									suGroupUtil.setChecksum(checksumMD5);
									inputFilesToPack.add(new File(destImgPath).getAbsoluteFile());
								} else {
									throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.IMG_ERR+" :"+program.getTitle(), null);
								}

								/**
								 * Before submitting a new in-app purchase for review, we must include a <price>
								 * block for each of the 175 territories and the <price> block should include
								 * only the <territory> and <price_tier> tags. Program price for US territory
								 * will be fetched from 'Program Prices' table
								 */
								List<InitialPricing> priceList = initialPricingRepository.findAll();
								List<IAPNewPricing> pricingTier = new ArrayList<>();
								for (InitialPricing price : priceList) {
									IAPNewPricing iPricingUtils = new IAPNewPricing();

									if (price.getTerritory().equalsIgnoreCase(InAppPurchaseConstants.US) && program.getProgramPrice() != null) {
										iPricingUtils.setTerritory(price.getTerritory());
										iPricingUtils.setTier(program.getProgramPrices().getTier());
									} else {
										iPricingUtils.setTerritory(price.getTerritory());
										iPricingUtils.setTier(Integer.parseInt(price.getTier()));
									}
									pricingTier.add(iPricingUtils);
								}
								suGroupUtil.setIapPricing(pricingTier);
								suGroupUtils.add(suGroupUtil);
								//
								/**Set Promotional Offers */
								// TODO: Set PromotionalOffer block here
								/*List<PromotionalOffer> promotionalOffers=new ArrayList<>();
								PromotionalOffer proOffer1=constructPromotionalOfferBlocks("20201609_01Megaoffer","20201609_01Megaoffer","free","1 Month","1",2);
								PromotionalOffer proOffer2=constructPromotionalOfferBlocks("20201609_02Megaoffer","20201609_02Megaoffer","pay-as-you-go","1 Month","1",2);
								promotionalOffers.add(proOffer1);	
								promotionalOffers.add(proOffer2);
								
								suGroupUtil.setPromotionalOffers(promotionalOffers);*/
							}
							//
							/** Added programs */
							eMapperUtils.setSubscriptionGroup(suGroupUtils);

							/** XML Generation */
							generateXMLForNewPrice(eMapperUtils, tempDirectory);
							log.info("XML file has been generated Sucessfully....");
							inputFilesToPack.add(new File(tempDirectory + InAppPurchaseConstants.METADATA_XML).getAbsoluteFile());
							log.info("---------- metadata XML placed in tempDirectory -----------");

							/** Save Subscription Group Information */
							log.info("programEntity: {} ", programEntity.size());
							if (!programEntity.isEmpty()) {
								for (Programs program : programEntity) {
									log.info("SubscriptionGroupName {} ::",program.getSubscriptionGroup().getSubscriptionGroupName());
									programRepository.save(program);
								}
							}

							/** Remove duplicate Screenshots */
							List<File> newList = inputFilesToPack.stream().distinct().collect(Collectors.toList());

							/** Create Zip */
							File[] arrayOfFiles = new File[] {};
							arrayOfFiles = newList.toArray(arrayOfFiles);
							if (arrayOfFiles == null || arrayOfFiles.length == 0) {
								throw new ApplicationException(Constants.ERROR_STATUS,InAppPurchaseConstants.FLE_ERR,null);
							}
							try {
								ZipUtil.packEntries(arrayOfFiles, new File(zipFilePath));
							} catch (ZipException exception) {
								throw new ApplicationException(Constants.ERROR_STATUS,InAppPurchaseConstants.ZIP_ERR,exception.getMessage());
							}
							log.info(" ------------- Zip file generated successfully ---------------------"+zipFilePath);

							/** Move files from TempDirectory to Respective folder*/
							String filepath ="";
							if(programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
								 filepath = "/usr/local/itms/job/publish/";
							}else if(programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)){
								filepath = "/usr/local/itms/job/unPublish/";
							}

							//copyFile(zipFilePath, filepath);
							copyFile(tempDirectory, filepath);

							/** Remove files from TempDirectory */
							// TODO :uncomment this line after testing
							// XmlMetadataUtils.removeFileOrDirectory(tempDirectory);

							/** Save Jobfile Information */
							IapJobInputs aJobInputs = new IapJobInputs();
							aJobInputs.setJobfileName(zipFileName);
							if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
								aJobInputs.setStatus(InstructorConstant.PUBLISH);
							} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
								aJobInputs.setStatus(InstructorConstant.UNPUBLISH);
							}
							aJobInputs.setTempDir(tempDirectory);
							iapJobInputsRepository.save(aJobInputs);

							/** Run script file */
							//executeCommand(programStatus);

							/** Email Notification*/
							if(!notificationMap.isEmpty()) {
								//sendNotification(notificationMap,programStatus);
							}

						}else {
							return new ResponseModel(Constants.SUCCESS_STATUS,InAppPurchaseConstants.PGM_ERR, null);
						}
					}
				}else {
					return new ResponseModel(Constants.SUCCESS_STATUS,InAppPurchaseConstants.PGM_ERR, null);
				}

			} else {
				throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.VEN_ERR, null);
			}
		return new ResponseModel(Constants.SUCCESS_STATUS,InAppPurchaseConstants.OK_MSG, null);
	}

	public ResponseModel uploadXMLForNewPriceProgram(String programStatus, Programs program) throws IOException {
		String uploadFilePath = "";
		String itmsUploadStatus = DBConstants.ITMS_UPDATED;
		if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
			uploadFilePath = "/usr/local/itms/uploadlog";
		} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
			uploadFilePath = "/usr/local/itms/unpublog";
		}
		File logFile = new File(uploadFilePath);
		if (logFile.exists()) {
			logFile.delete();
		}
		log.info("------------ Method :: buildMetadata (Generate XML) ------------");
		AppInformation appInformation = appInformationRepository.findByVendorId(InAppPurchaseConstants.VENDOR_ID);
		AppConfigKeyValue appConfigKeyValue = appConfigKeyValueRepository.findByKeyString(Constants.FAQ_URL);
		if (appInformation != null) {
			/** App level information from AppStore/iTune */
			ElementsMapperNewPrice eMapperUtils = new ElementsMapperNewPrice();
			eMapperUtils.setXmlns(appInformation.getITunepackage());
			eMapperUtils.setVersion(appInformation.getSoftwareVersion());
			/** Provider Id */
			eMapperUtils.setProvider(appInformation.getProviderId());
			/** Team Id */
			eMapperUtils.setTeamId(appInformation.getTeamId());
			/** Apple ID */
			eMapperUtils.setAppleId(appInformation.getAppleId());
			/** SKU */
			eMapperUtils.setVendorId(appInformation.getVendorId());
			VersionUtils vUtils = new VersionUtils();
			AppVersionInfo version = appVersionInfoRepository.findByIsLatestVersionAndAppPlatformAndApplication(true, DBConstants.IOS, KeyConstants.KEY_MEMBER);
			vUtils.setAppVersion(version.getAppVersion());
			/** Display name for App */
			vUtils.setAppTitle(appInformation.getAppTitle());
			/** Support URL */
			vUtils.setSupportURL(appConfigKeyValue.getValueString());
			/** Description*/
			vUtils.setAppDescription(appInformation.getDescription());
			eMapperUtils.setVersionUtil(vUtils);
			List<SubscriptionGroupNewPrice> suGroupUtils = new ArrayList<>();
			/**
			 * List of all Published Programs. Each program has mapped into each Subscription Group
			 */
			//Email Notification Variables
			//AppConfigKeyValue idConfigKeyValue=appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
			//String productId=idConfigKeyValue.getValueString();
			List<Programs> programEntity = new ArrayList<>();
			List<File> inputFilesToPack = new ArrayList<>();
			final String SIMPLEDATE = "yyyyMMddHHmmss";
			Format f = new SimpleDateFormat(SIMPLEDATE);
			String dateVal = f.format(new Date());
			final String tempDirectory = Files.createTempDirectory(InAppPurchaseConstants.DIR_PREFIX) + File.separator;
			String zipFileName = "";
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				zipFileName = InAppPurchaseConstants.JOB_PUBLISH.concat(dateVal).concat(InAppPurchaseConstants.ZIP_EXTN);
			} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
				zipFileName = InAppPurchaseConstants.JOB_UNPUBLISH.concat(dateVal).concat(InAppPurchaseConstants.ZIP_EXTN);
			}
			final String zipFilePath = tempDirectory + zipFileName;
			//System.out.println("zipFilePath >>>"+zipFilePath);
			int count = 0;
			count++;
			SubscriptionGroupNewPrice suGroupUtil = new SubscriptionGroupNewPrice();
			/** Subscription Group Name */
			/** Forming SubscriptionGroup Entity */
			SubscriptionGroup sGroup = new SubscriptionGroup();
			String subsGroupName;
			if (program.getSubscriptionGroup() != null && program.getSubscriptionGroup().getSubscriptionGroupName() != null && program.getSubscriptionGroup().getSubscriptionGroupName().length() > 0) {
				/**
				 * If upload failed subscriptionGroupName is already there in table. We can use
				 * that else go for iteration to find
				 */
				subsGroupName = program.getSubscriptionGroup().getSubscriptionGroupName();
				suGroupUtil.setSubscriptioGroupname(subsGroupName);
			} else {
				Long userid = null;
				if (program.getOwner() != null) {
					userid = program.getOwner().getUserId();
				}
				subsGroupName = findSubcriptionGrpName(userid, program);
				suGroupUtil.setSubscriptioGroupname(subsGroupName);
				sGroup.setSubscriptionGroupName(subsGroupName);
				sGroup.setSubscriptionDisplayName(program.getTitle());
				programEntity.add(program);
			}
			program.setSubscriptionGroup(sGroup);
			/** Localization Title ->Program Title */
			suGroupUtil.setTitle(program.getTitle());
			/** Product Id -> Program Id */
			AppConfigKeyValue configKeyValue = appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
			suGroupUtil.setProductId(configKeyValue.getValueString().concat(".").concat(String.valueOf(program.getProgramId())));
			/** Reference Name(Unique) -> Program Title */
			List<String> displayName = sGroupRepository.getAllSubscriptionDisplayName();
			if (!displayName.isEmpty() && displayName.contains(program.getTitle())) {
				suGroupUtil.setReferenceName(subsGroupName);
			} else {
				suGroupUtil.setReferenceName(program.getTitle());
			}

			/** Program Type */
			suGroupUtil.setType(InAppPurchaseConstants.PROGRAM_TYPE);
			/** Program Duration */
			suGroupUtil.setDuration(InAppPurchaseConstants.ONE_MONTH);
			/** Subscription Display Name */
			suGroupUtil.setSubscriptionDisplayName(program.getTitle());
			/** Program Description */
			suGroupUtil.setDescription(program.getShortDescription());

			/** Review Screenshot */
			if (program.getImage() != null) {
				String imagepath = program.getImage().getImagePath();
				String extension;
				if (imagepath.contains("vimeo")) {
					//https://i.vimeocdn.com/video/856397504_640x360.jpg?r=pad
					if (imagepath.contains("?")) {
						extension = imagepath.substring(imagepath.lastIndexOf('.') + 1, imagepath.lastIndexOf('?'));
					} else {
						extension = imagepath.substring(imagepath.lastIndexOf('.') + 1, imagepath.length());
					}
				} else {
					extension = imagepath.substring(imagepath.lastIndexOf('.') + 1, imagepath.length());
				}
				String uniqueFileName = "Img" + program.getProgramId() + "" + count + "." + extension;
				String destImgPath = imagedownload(imagepath, tempDirectory, extension, uniqueFileName);
				String checksumMD5 = DigestUtils.md5DigestAsHex(Files.newInputStream(Paths.get(destImgPath)));
				File file = new File(destImgPath);
				String bytes = String.valueOf(file.length());
				/** File Name */
				suGroupUtil.setFilename(uniqueFileName);
				/** File size */
				suGroupUtil.setSize(bytes);
				/** File checksum */
				suGroupUtil.setChecksum(checksumMD5);
				inputFilesToPack.add(new File(destImgPath).getAbsoluteFile());
			} else {
				throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.IMG_ERR + " :" + program.getTitle(), null);
			}
			/**
			 * Before submitting a new in-app purchase for review, we must include a <price>
			 * block for each of the 175 territories and the <price> block should include
			 * only the <territory> and <price_tier> tags. Program price for US territory
			 * will be fetched from 'Program Prices' table
			 */

			/* Cleared For Sale based on program status*/
			boolean clearedforsale = false;
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				clearedforsale = true;
			}
			/*
			 * setting initial or update pricing based on publish/republish
			 * */
			List<IAPNewPricing> pricingTier = setPriceTierForTerritories(program.getProgramPrices().getTier());
			suGroupUtil.setIapPricing(pricingTier);

			/* Setting cleared For Sale*/
			suGroupUtil.setClearedforsale(clearedforsale);
		   	/*List<InitialPricing> priceList = initialPricingRepository.findAll();
				List<IAPPricingUtils> pricingTier = new ArrayList<>();
				for (InitialPricing price : priceList) {
					IAPPricingUtils iPricingUtils = new IAPPricingUtils();
					if (price.getTerritory().equalsIgnoreCase(InAppPurchaseConstants.US) && program.getProgramPrice() != null) {
						iPricingUtils.setTerritory(price.getTerritory());
						iPricingUtils.setTier(program.getProgramPrices().getTier());
					} else {
						iPricingUtils.setTerritory(price.getTerritory());
						iPricingUtils.setTier(Integer.parseInt(price.getTier()));
					}
					pricingTier.add(iPricingUtils);
				}*/

			/** Introductory & Promotional Offers */
			List<DiscountOfferMapping> uploadedDiscounts = new ArrayList<>();

			if (program.getProgramDiscountMapping() != null && !program.getProgramDiscountMapping().isEmpty()) {
				List<DiscountOfferMapping> promoDiscounts = discountOfferMappingRepository.findByProgramsProgramIdAndNeedDiscountUpdateAndOfferCodeDetailIsNewUser(program.getProgramId(), true, false);
				List<DiscountOfferMapping> introRequiredDiscounts = discountOfferMappingRepository.findByProgramsProgramIdAndNeedDiscountUpdateAndOfferCodeDetailIsNewUser(program.getProgramId(), true, true);
				List<DiscountOfferMapping> introDiscounts = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsNewUser(program.getProgramId(), true);
				//Construct Promotional Offer Block
				if (!promoDiscounts.isEmpty()) {
					List<PromotionalOffer> promoOffers = new ArrayList<>();
					for (DiscountOfferMapping promoDiscount : promoDiscounts) {

						String offerStatus = promoDiscount.getOfferCodeDetail().getOfferStatus();
						if (!(DiscountsConstants.OFFER_INACTIVE.equalsIgnoreCase(offerStatus) || KeyConstants.KEY_EXPIRED.equalsIgnoreCase(offerStatus))) {
							PromotionalOffer offer = constructPromotionalOfferBlocks(promoDiscount, programStatus);
							promoOffers.add(offer);
							uploadedDiscounts.add(promoDiscount);
						}
					}
					if (!promoOffers.isEmpty()) {
						suGroupUtil.setPromotionalOffers(promoOffers);
					}
				}
				//Construct Intro Offer Block
				// Always add Active offer here to construct block.
				// To remove any offer add active offer to upload(omit In active offer)
				if (!introDiscounts.isEmpty()) {
					boolean flag = false;
					if (!introRequiredDiscounts.isEmpty()) {
						List<IntroductoryOffer> offer = new ArrayList<>();
						for (DiscountOfferMapping introDiscount : introDiscounts) {//introDiscounts
							// !introRequiredDiscounts.isEmpty() ->to check new discounts updates
							if (Boolean.TRUE.equals(introDiscount.getNeedDiscountUpdate()) && introDiscount.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE)
									&& programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
								flag = true;
								List<InitialPricing> priceList = initialPricingRepository.findAll();
								for (InitialPricing price : priceList) {
									offer.add(constructIntroductoryOffer(introDiscount, price.getTerritory()));
								}
							}
		    				/*else {
			    				System.out.println("else part");
			    				//Its already removed from app store
			    				if(Boolean.TRUE.equals(introDiscount.getNeedDiscountUpdate())) {
			    					flag=false;
			    				}
			    			}*/
							uploadedDiscounts.add(introDiscount);
						}
						if (!flag) { //|| programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)
							// If you deliver an empty tag (<offers></offers> or <offers/>), all existing and future offers will be removed.
							suGroupUtil.setRemoveIntroductoryOffers("");
						} else {
							log.info("intro offer list size :" + offer.size());
							suGroupUtil.setIntroductoryOffers(offer);
						}
					}

				}//
			}
			//
			suGroupUtils.add(suGroupUtil);
			/** Added programs */
			eMapperUtils.setSubscriptionGroup(suGroupUtils);
			/** XML Generation */
			generateXMLForNewPrice(eMapperUtils, tempDirectory);
			inputFilesToPack.add(new File(tempDirectory + InAppPurchaseConstants.METADATA_XML).getAbsoluteFile());
			/** Save Subscription Group Information */
			if (!programEntity.isEmpty()) {
				programRepository.save(program);
			}
			/** Remove duplicate Screenshots */
			List<File> newList = inputFilesToPack.stream().distinct().collect(Collectors.toList());
			/** Create Zip */
			File[] arrayOfFiles = new File[]{};
			arrayOfFiles = newList.toArray(arrayOfFiles);
			if (arrayOfFiles == null || arrayOfFiles.length == 0) {
				throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.FLE_ERR, null);
			}
			try {
				ZipUtil.packEntries(arrayOfFiles, new File(zipFilePath));
			} catch (ZipException exception) {
				throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.ZIP_ERR, exception.getMessage());
			}
			/** Move files from TempDirectory to Respective folder*/
			String filepath = "";
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				filepath = "/usr/local/itms/job/publish/";
				//filepath = "E:\\FITWISE PROJECT\\Documents\\test";
			} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
				filepath = "/usr/local/itms/job/unPublish/";
			}
			copyFile(tempDirectory, filepath);

			/** Save Jobfile Information and cleanup existing data*/
			IapJobInputs iapJobInputs = iapJobInputsRepository.findTop1ByProgram(program);
			if (iapJobInputs != null) {
				if (!StringUtils.isEmpty(iapJobInputs.getTempDir())) {
					File file = new File(iapJobInputs.getTempDir());
					FileUtils.deleteDirectory(file);
				}
			} else {
				iapJobInputs = new IapJobInputs();
			}
			iapJobInputs.setJobfileName(zipFileName);
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				iapJobInputs.setStatus(InstructorConstant.PUBLISH);
			} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
				iapJobInputs.setStatus(InstructorConstant.UNPUBLISH);
			}
			iapJobInputs.setTempDir(tempDirectory);
			iapJobInputs.setProgram(program);
			iapJobInputsRepository.save(iapJobInputs);
			/** Run script file */
			executeCommand(programStatus);
			logFile = new File(uploadFilePath);
			if (logFile.exists()) {
				int counter = 0;
				int lines_to_read = 4;
				boolean status = false;
				ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(logFile);
				String allContent = "";
				while (counter < lines_to_read) {
					String content = reversedLinesFileReader.readLine();
					System.out.println("Line " + counter + " : " + content);
					counter++;
					if (content.contains("packages were uploaded successfully")) {
						status = true;
						break;
					}
					allContent += content;
				}
				if (status) {
					//Set discount update flag as 'false'. Since it's already uploaded successfully into the app store
					if (!uploadedDiscounts.isEmpty()) {
						for (DiscountOfferMapping discount : uploadedDiscounts) {
							discount.setNeedDiscountUpdate(false);
							discountOfferMappingRepository.save(discount);
						}
						uploadedDiscounts.clear();
					}
				}

				if (!status) {
					throw new ApplicationException(Constants.ERROR_STATUS, allContent, null);
				}
			}
		} else {
			throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.VEN_ERR, null);
		}
		Map<String, String> uploadDataMap = new HashMap<>();
		uploadDataMap.put(KeyConstants.KEY_STATUS, itmsUploadStatus);
		return new ResponseModel(Constants.SUCCESS_STATUS, InAppPurchaseConstants.OK_MSG, uploadDataMap);
	}

	public ResponseModel uploadXMLForUpdatePriceProgram(String programStatus, Programs program, boolean clearForSaleAllowed) throws IOException {
		String uploadFilePath = "";
		String itmsUploadStatus = DBConstants.ITMS_UPDATED;
		if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
			uploadFilePath = "/usr/local/itms/uploadlog";
		} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
			uploadFilePath = "/usr/local/itms/unpublog";
		}
		File logFile = new File(uploadFilePath);
		if (logFile.exists()) {
			logFile.delete();
		}
		log.info("------------ Method :: buildMetadata (Generate XML) ------------");
		AppInformation appInformation = appInformationRepository.findByVendorId(InAppPurchaseConstants.VENDOR_ID);
		AppConfigKeyValue appConfigKeyValue = appConfigKeyValueRepository.findByKeyString(Constants.FAQ_URL);
		if (appInformation != null) {
			/** App level information from AppStore/iTune */
			ElementsMapperUpdatePrice eMapperUtils = new ElementsMapperUpdatePrice();
			eMapperUtils.setXmlns(appInformation.getITunepackage());
			eMapperUtils.setVersion(appInformation.getSoftwareVersion());
			/** Provider Id */
			eMapperUtils.setProvider(appInformation.getProviderId());
			/** Team Id */
			eMapperUtils.setTeamId(appInformation.getTeamId());
			/** Apple ID */
			eMapperUtils.setAppleId(appInformation.getAppleId());
			/** SKU */
			eMapperUtils.setVendorId(appInformation.getVendorId());
			VersionUtils vUtils = new VersionUtils();
			AppVersionInfo version = appVersionInfoRepository.findByIsLatestVersionAndAppPlatformAndApplication(true, DBConstants.IOS, KeyConstants.KEY_MEMBER);
			vUtils.setAppVersion(version.getAppVersion());
			/** Display name for App */
			vUtils.setAppTitle(appInformation.getAppTitle());
			/** Support URL */
			vUtils.setSupportURL(appConfigKeyValue.getValueString());
			/** Description*/
			vUtils.setAppDescription(appInformation.getDescription());
			eMapperUtils.setVersionUtil(vUtils);
			List<SubscriptionGroupUpdatePrice> suGroupUtils = new ArrayList<>();
			/**
			 * List of all Published Programs. Each program has mapped into each Subscription Group
			 */
			//Email Notification Variables
			//AppConfigKeyValue idConfigKeyValue=appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
			//String productId=idConfigKeyValue.getValueString();
			List<Programs> programEntity = new ArrayList<>();
			List<File> inputFilesToPack = new ArrayList<>();
			final String SIMPLEDATE = "yyyyMMddHHmmss";
			Format f = new SimpleDateFormat(SIMPLEDATE);
			String dateVal = f.format(new Date());
			final String tempDirectory = Files.createTempDirectory(InAppPurchaseConstants.DIR_PREFIX) + File.separator;
			String zipFileName = "";
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				zipFileName = InAppPurchaseConstants.JOB_PUBLISH.concat(dateVal).concat(InAppPurchaseConstants.ZIP_EXTN);
			} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
				zipFileName = InAppPurchaseConstants.JOB_UNPUBLISH.concat(dateVal).concat(InAppPurchaseConstants.ZIP_EXTN);
			}
			final String zipFilePath = tempDirectory + zipFileName;
			//System.out.println("zipFilePath >>>"+zipFilePath);
			int count = 0;
			count++;
			SubscriptionGroupUpdatePrice suGroupUtil = new SubscriptionGroupUpdatePrice();
			/** Subscription Group Name */
			/** Forming SubscriptionGroup Entity */
			SubscriptionGroup sGroup = new SubscriptionGroup();
			String subsGroupName;
			if (program.getSubscriptionGroup() != null && program.getSubscriptionGroup().getSubscriptionGroupName() != null && program.getSubscriptionGroup().getSubscriptionGroupName().length() > 0) {
				/**
				 * If upload failed subscriptionGroupName is already there in table. We can use
				 * that else go for iteration to find
				 */
				subsGroupName = program.getSubscriptionGroup().getSubscriptionGroupName();
				suGroupUtil.setSubscriptioGroupname(subsGroupName);
			} else {
				Long userid = null;
				if (program.getOwner() != null) {
					userid = program.getOwner().getUserId();
				}
				subsGroupName = findSubcriptionGrpName(userid, program);
				suGroupUtil.setSubscriptioGroupname(subsGroupName);
				sGroup.setSubscriptionGroupName(subsGroupName);
				sGroup.setSubscriptionDisplayName(program.getTitle());
				programEntity.add(program);
			}
			program.setSubscriptionGroup(sGroup);
			/** Localization Title ->Program Title */
			suGroupUtil.setTitle(program.getTitle());
			/** Product Id -> Program Id */
			AppConfigKeyValue configKeyValue = appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
			suGroupUtil.setProductId(configKeyValue.getValueString().concat(".").concat(String.valueOf(program.getProgramId())));
			/** Reference Name(Unique) -> Program Title */
			List<String> displayName = sGroupRepository.getAllSubscriptionDisplayName();
			if (!displayName.isEmpty() && displayName.contains(program.getTitle())) {
				suGroupUtil.setReferenceName(subsGroupName);
			} else {
				suGroupUtil.setReferenceName(program.getTitle());
			}

			/** Program Type */
			suGroupUtil.setType(InAppPurchaseConstants.PROGRAM_TYPE);
			/** Program Duration */
			suGroupUtil.setDuration(InAppPurchaseConstants.ONE_MONTH);
			/** Subscription Display Name */
			suGroupUtil.setSubscriptionDisplayName(program.getTitle());
			/** Program Description */
			suGroupUtil.setDescription(program.getShortDescription());

			/** Review Screenshot */
			if (program.getImage() != null) {
				String imagepath = program.getImage().getImagePath();
				String extension;
				if (imagepath.contains("vimeo")) {
					//https://i.vimeocdn.com/video/856397504_640x360.jpg?r=pad
					if (imagepath.contains("?")) {
						extension = imagepath.substring(imagepath.lastIndexOf('.') + 1, imagepath.lastIndexOf('?'));
					} else {
						extension = imagepath.substring(imagepath.lastIndexOf('.') + 1, imagepath.length());
					}
				} else {
					extension = imagepath.substring(imagepath.lastIndexOf('.') + 1, imagepath.length());
				}
				String uniqueFileName = "Img" + program.getProgramId() + "" + count + "." + extension;
				String destImgPath = imagedownload(imagepath, tempDirectory, extension, uniqueFileName);
				String checksumMD5 = DigestUtils.md5DigestAsHex(Files.newInputStream(Paths.get(destImgPath)));
				File file = new File(destImgPath);
				String bytes = String.valueOf(file.length());
				/** File Name */
				suGroupUtil.setFilename(uniqueFileName);
				/** File size */
				suGroupUtil.setSize(bytes);
				/** File checksum */
				suGroupUtil.setChecksum(checksumMD5);
				inputFilesToPack.add(new File(destImgPath).getAbsoluteFile());
			} else {
				throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.IMG_ERR + " :" + program.getTitle(), null);
			}
			/**
			 * Before submitting a new in-app purchase for review, we must include a <price>
			 * block for each of the 175 territories and the <price> block should include
			 * only the <territory> and <price_tier> tags. Program price for US territory
			 * will be fetched from 'Program Prices' table
			 */

			/* Cleared For Sale based on program status*/
			boolean clearedforsale = false;
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				clearedforsale = true;
			}
			/*
			 * setting initial or update pricing based on publish/republish
			 * */
			List<IAPUpdatePricing> pricingUpdateTier = setUpdatePriceTierForTerritories(program.getProgramPrices().getTier());
			suGroupUtil.setIapPricing(pricingUpdateTier);
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH) && !clearForSaleAllowed) {
				clearedforsale = false;
				itmsUploadStatus = DBConstants.ITMS_AWAITING_CLEAR_FOR_SALE;
			}

			/* Setting cleared For Sale*/
			suGroupUtil.setClearedforsale(clearedforsale);
		   	/*List<InitialPricing> priceList = initialPricingRepository.findAll();
				List<IAPPricingUtils> pricingTier = new ArrayList<>();
				for (InitialPricing price : priceList) {
					IAPPricingUtils iPricingUtils = new IAPPricingUtils();
					if (price.getTerritory().equalsIgnoreCase(InAppPurchaseConstants.US) && program.getProgramPrice() != null) {
						iPricingUtils.setTerritory(price.getTerritory());
						iPricingUtils.setTier(program.getProgramPrices().getTier());
					} else {
						iPricingUtils.setTerritory(price.getTerritory());
						iPricingUtils.setTier(Integer.parseInt(price.getTier()));
					}
					pricingTier.add(iPricingUtils);
				}*/

			/** Introductory & Promotional Offers */
			List<DiscountOfferMapping> uploadedDiscounts = new ArrayList<>();

			if (program.getProgramDiscountMapping() != null && !program.getProgramDiscountMapping().isEmpty()) {
				List<DiscountOfferMapping> promoDiscounts = discountOfferMappingRepository.findByProgramsProgramIdAndNeedDiscountUpdateAndOfferCodeDetailIsNewUser(program.getProgramId(), true, false);
				List<DiscountOfferMapping> introRequiredDiscounts = discountOfferMappingRepository.findByProgramsProgramIdAndNeedDiscountUpdateAndOfferCodeDetailIsNewUser(program.getProgramId(), true, true);
				List<DiscountOfferMapping> introDiscounts = discountOfferMappingRepository.findByProgramsProgramIdAndOfferCodeDetailIsNewUser(program.getProgramId(), true);
				//Construct Promotional Offer Block
				if (!promoDiscounts.isEmpty()) {
					List<PromotionalOffer> promoOffers = new ArrayList<>();
					for (DiscountOfferMapping promoDiscount : promoDiscounts) {
						PromotionalOffer offer = constructPromotionalOfferBlocks(promoDiscount, programStatus);
						promoOffers.add(offer);
						uploadedDiscounts.add(promoDiscount);
					}
					suGroupUtil.setPromotionalOffers(promoOffers);
				}
				//Construct Intro Offer Block
				// Always add Active offer here to construct block.
				// To remove any offer add active offer to upload(omit In active offer)
				if (!introDiscounts.isEmpty()) {
					boolean introDiscountAvailable = false;
					if (!introRequiredDiscounts.isEmpty()) {
						List<IntroductoryOffer> offer = new ArrayList<>();
						for (DiscountOfferMapping introDiscount : introDiscounts) {//introDiscounts
							// !introRequiredDiscounts.isEmpty() ->to check new discounts updates
							if (Boolean.TRUE.equals(introDiscount.getNeedDiscountUpdate()) && introDiscount.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_ACTIVE)
									&& programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
								introDiscountAvailable = true;
								List<InitialPricing> priceList = initialPricingRepository.findAll();
								for (InitialPricing price : priceList) {
									offer.add(constructIntroductoryOffer(introDiscount, price.getTerritory()));
								}
							}
		    				/*else {
			    				System.out.println("else part");
			    				//Its already removed from app store
			    				if(Boolean.TRUE.equals(introDiscount.getNeedDiscountUpdate())) {
			    					flag=false;
			    				}
			    			}*/
							uploadedDiscounts.add(introDiscount);
						}
						if (!introDiscountAvailable) { //|| programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)
							// If you deliver an empty tag (<offers></offers> or <offers/>), all existing and future offers will be removed.
							suGroupUtil.setRemoveIntroductoryOffers("");
						} else {
							log.info("intro offer list size :" + offer.size());
							suGroupUtil.setIntroductoryOffers(offer);
						}
					}

				}//
			}
			//
			suGroupUtils.add(suGroupUtil);
			/** Added programs */
			eMapperUtils.setSubscriptionGroup(suGroupUtils);
			/** XML Generation */
			generateXMLForUpdatePrice(eMapperUtils, tempDirectory);
			inputFilesToPack.add(new File(tempDirectory + InAppPurchaseConstants.METADATA_XML).getAbsoluteFile());
			/** Save Subscription Group Information */
			if (!programEntity.isEmpty()) {
				programRepository.save(program);
			}
			/** Remove duplicate Screenshots */
			List<File> newList = inputFilesToPack.stream().distinct().collect(Collectors.toList());
			/** Create Zip */
			File[] arrayOfFiles = new File[]{};
			arrayOfFiles = newList.toArray(arrayOfFiles);
			if (arrayOfFiles == null || arrayOfFiles.length == 0) {
				throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.FLE_ERR, null);
			}
			try {
				ZipUtil.packEntries(arrayOfFiles, new File(zipFilePath));
			} catch (ZipException exception) {
				throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.ZIP_ERR, exception.getMessage());
			}
			/** Move files from TempDirectory to Respective folder*/
			String filepath = "";
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				filepath = "/usr/local/itms/job/publish/";
			} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
				filepath = "/usr/local/itms/job/unPublish/";
			}
			copyFile(tempDirectory, filepath);

			/** Save Jobfile Information and cleanup existing data*/
			IapJobInputs iapJobInputs = iapJobInputsRepository.findTop1ByProgram(program);
			if (iapJobInputs != null) {
				if (!StringUtils.isEmpty(iapJobInputs.getTempDir())) {
					File file = new File(iapJobInputs.getTempDir());
					FileUtils.deleteDirectory(file);
				}
			} else {
				iapJobInputs = new IapJobInputs();
			}
			iapJobInputs.setJobfileName(zipFileName);
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				iapJobInputs.setStatus(InstructorConstant.PUBLISH);
			} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {
				iapJobInputs.setStatus(InstructorConstant.UNPUBLISH);
			}
			iapJobInputs.setTempDir(tempDirectory);
			iapJobInputs.setProgram(program);
			iapJobInputsRepository.save(iapJobInputs);
			/** Run script file */
			executeCommand(programStatus);
			logFile = new File(uploadFilePath);
			if (logFile.exists()) {
				int counter = 0;
				int lines_to_read = 4;
				boolean status = false;
				ReversedLinesFileReader reversedLinesFileReader = null;
				String allContent = "";
				try{
					reversedLinesFileReader = new ReversedLinesFileReader(logFile);
					while (counter < lines_to_read) {
						String content = reversedLinesFileReader.readLine();
						log.info("Line " + counter + " : " + content);
						counter++;
						if (content.contains("packages were uploaded successfully")) {
							status = true;
							break;
						}
						allContent += content;
					}
				}catch (IOException exception){
					log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
				}finally {
					if(reversedLinesFileReader != null){
						reversedLinesFileReader.close();
					}
				}
				if (status) {
					//Set discount update flag as 'false'. Since it's already uploaded successfully into the app store
					if (!uploadedDiscounts.isEmpty()) {
						for (DiscountOfferMapping discount : uploadedDiscounts) {
							discount.setNeedDiscountUpdate(false);
							discountOfferMappingRepository.save(discount);
						}
						uploadedDiscounts.clear();
					}
				}

				if (!status) {
					throw new ApplicationException(Constants.ERROR_STATUS, allContent, null);
				}
			}
		} else {
			throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.VEN_ERR, null);
		}
		Map<String, String> uploadDataMap = new HashMap<>();
		uploadDataMap.put(KeyConstants.KEY_STATUS, itmsUploadStatus);
		return new ResponseModel(Constants.SUCCESS_STATUS, InAppPurchaseConstants.OK_MSG, uploadDataMap);
	}

	private void sendNotification(Map<String,String> notificationMap,String programStatus) {
		try {
			String subject="AppStore - In-App Purchases Upload for ".concat(programStatus);
			String text="\n The following programs have been uploaded into the App Store. Please find the details below.\n"
			         + "<br> <table width='100%' border='1' align='center'>"
			                + "<tr align='left'>"
			                + "<td><b>Program Id & Name <b></td>"
			                + "<td><b>Program Price <b></td>"
			                + "</tr>";

			for (Map.Entry entry : notificationMap.entrySet()) {
				text = text + "<tr align='left'>" + "<td>" + entry.getKey() + "</td>" + "<td>" + entry.getValue()
						+ "</td>" + "</tr>";
			}
			String mailBody = EmailConstants.BODY_HTML_TEMPLATE.replace(EmailConstants.EMAIL_GREETINGS, "Hi Team,").replace("#MAIL_BODY#", text);
			mailSender.sendHtmlReminderMail(appleProperties.itmsNotificationToEMailAddress, subject, mailBody);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private IntroductoryOffer constructIntroductoryOffer(DiscountOfferMapping discount, String territory) {
		IntroductoryOffer introductoryOffer=new IntroductoryOffer();
		try {	
					introductoryOffer.setTerritory(territory);
					introductoryOffer.setType(DiscountsConstants.TYPE_INTRO);
					introductoryOffer.setMode(discount.getOfferCodeDetail().getOfferMode());
					if(discount.getOfferCodeDetail().getOfferMode()!=null && discount.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
						introductoryOffer.setDuration(DiscountsConstants.PAY_AS_YOU_GO_DURATION);
						introductoryOffer.setNoOfPeriods(String.valueOf(discount.getOfferCodeDetail().getOfferDuration().getDurationInMonths()));
						introductoryOffer.setTier(String.valueOf(discount.getOfferCodeDetail().getOfferPrice().getTier()));
					}else if(discount.getOfferCodeDetail().getOfferMode()!=null && discount.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)){
						introductoryOffer.setDuration(discount.getOfferCodeDetail().getOfferDuration().getDurationPeriod());
						introductoryOffer.setNoOfPeriods(DiscountsConstants.DEFAULT_PERIOD);
					}
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date now = new Date();

					Date offerStart = discount.getOfferCodeDetail().getOfferStartingDate();
                    Date offerEnd = discount.getOfferCodeDetail().getOfferEndingDate();

                    /*
                    * If start date is a past date, today is set as start date
                    * */
                    if(offerStart.before(now)) {
						offerStart = now;

						//If end date is today, we set end date as tomorrow
						if (fitwiseUtils.isSameDayInTimeZone(now, offerEnd, null)) {
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(offerStart);
							calendar.add(Calendar.DATE, 1);
							offerEnd = calendar.getTime();
						}
					}

                    //One day delay has been added for UTC to american time zone conversion
                    Calendar c = Calendar.getInstance(); 
                    c.setTime(offerStart); 
                    c.add(Calendar.DATE, 1);
                    Date startdt = c.getTime();
                    introductoryOffer.setStartDate(dateFormat.format(startdt));
                    
                    c = Calendar.getInstance(); 
                    c.setTime(offerEnd); 
                    c.add(Calendar.DATE, 1);
                    Date enddt = c.getTime();
                    introductoryOffer.setEndDate(dateFormat.format(enddt));
                    /*//Current offer we can omit start date.
                    if (offerStart.after(now) && offerEnd.after(now)) {
                    	introductoryOffer.setStartDate(dateFormat.format(discount.getOfferCodeDetail().getOfferStartingDate()));
					}
                    //Offer ending today , Apple does not allow past date as start date and today as end date.
                    // This offer will expire tomorrow by scheduler job at start of the day
                    if(!offerEnd.equals(now)) {
                    	introductoryOffer.setEndDate(dateFormat.format(discount.getOfferCodeDetail().getOfferEndingDate()));
                    }*/
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return introductoryOffer;
	}
	
	 private PromotionalOffer constructPromotionalOfferBlocks(DiscountOfferMapping discount,String programStatus) {		
		 PromotionalOffer promotionalOffer=new PromotionalOffer();		
		try {
			// Expired, InActive, UnPublished program's offer should be removed from App Store
			if (discount.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(DiscountsConstants.OFFER_INACTIVE)
					|| discount.getOfferCodeDetail().getOfferStatus().equalsIgnoreCase(KeyConstants.KEY_EXPIRED)
					|| (programStatus != null && programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH))) {
				promotionalOffer.setRemoveOffer("true");
			}
			promotionalOffer.setOfferCode(discount.getOfferCodeDetail().getOfferCode());
			//promotionalOffer.setOfferName(discount.getOfferCodeDetail().getOfferName());
			promotionalOffer.setOfferName(discount.getOfferCodeDetail().getAppleOfferName());
			promotionalOffer.setMode(discount.getOfferCodeDetail().getOfferMode());
			
			//free mode : no need to set price tier
			if(discount.getOfferCodeDetail().getOfferMode()!=null && discount.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_PAY_AS_YOU_GO)) {
				promotionalOffer.setDuration(DiscountsConstants.PAY_AS_YOU_GO_DURATION);
				promotionalOffer.setNoOfPeriods(String.valueOf(discount.getOfferCodeDetail().getOfferDuration().getDurationInMonths()));
				promotionalOffer.setIapPricing(setPriceTierForTerritories(discount.getOfferCodeDetail().getOfferPrice().getTier()));	
				
			}else if(discount.getOfferCodeDetail().getOfferMode()!=null && discount.getOfferCodeDetail().getOfferMode().equalsIgnoreCase(DiscountsConstants.MODE_FREE)){
				promotionalOffer.setDuration(discount.getOfferCodeDetail().getOfferDuration().getDurationPeriod());
				promotionalOffer.setNoOfPeriods(DiscountsConstants.DEFAULT_PERIOD);
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
			//throw new ApplicationException(Constants.ERROR_STATUS, "Method : constructPromotionalOfferBlocks,Exception when constructing PromotionalOfferBlocks", e.getMessage());
		}
		return promotionalOffer;
	}
	
	/**
	 * Construct Price Block (<price>) for all 175 territories.
	 * By default US price tier will be set for remaining all 175 territories for XML validation.Manually we should submit Program Price in App Store Connect.
	 * @param priceTier -This is for US territory
	 * @return
	 */
	private List<IAPNewPricing> setPriceTierForTerritories(int priceTier) {
		//log.info(" ---------- Method : setPriceTierForTerritories -----------------");
		List<IAPNewPricing> pricingTier = new ArrayList<>();
		try {
			// It returns all 175 territories.
			List<InitialPricing> priceList = initialPricingRepository.findAll();
			
			for (InitialPricing price : priceList) {
				IAPNewPricing priceTierObj = new IAPNewPricing();
				priceTierObj.setTerritory(price.getTerritory());
				priceTierObj.setTier(priceTier);
				pricingTier.add(priceTierObj);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return pricingTier;
	}

	private List<IAPUpdatePricing> setUpdatePriceTierForTerritories(int priceTier) {
		//log.info(" ---------- Method : setPriceTierForTerritories -----------------");
		List<IAPUpdatePricing> pricingTier = new ArrayList<>();
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		Date startDate = Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = simpleDateFormat.format(startDate);

		try {
			// It returns all 175 territories.
			List<InitialPricing> priceList = initialPricingRepository.findAll();

			for (InitialPricing price : priceList) {
				IAPUpdatePricing priceTierObj = new IAPUpdatePricing();
				priceTierObj.setTerritory(price.getTerritory());
				priceTierObj.setTier(priceTier);
				priceTierObj.setStartDate(formattedDate);
				pricingTier.add(priceTierObj);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return pricingTier;
	}

	private String findSubcriptionGrpName(Long ownerId, Programs programs) {
		log.info(" ---------- Method : findSubcriptionGrpName -----------------");
		String subscriptioGroupName;
		String firstName;
		String lastName;
		String symbol="-";
		String empty="";
		Long length=(long)64;
		
		String program=programs.getTitle();
		try {
			List<String> sGroupFromDB = sGroupRepository.getAllSubscriptionGroupName();
			if(!sGroupFromDB.isEmpty()) {
				log.info("Size {}", sGroupFromDB.size());
			}
			
			if(ownerId!=null) {
				UserProfile userProfile=userProfileRepository.findByUserUserId(ownerId);
				firstName=userProfile.getFirstName();
				lastName=userProfile.getLastName();
				subscriptioGroupName=program.concat(symbol).concat(firstName).concat(empty).concat(lastName);
				
				if(subscriptioGroupName.length()<=length && !sGroupFromDB.contains(subscriptioGroupName) ) {
					return subscriptioGroupName;
				}else if(program.concat(symbol).concat(firstName).length()<=64 && !sGroupFromDB.contains(program.concat(symbol).concat(firstName))) {
					subscriptioGroupName=program.concat(symbol).concat(firstName);
					return subscriptioGroupName;
				}else if(program.concat(symbol).concat(lastName).length()<=64 && !sGroupFromDB.contains(program.concat(symbol).concat(lastName))) {
					subscriptioGroupName=program.concat(symbol).concat(lastName);
					return subscriptioGroupName;
				}else {
					String random=UUID.randomUUID().toString().toUpperCase();
					String append=random.substring(0, random.indexOf('-')+1);		        	
					subscriptioGroupName=program.concat(symbol).concat(append).concat(String.valueOf(programs.getProgramId()));
					return subscriptioGroupName;
				}
			}else {
				String random=UUID.randomUUID().toString().toUpperCase();
				String append=random.substring(0, random.indexOf('-')+1);		 
				subscriptioGroupName=program.concat(symbol).concat(append).concat(String.valueOf(programs.getProgramId()));
				return subscriptioGroupName;
			}			
		} catch (Exception e) {
			throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.SGP_ERR, e.getMessage());
		}
	}
	
	private static void generateXMLForNewPrice(Object data, String tempDirectory) {
		try {
			JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] { ElementsMapperNewPrice.class }, null);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(data, new File(tempDirectory+InAppPurchaseConstants.METADATA_XML));
		}catch(Exception e){
			log.error("Xml generation failed : " + e.getMessage());
			throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.XML_ERR, e.getMessage());
		}
	}

	private static void generateXMLForUpdatePrice(Object data, String tempDirectory) {
		try {
			JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] { ElementsMapperUpdatePrice.class }, null);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(data, new File(tempDirectory+InAppPurchaseConstants.METADATA_XML));
		}catch(Exception e){
			log.error("Xml generation failed : " + e.getMessage());
			throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.XML_ERR, e.getMessage());
		}
	}
	
	public String imagedownload(String path,String tempDirectory,String extension,String uniqueFileName) {
		String destPath;
		final AmazonS3 s3Client = new AmazonS3Client();
		BufferedImage srcImage;
		try {
			if(path.contains("vimeo")) {
				path=path.replaceAll(" ","%20");
				URL url = new URL(path);
			     srcImage = ImageIO.read(url);
			}else {
				S3Object s3Object = s3Client.getObject(new GetObjectRequest(awsProperties.getAwsS3BucketResources(), path.replace(awsProperties.getAwsResourceBaseUrl() + "/", "")));
				InputStream objectData = s3Object.getObjectContent();
				srcImage = javax.imageio.ImageIO.read(objectData);
			}
			Image thumbnail = srcImage.getScaledInstance(640, 920, Image.SCALE_SMOOTH);
			BufferedImage resizedImage = new BufferedImage(640, 920, BufferedImage.TYPE_INT_RGB);
			resizedImage.getGraphics().drawImage(thumbnail, 0, 0, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(resizedImage, extension, baos);
			InputStream in = new ByteArrayInputStream(baos.toByteArray());
			destPath=tempDirectory+uniqueFileName;
			IOUtils.copy(in, new FileOutputStream(destPath));
		} catch (Exception e) {
			log.error("File download failure : " + e.getMessage());
			throw new ApplicationException(Constants.ERROR_STATUS, InAppPurchaseConstants.DWN_ERR, e.getMessage());
		}
		return destPath;
	}

	private static void copyFile(String srcDir, String targetFilePath){
		File sourceDir = new File(srcDir);
		File targetFile = new File(targetFilePath);
		try	{
			FileUtils.copyDirectory(sourceDir, targetFile);
		} catch (IOException e)	{
			log.error("File copy failed.");
			throw new ApplicationException(Constants.ERROR_STATUS,"Error in copyFile Method ",e.getMessage());
		}
	}

	public void executeCommand(String programStatus) {
		log.info("----------- Entering in executeCommand method ----------------");
		String shellscript="";
		try {
			// Run a shell script
			if (programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
				shellscript="/usr/local/itms/bin/publishPrograms.sh";
			} else if (programStatus.equalsIgnoreCase(InstructorConstant.UNPUBLISH)) {	
				shellscript="/usr/local/itms/bin/unpublishPrograms.sh";
			}
			Process process = Runtime.getRuntime().exec(shellscript);
			// -- Windows --
			// Run a command
			//Process process = Runtime.getRuntime().exec("cmd /c dir E:\\FITWISE PROJECT\\SourceCode_STS\\trainnr-backend\\itms\\bin\\fileMonitering_windows.sh");
			//Run a bat file
			//Process process = Runtime.getRuntime().exec("cmd /c hello.bat", null, new File("C:\\Users\\mkyong\\"));
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

			int exitVal = process.waitFor();
			if (exitVal == 0) {
				log.info("Execute command completed.");
			} else {
				log.error("Execute command failed");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApplicationException(Constants.ERROR_STATUS,"Error in executeCommand Method "+e.getMessage(),e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new ApplicationException(Constants.ERROR_STATUS,"Error in executeCommand Method "+e.getMessage(),e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(Constants.ERROR_STATUS,"Error in executeCommand Method "+e.getMessage(),e.getMessage());
		}
	}

}
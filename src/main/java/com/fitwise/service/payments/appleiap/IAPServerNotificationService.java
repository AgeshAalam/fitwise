package com.fitwise.service.payments.appleiap;


import com.fitwise.components.UserComponents;
import com.fitwise.constants.*;
import com.fitwise.constants.discounts.DiscountsConstants;
import com.fitwise.constants.payments.appleiap.InAppPurchaseConstants;
import com.fitwise.constants.payments.appleiap.NotificationConstants;
import com.fitwise.entity.*;
import com.fitwise.entity.discounts.DiscountOfferMapping;
import com.fitwise.entity.discounts.OfferCodeDetail;
import com.fitwise.entity.discounts.OfferCodeDetailAndOrderMapping;
import com.fitwise.entity.payments.appleiap.*;
import com.fitwise.entity.payments.common.InvoiceManagement;
import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.payments.paypal.UserAccountAndPayPalIdMapping;
import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import com.fitwise.entity.subscription.*;
import com.fitwise.exception.ApplicationException;
import com.fitwise.properties.AppleProperties;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.properties.MobileAppProperties;
import com.fitwise.repository.AppConfigKeyValueRepository;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.ProgramRepository;
import com.fitwise.repository.SubscriptionPaymentHistoryRepository;
import com.fitwise.repository.discountsRepository.DiscountOfferMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailAndOrderMappingRepository;
import com.fitwise.repository.discountsRepository.OfferCodeDetailRepository;
import com.fitwise.repository.order.InvoiceManagementRepository;
import com.fitwise.repository.order.OrderManagementRepository;
import com.fitwise.repository.payments.appleiap.*;
import com.fitwise.repository.payments.stripe.connect.StripeAccountAndUserMappingRepository;
import com.fitwise.repository.payments.stripe.paypal.UserAccountAndPayPalIdMappingRepository;
import com.fitwise.repository.subscription.*;
import com.fitwise.service.dynamiclink.DynamicLinkService;
import com.fitwise.service.qbo.FitwiseQboEntityService;
import com.fitwise.service.receiptInvoice.InvoicePDFGenerationService;
import com.fitwise.service.validation.ValidationService;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.mail.AsyncMailer;
import com.fitwise.utils.payments.OrderNumberGenerator;
import com.fitwise.view.ResponseModel;
import com.fitwise.view.payments.appleiap.InitialPurchaseRequestView;
import com.fitwise.view.payments.appleiap.InitialPurchaseResponseView;
import com.fitwise.view.payments.appleiap.PayloadJsonRequestView;
import com.fitwise.view.payments.appleiap.SignatureResponseView;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IAPServerNotificationService {
    @Autowired
    private ProgramRepository programsRepo;

    @Autowired
    private OrderManagementRepository orderManagementRepo;

    @Autowired
    private PlatformTypeRepository platformsRepo;

    @Autowired
    private UserComponents userComponents;

    @Autowired
    private AppConfigKeyValueRepository appConfigKeyValueRepository;

    @Autowired
    private IosReceiptInfoRepository iReceiptInfoRepository;

    @Autowired
    private ProgramSubscriptionRepo pgmSubscriptionRepo;

    @Autowired
    private SubscriptionPlansRepo subscriptionPlansRepo;

    @Autowired
    private SubscriptionStatusRepo subscriptionStatusRepo;

    @Autowired
    private SubscriptionAuditRepo subscriptionAuditRepo;

    @Autowired
    private SubscriptionTypesRepo subscriptionTypesRepo;

    @Autowired
    private ApplePaymentRepository applePaymentRepository;

    @Autowired
    private VerifyReceiptRepository verifyReceiptRepository;

    @Autowired
    private InvoiceManagementRepository invoiceManagementRepository;

    @Autowired
    private AppleSubscriptionStatusRepository appleSubscriptionStatusRepo;

    @Autowired
    private AppleProductSubscriptionRepository aRepository;

    @Autowired
    private FitwiseQboEntityService fitwiseQboEntityService;

    @Autowired
    private GeneralProperties generalProperties;

    @Autowired
    private SubscriptionPaymentHistoryRepository subscriptionPaymentHistoryRepository;

    @Autowired
    private ValidationService validationService;
    @Autowired
    FitwiseUtils fitwiseUtils;

    @Autowired
    AppleProperties appleProperties;

	@Autowired
	private MobileAppProperties mobileAppProperties;
	
	@Autowired
	OfferCodeDetailRepository offerCodeDetailRepository;
	
	@Autowired
	OfferCodeDetailAndOrderMappingRepository offerCodeDetailAndOrderMappingRepository;
	
	@Autowired
	DiscountOfferMappingRepository disOfferMappingRepository;

	@Autowired
	private EmailContentUtil emailContentUtil;

	@Autowired
	private DynamicLinkService dynamicLinkService;

	@Autowired
	private AsyncMailer asyncMailer;

	@Autowired
	private InvoicePDFGenerationService invoicePDFGenerationService;
	
	@Autowired
	IntroOfferUserTrackingRepository  iUserTrackingRepository;

	private final StripeAccountAndUserMappingRepository stripeAccountAndUserMappingRepository;

	private final UserAccountAndPayPalIdMappingRepository userAccountAndPayPalIdMappingRepository;

	private static PrivateKey privateKey;
	
	private static final String SIGNALGORITHMS = "SHA256withECDSA";
	//private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

//    @Scheduled(cron=InAppPurchaseConstants.SCHEDULAR_RENEWAL)
    @Transactional
    public ResponseModel getExpiryProgramList() {
        String info_msg;
        //LinkedHashmap maintains insertion order.
        Map<String, AppleProductSubscription> allSubscriptionListMap = new LinkedHashMap<>();
        try {
            log.info(" -------------------Method Name : getExpiryProgramList -------------------");
            List<AppleProductSubscription> appleSubscriptions = aRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
            if(!appleSubscriptions.isEmpty()) {
                //key =UserId@@@ProgramId@@@OriginalTransactioId ; Value=appleSubscription entity
                // It will give the most recent transaction for the 'key' Combo
                for (AppleProductSubscription appleSubscription : appleSubscriptions) {
                    if (appleSubscription.getUser() != null && appleSubscription.getUser().getUserId() != null
                            && appleSubscription.getProgram() != null
                            && appleSubscription.getProgram().getProgramId() != null
                            && appleSubscription.getOriginalTransactionId() != null) {
                        String key = String.valueOf(appleSubscription.getUser().getUserId()).concat("@@")
                                .concat(String.valueOf(appleSubscription.getProgram().getProgramId())).concat("@@")
                                .concat(appleSubscription.getOriginalTransactionId());
                        allSubscriptionListMap.put(key, appleSubscription);
                    }
                }
            }
			//
            log.info("allSubscriptionListMap Size :: {}", allSubscriptionListMap.size());
            List<String> keyList=new ArrayList<>();
            if(!allSubscriptionListMap.isEmpty()) {
                List<AppleProductSubscription> listOfSubscription = new ArrayList<>(allSubscriptionListMap.values());
                if(!listOfSubscription.isEmpty()) {
                    for(AppleProductSubscription appleSubscription:listOfSubscription) {
                        // only for Active Subscription, Renewal check is required.
                        if (appleSubscription.getAppleSubscriptionStatus()!=null && appleSubscription.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
                            keyList.add(appleSubscription.getOriginalTransactionId()+"@@@"+appleSubscription.getTransactionId()+"@@@"+appleSubscription.getProgram().getProgramId()+"@@@"+appleSubscription.getUser().getUserId());
                            //log.info("Row Id -----> {}"+appleSubscription.getId());
                        }
                    }
                }
            }
            //            
            if (!keyList.isEmpty()) {
            	keyList = keyList.stream().distinct().collect(Collectors.toList());
            	log.info("Active KeyList Size :: {}", keyList.size());
                // find out current date time in 'UTC' format
            	/*String pattern = "yyyy-MM-dd HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date today = Calendar.getInstance().getTime();
                String todayAsString = df.format(today);
                Date date = null;
                
                try {
                	date = df.parse(todayAsString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/
                Date date = new Date();
                for (String keyData : keyList) {
                    String[] arrOfStr = keyData.split("@@@");
                    String originalTransactionId = arrOfStr[0];
                    String transactionId = arrOfStr[1];
                    String programId = arrOfStr[2];
                    String userId = arrOfStr[3];
                    if (originalTransactionId != null && transactionId != null && programId != null && userId != null) {
                    	try {
                    		//Check Program Status
                            //Renewal should happen only for 'Publish' status programs.
                            Programs programs = programsRepo.findByProgramId(Long.parseLong(programId));
                          
                            if(programs!=null && programs.getStatus().equalsIgnoreCase(InstructorConstant.PUBLISH)) {
                            	ProgramSubscription pgmSubscription = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(Long.valueOf(userId), Long.valueOf(programId));
                            	if(pgmSubscription!=null && pgmSubscription.getSubscribedViaPlatform().getPlatformTypeId()==2 
                            			&& pgmSubscription.isAutoRenewal() && pgmSubscription.getSubscribedDate().before(date)) {
                            		log.info(" Renew Pgm & User Id {},{}",pgmSubscription.getProgram().getProgramId(),pgmSubscription.getUser().getUserId());
                            		
                            		 ApplePayment applePayment = applePaymentRepository.findTop1ByTransactionIdAndOriginalTransactionIdAndTransactionStatusOrderByCreatedDateDesc(transactionId, originalTransactionId, KeyConstants.KEY_PAYMENT_SUCCESS);
                            		 //if (applePayment != null) {
                                         if (applePayment != null && applePayment.getExpiryDate() != null && (applePayment.getExpiryDate().before(date) || applePayment.getExpiryDate().equals(date) )) {
                                        	 log.info(" Original  & Txn Id {},{}",originalTransactionId,transactionId);
                                        	 Date purchaseDate=applePayment.getPurchaseDate();
                                        	 VerifyReceipt receipt = verifyReceiptRepository.findTop1ByOriginalTxnIdAndProgramProgramIdAndUserUserIdOrderByIdDesc(originalTransactionId, Long.valueOf(programId), Long.valueOf(userId));
                                             if (receipt != null) {
                                             	try {
                                             		validateReceiptData(receipt, purchaseDate);
                                             	}catch(Exception exp) {
                                             		exp.printStackTrace();
                                             	}
                                                 
                                             }// if loop
                                         }
                                     //} //if loop
                            	}//condition
                            }
                    	}catch(Exception e) {
                    		log.info("************** Exception occurs {}",transactionId);
                    	}                        
                    }
                    //loop ends
                }
            }
		} catch (Exception e) {
            e.printStackTrace();
        }
        info_msg="Expired Programs were auto renewed successfully";
        return new ResponseModel(Constants.SUCCESS_STATUS, info_msg, null);
    }

    @Transactional
    public ResponseModel validateReceiptData(VerifyReceipt receiptData, Date pgmPurchaseDt) {// VerifyReceiptRequestView
        // requestView
        log.info(" -------------------Method Name : validateReceiptData -------------------");
        String receiptURL=appleProperties.getVerifyReceiptEndPoint();
        String appSecretKey=appleProperties.getAppSecretKey();
        String message = "";
        String originalTxnId = receiptData.getOriginalTxnId();
        CloseableHttpResponse response;
        String encodedReceiptData = receiptData.getReceiptData();
        JSONObject payload;
        JSONObject latestReceipt = null;
        JSONObject pendingRenewal = null;
        boolean autorenew=false;
        User user=receiptData.getUser();
        String updatedReceipt="";
        String expirantIntent ="";
        List<JSONObject> renewalObjArray=new ArrayList<>();
        String pdtIdentifier=findProductIdetifier();
        String programId = pdtIdentifier.concat(".").concat(String.valueOf(receiptData.getProgram().getProgramId()));
        if (!encodedReceiptData.isEmpty()) {
            try {
				CloseableHttpClient client = HttpClients.createDefault();
				JSONObject requestData = new JSONObject();
				requestData.put(NotificationConstants.RECIEPT_DATA, encodedReceiptData);
				requestData.put(NotificationConstants.RECIEPT_PD, appSecretKey);
				requestData.put(NotificationConstants.EXCL_OLD_TXN, false);
				HttpPost httpPost = new HttpPost(receiptURL);
				StringEntity entity = new StringEntity(requestData.toString());
				httpPost.setEntity(entity);
				httpPost.setHeader(NotificationConstants.CONT_TYP, NotificationConstants.JSON);
				response = client.execute(httpPost);
				String json = EntityUtils.toString(response.getEntity());
				payload = new JSONObject(json);
				if (payload.has(NotificationConstants.LATEST_RCPT_DATA)) {
					updatedReceipt = payload.getString(NotificationConstants.LATEST_RCPT_DATA);
				}
				if (payload.get(NotificationConstants.RCPT_ST).toString().equalsIgnoreCase("0")) {// validity
					if (payload.has(NotificationConstants.LATEST_RCPT)) {
						JSONArray latestReceiptArray = payload.getJSONArray(NotificationConstants.LATEST_RCPT);
						// renewalObjArray Contains all transaction details for the given
						// program/Original transaction id
						for (int i = 0; i < latestReceiptArray.length(); i++) {
							if (latestReceiptArray.getJSONObject(i).getString(NotificationConstants.PRODUCT_ID).trim().equalsIgnoreCase(programId.trim()) 
									&& latestReceiptArray.getJSONObject(i).getString(NotificationConstants.ORIGINAL_TRANSACTION_ID).trim().equalsIgnoreCase(originalTxnId.trim())) {
								
								renewalObjArray.add(latestReceiptArray.getJSONObject(i));
							}
						}

						// Fetch 'in_app' array if the transactions not present in 'latest_receipt_info'
						// array
						if (renewalObjArray.isEmpty()) {
							JSONObject receipt = payload.getJSONObject(NotificationConstants.RECEIPT);
							JSONArray inAppArray = receipt.getJSONArray(NotificationConstants.IN_APP_ARRAY);
							for (int i = 0; i < inAppArray.length(); i++) {
								if (inAppArray.getJSONObject(i).getString(NotificationConstants.ORIGINAL_TRANSACTION_ID).trim().equalsIgnoreCase(originalTxnId.trim()) 
										&& inAppArray.getJSONObject(i).getString(NotificationConstants.PRODUCT_ID).trim().equalsIgnoreCase(programId.trim())) {
									renewalObjArray.add(inAppArray.getJSONObject(i));
								}
							}
						}
					}
					// latestReceiptArray for loop ends
					// Boolean dataAvail=false;
					log.info("renewalObjArray length {}", renewalObjArray.size());
					// Sort based on purchase date (Ascending)
					if (!renewalObjArray.isEmpty()) {
						renewalObjArray.sort(new ReceiptJSONComparator());
					}

					if (payload.has(NotificationConstants.PENDING_RENEWAL_INFO)) {
						JSONArray pendingRenewalArray = payload
								.getJSONArray(NotificationConstants.PENDING_RENEWAL_INFO);
						if (pendingRenewalArray != null && pendingRenewalArray.length() > 0) {
							for (int i = 0; i < pendingRenewalArray.length(); i++) {
								if (pendingRenewalArray.getJSONObject(i).getString(NotificationConstants.ORIGINAL_TRANSACTION_ID).trim().equalsIgnoreCase(originalTxnId.trim()) 
										&& pendingRenewalArray.getJSONObject(i).getString(NotificationConstants.AUTO_RENEW_PDT).trim().equalsIgnoreCase(programId.trim())) {
									pendingRenewal = pendingRenewalArray.getJSONObject(i);
								}
							}
						}
					}
				} else {
					message = receiptStatus(Integer.parseInt(payload.get(NotificationConstants.RCPT_ST).toString()));
				}
				// ========= Fetching Field values ==================
				if (pendingRenewal != null) {
					String renew = pendingRenewal.getString(NotificationConstants.AUTO_RENEW);
					if (renew.equalsIgnoreCase("1")) {
						autorenew = true;
					}
					if (pendingRenewal.has(NotificationConstants.EXPIRANT_INTENT)) {
						expirantIntent = pendingRenewal.getString(NotificationConstants.EXPIRANT_INTENT);
					}
				}
				log.info("auto renew & expirantIntent {} {}", autorenew, expirantIntent);
				// Fetching Newly Renewed Data from Array
				// Note : pgmPurchaseDt ==> Previous purchase date
				if (!renewalObjArray.isEmpty()) {
					for (JSONObject latest : renewalObjArray) {
						if (pgmPurchaseDt.before(dateFormat(latest.getString(NotificationConstants.PURCHASE_DT)))) {
							latestReceipt = latest;
							log.info(" Renewed Transaction ID ::: {}",latest.get(NotificationConstants.TRANSACTION_ID));
							break;
						}	
					}
				}
                    	
				    
				// Track Expired Subscription
				if (!StringUtils.isEmpty(expirantIntent)) {
					if (!renewalObjArray.isEmpty()) {
						for (JSONObject latest : renewalObjArray) {
							if (pgmPurchaseDt.equals(dateFormat(latest.getString(NotificationConstants.PURCHASE_DT)))) {
								latestReceipt = latest;
								log.info(" Expired Transaction ID ::: {}",latest.get(NotificationConstants.TRANSACTION_ID));
								break;
							}
						}
					}
					if(latestReceipt!=null) {
						String idTransaction = "";
						String idOriginaltransaction = "";
						String webLineItemId = "";
						if (latestReceipt.has(NotificationConstants.TRANSACTION_ID)) {
							idTransaction = latestReceipt.getString(NotificationConstants.TRANSACTION_ID);
						}
						if (latestReceipt.has(NotificationConstants.ORIGINAL_TRANSACTION_ID)) {
							idOriginaltransaction = latestReceipt.getString(NotificationConstants.ORIGINAL_TRANSACTION_ID);
						}
						if (latestReceipt.has(NotificationConstants.SUBSCRIPTION_ID)) {
							webLineItemId = latestReceipt.getString(NotificationConstants.SUBSCRIPTION_ID);
						}
						AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo
								.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);

						AppleProductSubscription existSubs = aRepository
								.findTop1ByProgramAndUserAndAppleSubscriptionStatusOrderByModifiedDateDesc(
										receiptData.getProgram(), user, appleSubscriptionStatus);
						if (existSubs == null) {
							saveAppleProgramSubscription(user, receiptData.getProgram(), idTransaction, webLineItemId,
									idOriginaltransaction, appleSubscriptionStatus, NotificationConstants.REN_OFF);
						}

						ProgramSubscription programSubscription = pgmSubscriptionRepo
								.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(),
										receiptData.getProgram().getProgramId());
						if (programSubscription != null) {
							programSubscription.setAutoRenewal(false);
							pgmSubscriptionRepo.save(programSubscription);
						}
					}
					
				} else if (latestReceipt != null) {

					// Check whether auto renew already happened via 'INTERACTIVE_RENEWAL' event.
					// By using this check we can avoid 2 different orders for same transaction id.
					boolean isAlreadyRenewed = true;
					String idTransaction = "";
					String idOriginaltransaction = "";
					if (latestReceipt.has(NotificationConstants.TRANSACTION_ID)) {
						idTransaction = latestReceipt.getString(NotificationConstants.TRANSACTION_ID);
					}
					if (latestReceipt.has(NotificationConstants.ORIGINAL_TRANSACTION_ID)) {
						idOriginaltransaction = latestReceipt.getString(NotificationConstants.ORIGINAL_TRANSACTION_ID);
					}
					if (idTransaction != null && idOriginaltransaction != null) {
						AppleProductSubscription applePrSubscription = aRepository.findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(idTransaction,idOriginaltransaction);
						if (applePrSubscription == null) {
							isAlreadyRenewed = false;
						}
					}
					//
					if (!isAlreadyRenewed) {
						Programs program = receiptData.getProgram();
						// ios_receipt_data entry
						IosReceiptInfo iReceiptInfo = updSubscriptionViaVerifyReceipt(latestReceipt, pendingRenewal,
								updatedReceipt);
						iReceiptInfo.setStatus(payload.get(NotificationConstants.RCPT_ST).toString());
						iReceiptInfoRepository.save(iReceiptInfo);
						// Store Encoded receipt
						VerifyReceipt verifyReceipt = verifyReceiptRepository.findTop1ByOriginalTxnIdAndProgramProgramIdAndUserUserIdOrderByIdDesc(iReceiptInfo.getOriginalTransactionId(), program.getProgramId(),user.getUserId());
						if (verifyReceipt != null) {
							verifyReceipt.setReceiptData(updatedReceipt);
							verifyReceiptRepository.save(verifyReceipt);
						}
						message = receiptStatus(Integer.parseInt(payload.get(NotificationConstants.RCPT_ST).toString()));
						log.info(" Method Name : validateReceiptData : AutoRenwal ----------");
						// Mapping Promotional offers with Orders
						OfferCodeDetail offerCode = null;
						if (iReceiptInfo.getOfferId() != null) {
							String promoOfferId = iReceiptInfo.getOfferId();
							if (!StringUtils.isEmpty(promoOfferId)) {
								offerCode = offerCodeDetailRepository.findByOfferCodeAndIsInUse(promoOfferId, true);
							}
						} else {
							// Mapping Introductory offers with Orders
							offerCode = findIntroOffer(program.getProgramId(), user.getUserId(),iReceiptInfo.getIsTrialPeriod(), iReceiptInfo.getIsInIntroOffer());

						}
						AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
						saveAppleProgramSubscription(user, program, iReceiptInfo.getTransactionId(),iReceiptInfo.getWeborderLineitemId(), iReceiptInfo.getOriginalTransactionId(),appleSubscriptionStatus, NotificationConstants.RENEWAL);
						// Populate Payment Information
						OrderManagement orderManagement = createOrderManagement(program, autorenew, user, "",KeyConstants.KEY_PROCESSING, offerCode);
						// Update Program Subscription and create Audit entry
						ProgramSubscription pgmSubscription = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(),receiptData.getProgram().getProgramId());
						updateSubscribedPgm(user, pgmSubscription, true, autorenew, iReceiptInfo.getPurchaseDate(),NotificationConstants.RENEWAL, orderManagement);
						addPaymentInfo(iReceiptInfo, orderManagement, NotificationConstants.RENEWAL, "");
						updateOrderStatus(orderManagement.getOrderId(), KeyConstants.KEY_SUCCESS);
						try{
							//Sending mail to member
							String subject = EmailConstants.PROGRAM_SUBSCRIPTION_RENEWAL_SUBJECT.replace(EmailConstants.PROGRAM_TITLE,  program.getTitle() );
							String mailBody = EmailConstants.PROGRAM_SUBSCRIPTION_RENEWAL_CONTENT.replace(EmailConstants.PROGRAM_TITLE, "<b>" + program.getTitle() + "</b>");
							String userName = fitwiseUtils.getUserFullName(user);
							User instructor = program.getOwner();
							String memberProgram = EmailConstants.MEMBER_PROGRAM_LINK.replace(EmailConstants.LITERAL_APP_URL, dynamicLinkService.constructProgramLinkForMember(program.getProgramId(),instructor));
							mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
									.replace(EmailConstants.EMAIL_SUPPORT_URL, memberProgram);
							mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);

							try {
								InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(orderManagement);
								if(invoiceManagement != null){
									File file = invoicePDFGenerationService.generateInvoicePdf(orderManagement.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
									List<String> fileList = Collections.singletonList(file.getAbsolutePath());
									asyncMailer.sendHtmlMailWithAttachment(user.getEmail(),null, subject, mailBody, fileList);
								}
							} catch (Exception e) {
								log.error("Invoice PDF generation failed for subscription renewal mail. Order id : " + orderManagement.getOrderId());
								log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
								asyncMailer.sendHtmlMail(orderManagement.getUser().getEmail(), subject, mailBody);
							}
							/*
							 * Stripe connect onboarding reminder mail
							 * */
							boolean isOnboardingDetailsSubmitted = false;
							StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
							if(stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsDetailsSubmitted() != null && stripeAccountAndUserMapping.getIsDetailsSubmitted()){
								isOnboardingDetailsSubmitted = true;
							}
							boolean isOnBoardedViaPayPal = false;
							UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
							if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
								isOnBoardedViaPayPal = true;
							}

							if (!isOnboardingDetailsSubmitted && !isOnBoardedViaPayPal) {
								userName = fitwiseUtils.getUserFullName(instructor);
								subject = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_SUBJECT;
								String trainnrDashboard = EmailConstants.STRIPE_CONNECT_ONBOARD_DASHBOARD_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl() + RestUrlConstants.APP_INSTRUCTOR_DASHBOARD);
								mailBody = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_CONTENT;
								mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
										.replace(EmailConstants.EMAIL_BODY, mailBody)
										.replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrDashboard);
								mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
								asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
							}

						}catch(Exception e){
							log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
						}
					}
				   }
                 }catch (Exception e) {
                e.printStackTrace();
                //throw new ApplicationException(Constants.ERROR_STATUS, message, e.getMessage());
            }
        } // if (!encodedReceiptData.isEmpty()) ends here

        return new ResponseModel(Constants.SUCCESS_STATUS, message, null);
    }

    private OfferCodeDetail findIntroOffer(Long programId, Long userId, Boolean isTrail, Boolean isIntro) {
    	OfferCodeDetail offerCode=null;
    	// Check eligibility for 'New User' offer.
		IntroOfferUserTracking track = iUserTrackingRepository.findTop1ByProgramIdAndUserIdOrderByCreatedDateDesc(programId,userId);
		if (track != null) {
			if (Boolean.FALSE.equals(track.getIsAvailedIntroOffer())) {
				OfferCodeDetail offer = offerCodeDetailRepository.findByOfferCodeId(track.getOfferCodeId());
				if (offer != null && offer.getOfferDuration().getDurationInMonths() == track.getOfferDuration()) {
					track.setIsAvailedIntroOffer(true);
					iUserTrackingRepository.save(track);
				} else {
					offerCode=offer;
					track.setOfferDuration(track.getOfferDuration()+1);
					track.setIsAvailedIntroOffer(false);
					iUserTrackingRepository.save(track);
				}
			}
		} else {
			//
			if (Boolean.TRUE.equals(isTrail)) {
				// Fetch 'Current' Free mode offer for New user
				List<DiscountOfferMapping> discounts = disOfferMappingRepository
						.findByProgramsProgramIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailOfferModeAndOfferCodeDetailOfferStatus(
								programId, true, DiscountsConstants.MODE_FREE,
								DiscountsConstants.OFFER_ACTIVE);
				if (!discounts.isEmpty()) {
					Date today = new Date();
					for (DiscountOfferMapping discount : discounts) {
						if ((discount.getOfferCodeDetail().getOfferStartingDate().equals(today) || discount.getOfferCodeDetail().getOfferStartingDate().before(today)) 
								&& (discount.getOfferCodeDetail().getOfferEndingDate().equals(today) || discount.getOfferCodeDetail().getOfferEndingDate().after(today))) {
							offerCode = discount.getOfferCodeDetail();
						}
					}
				}
			}
			//
			if (offerCode == null && Boolean.TRUE.equals(isIntro)) {
				// Fetch 'Current' pay-as-you-go mode offer for New user
				List<DiscountOfferMapping> discounts = disOfferMappingRepository
						.findByProgramsProgramIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailOfferModeAndOfferCodeDetailOfferStatus(programId, true,
								DiscountsConstants.MODE_PAY_AS_YOU_GO,
								DiscountsConstants.OFFER_ACTIVE);
				if (!discounts.isEmpty()) {
					Date today = new Date();
					for (DiscountOfferMapping discount : discounts) {
						if ((discount.getOfferCodeDetail().getOfferStartingDate().equals(today) || discount.getOfferCodeDetail().getOfferStartingDate().before(today)) 
								&& (discount.getOfferCodeDetail().getOfferEndingDate().equals(today) || discount.getOfferCodeDetail().getOfferEndingDate().after(today))) {
							offerCode = discount.getOfferCodeDetail();
						}
					}
				}
			}
			//
			if (offerCode != null) {
				IntroOfferUserTracking intro = new IntroOfferUserTracking();
				intro.setProgramId(programId);
				intro.setUserId(userId);
				intro.setIsAvailedIntroOffer(false); // set true once reached the offer duration count
				intro.setOfferCodeId(offerCode.getOfferCodeId());
				intro.setOfferDuration(1);
				iUserTrackingRepository.save(intro);
			}

		} // track else part
		return offerCode;
	}

	@Transactional
    private IosReceiptInfo updSubscriptionViaVerifyReceipt(JSONObject latestReceipt, JSONObject pendingRenewal, String latestEncodedReceipt) {
        String expirantIntent;
		IosReceiptInfo iosReceiptInfo = new IosReceiptInfo();
        try {
            if (latestReceipt != null) {
                if(latestEncodedReceipt!=null) {
                    iosReceiptInfo.setReceiptData(latestEncodedReceipt);
                }

                if (latestReceipt.has(NotificationConstants.PRODUCT_ID)) {
                    iosReceiptInfo.setProgramId(
                            Long.valueOf(programFormat(latestReceipt.getString(NotificationConstants.PRODUCT_ID))));
                }
                if (latestReceipt.has(NotificationConstants.TRANSACTION_ID)) {
                    iosReceiptInfo.setTransactionId(latestReceipt.getString(NotificationConstants.TRANSACTION_ID));
                }
                if (latestReceipt.has(NotificationConstants.ORIGINAL_TRANSACTION_ID)) {
                    iosReceiptInfo.setOriginalTransactionId(
                            latestReceipt.getString(NotificationConstants.ORIGINAL_TRANSACTION_ID));
                }
                if (latestReceipt.has(NotificationConstants.SUBSCRIPTION_ID)) {
                    iosReceiptInfo
                            .setWeborderLineitemId(latestReceipt.getString(NotificationConstants.SUBSCRIPTION_ID));
                }
                if (latestReceipt.has(NotificationConstants.PURCHASE_DT)) {
                    iosReceiptInfo
                            .setPurchaseDate(dateFormat(latestReceipt.getString(NotificationConstants.PURCHASE_DT)));
                }
                if (latestReceipt.has(NotificationConstants.EXPIRES_DT)) {
                    iosReceiptInfo
                            .setExpiresDate(dateFormat(latestReceipt.getString(NotificationConstants.EXPIRES_DT)));
                }

                if (latestReceipt.has(NotificationConstants.EXPIRES_DT_PST)) {
                    iosReceiptInfo
                            .setExpiresDate(dateFormat(latestReceipt.getString(NotificationConstants.EXPIRES_DT_PST)));
                }

                if (latestReceipt.has(NotificationConstants.CANCEL_DT)) {
                    iosReceiptInfo
                            .setCancellationDate(dateFormat(latestReceipt.getString(NotificationConstants.CANCEL_DT)));
                }
                if (latestReceipt.has(NotificationConstants.CANCEL_REASON)) {
                    if (latestReceipt.getString(NotificationConstants.CANCEL_REASON).equalsIgnoreCase("0")) {
                        iosReceiptInfo.setMessage(NotificationConstants.CANCEL_0);
                    } else if (latestReceipt.getString(NotificationConstants.CANCEL_REASON).equalsIgnoreCase("1")) {
                        iosReceiptInfo.setMessage(NotificationConstants.CANCEL_1);
                    }
                }
                if (latestReceipt.has(NotificationConstants.IS_TRIAL)) {
                    iosReceiptInfo
                            .setIsTrialPeriod(Boolean.valueOf(latestReceipt.getString(NotificationConstants.IS_TRIAL)));
                }
                if (latestReceipt.has(NotificationConstants.IS_IN_INTRO_OFFER)) {
                    iosReceiptInfo
                            .setIsInIntroOffer(Boolean.valueOf(latestReceipt.getString(NotificationConstants.IS_IN_INTRO_OFFER)));
                }               

                if (pendingRenewal != null) {
                    if (pendingRenewal.has(NotificationConstants.IS_BILLING_RETRY)) {
                        iosReceiptInfo.setBillingRetry(
                                Boolean.valueOf(pendingRenewal.getString(NotificationConstants.IS_BILLING_RETRY)));
                    }
                    if (pendingRenewal.has(NotificationConstants.EXPIRANT_INTENT)) {
                        expirantIntent = pendingRenewal.getString(NotificationConstants.EXPIRANT_INTENT);
                        iosReceiptInfo.setExpirationIntent(expirantIntent);
                        if (!expirantIntent.isEmpty()) {
                            iosReceiptInfo.setMessage(expirationStatus(expirantIntent));
                        }
                    }
                    if (pendingRenewal.has(NotificationConstants.AUTO_RENEW)) {
                        String renew = pendingRenewal.getString(NotificationConstants.AUTO_RENEW);

                        if(renew !=null && renew.equalsIgnoreCase("1")) {
                            iosReceiptInfo.setAutoRenewStatus(true);
                        }else if(renew !=null && renew.equalsIgnoreCase("0")){
                            iosReceiptInfo.setAutoRenewStatus(false);
                        }
                    }
                }
                // Offer Id
    			if (latestReceipt.has(NotificationConstants.OFFER_ID)) {
    				iosReceiptInfo.setOfferId(latestReceipt.getString(NotificationConstants.OFFER_ID));
    			}
    			// Offer Name
    			if (latestReceipt.has(NotificationConstants.OFFER_NAME)) {
    				iosReceiptInfo.setOfferName(latestReceipt.getString(NotificationConstants.OFFER_NAME));
    			}
                iosReceiptInfo.setLatestReceiptData(latestReceipt.toString());
                iosReceiptInfo.setNotificationType("Auto Renewal by Program");
            }          	
            //
            iReceiptInfoRepository.save(iosReceiptInfo);

            AppleSubscriptionStatus appSubscriptionStatus = findSubcriptionStatus(NotificationConstants.RENEWAL,iosReceiptInfo);
            if (appSubscriptionStatus != null) {
                iosReceiptInfo.setSubscriptionStatus(appSubscriptionStatus);
            }
            iosReceiptInfo = iReceiptInfoRepository.save(iosReceiptInfo);
        } catch (Exception e) {
            throw new ApplicationException(Constants.ERROR_STATUS,
                    "Exception in updateSubscriptionViaVerifyReceipt Method.", e.getMessage());
        }
        return iosReceiptInfo;
    }

    private String receiptStatus(int status) {
        String msg = "";
        try {
            switch (status) {
                case 21000:
                    msg = "The App Store could not read the JSON object you provided";
                    log.info("\n  21000 : The App Store could not read the JSON object you provided. ");

                    break;
                case 21002:
                    msg = "The data in the receipt-data property was malformed.";
                    log.info("\n  21002 : The data in the receipt-data property was malformed..   ");
                    break;
                case 21003:
                    msg = "The data in the receipt-data property was malformed.";
                    log.info("\n  21003 : The receipt could not be authenticated. ");
                    break;
                case 21004:
                    msg = "The shared secret you provided does not match the shared secret on file for your account.";
                    log.info(
                            "\n  21004 : The shared secret you provided does not match the shared secret on file for your account. ");
                    break;
                case 21005:
                    msg = "The receipt server is not currently available.";
                    log.info("\n  21005 : The receipt server is not currently available. ");
                    break;
                case 21006:
                    msg = "This receipt is valid but the subscription has expired. When this status code is returned to your server, the receipt data is also decoded and returned as part of the response.";
                    log.info(
                            "\n  21006 : This receipt is valid but the subscription has expired. When this status code is returned to your server, the receipt data is also decoded and returned as part of the response. ");
                    break;
                case 21007:
                    msg = "This receipt is a sandbox receipt, but it was sent to the production service for verification.";
                    log.info(
                            "\n  21007 : This receipt is a sandbox receipt, but it was sent to the production service for verification. ");
                    break;
                case 21008:
                    msg = "This receipt is a production receipt, but it was sent to the sandbox service for verification.";
                    log.info(
                            "\n  21008 : This receipt is a production receipt, but it was sent to the sandbox service for verification. ");
                    break;

                default:
                    msg = "Active subscription.";
                    log.info("\n  0 : valid ....Active subscription. ");
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return msg;
    }

    /**
     * Capture user info while Purchasing new product.This API will be called from app side.
     * Entire subscription related data is populating here.
     * @param requestView
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Transactional
    public ResponseModel captureNewBuyDetails(InitialPurchaseRequestView requestView) throws IOException {
        log.info(" ------------------- Method :: Capture InitialPurchase Details  -------------------");
		log.info("Capture new buy details starts.");
		long apiStartTimeMillis = new Date().getTime();
        String receiptURL=appleProperties.getVerifyReceiptEndPoint();
        String appSecretKey=appleProperties.getAppSecretKey();
        CloseableHttpResponse response;
        InitialPurchaseResponseView iResponseView = new InitialPurchaseResponseView();
        IosReceiptInfo iosReceipt=new IosReceiptInfo();
        //Request view
        String txnID = requestView.getTransactionId();
        String pgmID = requestView.getProgramId();
        User user=userComponents.getUser();
        String exstOrderId = requestView.getOrderId();
        String errMsg=requestView.getErrMsg();
        //
        String programid;
        String weborderlineitem;
        Boolean autorenew = false;
        String transactionId;
        String orgTxnId="";
        String message = "";
        Date purchaseDt;
        OrderManagement existingOrd = null;
        OrderManagement newOrder = null;
        long responseCode=Constants.SUCCESS_STATUS;
        String eventType;
        JSONObject payload = null;
        String orderid;
        boolean isTrial=false;
        boolean isInIntroOffer=false;
		log.info("Get user and apple properties : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
		long profilingEndTimeMillis = new Date().getTime();

        // As we introduce Common Subscription URL to receive Apple's Server to Sever Notifications.
        // Product Identifier check has been introduced here.
        String productId=findProductIdetifier();
        if (requestView.getProgramId().contains(productId)) {
            Programs program = programsRepo.findByProgramId(Long.parseLong(programFormat(requestView.getProgramId())));
            String programStatus = program.getStatus();
            String programTitle = program.getTitle();
			log.info("Query: getting program from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
			profilingEndTimeMillis = new Date().getTime();

            // Also check Program status.If not in 'Publish' status we should stop receiving subscription info(INTERACTIVE_RENEWAL).
            if(programStatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) {
                if (!exstOrderId.isEmpty()) {
                    existingOrd = orderManagementRepo.findTop1ByOrderIdOrderByCreatedDateDesc(requestView.getOrderId());
                    if (existingOrd == null) {
                        throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_INCORRECT_ORDER_ID,null);
                    }
                }
				log.info("Query: getting order management from DB : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
				profilingEndTimeMillis = new Date().getTime();
                if (requestView.getErrMsg().isEmpty()) {
                    errMsg = NotificationConstants.ORD_RETRY;
                }
                if (requestView.getReceiptdata().isEmpty()) {
                    if (existingOrd != null) {
                        message = errMsg + existingOrd.getOrderId();
                        iResponseView.setOrderId(existingOrd.getOrderId());
                        addPaymentInfo(null, existingOrd, NotificationConstants.DID_FAIL_TO_RENEW, message);
                        responseCode = Constants.ERROR_STATUS;
                    } else {
                        OrderManagement order = createOrderManagement(program, null, null, "", KeyConstants.KEY_FAILURE,null);
                        message = errMsg + order.getOrderId();
                        iResponseView.setOrderId(order.getOrderId());
                        addPaymentInfo(null, order, NotificationConstants.DID_FAIL_TO_RENEW, message);
                        responseCode = Constants.ERROR_STATUS;
                    }
                } else if (!requestView.getTransactionId().isEmpty() && !requestView.getReceiptdata().isEmpty()) {
                    try (CloseableHttpClient client = HttpClients.createDefault()) {
                        JSONObject requestData = new JSONObject();
                        requestData.put(NotificationConstants.RECIEPT_DATA, requestView.getReceiptdata());
                        requestData.put(NotificationConstants.RECIEPT_PD, appSecretKey);
                        requestData.put(NotificationConstants.EXCL_OLD_TXN, false);
                        //
                        HttpPost httpPost = new HttpPost(receiptURL);
                        StringEntity entity = new StringEntity(requestData.toString());
                        httpPost.setEntity(entity);
                        httpPost.setHeader(NotificationConstants.CONT_TYP, NotificationConstants.JSON);
                        response = client.execute(httpPost);

                        String json = EntityUtils.toString(response.getEntity());
                        payload = new JSONObject(json);
                    }
				}
				log.info("Creating order and apple payment : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
				profilingEndTimeMillis = new Date().getTime();
                log.info(" Transaction Id :{}" + requestView.getTransactionId());
                log.info(" User Id : {} ", user.getUserId());
                log.info(" Program Id : {} ", requestView.getProgramId());
                JSONArray latestReceipt;
                JSONObject entry=null;
                boolean dataAvail=false;
                if (payload != null && payload.get(NotificationConstants.RCPT_ST).toString().equalsIgnoreCase("0")) {
                		latestReceipt = payload.getJSONArray(NotificationConstants.LATEST_RCPT);
                		 for (int i = 0; i < latestReceipt.length(); i++) {
                			 if (latestReceipt.getJSONObject(i).getString(NotificationConstants.TRANSACTION_ID).trim().equalsIgnoreCase(txnID.trim()) 
                					 && latestReceipt.getJSONObject(i).getString(NotificationConstants.PRODUCT_ID).trim().equalsIgnoreCase(pgmID.trim())) {
                				 dataAvail=true;
                				 entry = latestReceipt.getJSONObject(i);
                			 }
                    	 }
                		 // Fetch in_app array
                		 if(Boolean.FALSE.equals(dataAvail)) {
                         	JSONObject receipt = payload.getJSONObject(NotificationConstants.RECEIPT);
                             JSONArray inAppArray = receipt.getJSONArray(NotificationConstants.IN_APP_ARRAY);
                             for (int i = 0; i < inAppArray.length(); i++) {
                                 if (inAppArray.getJSONObject(i).getString(NotificationConstants.TRANSACTION_ID).trim().equalsIgnoreCase(txnID.trim()) 
                                 		&& inAppArray.getJSONObject(i).getString(NotificationConstants.PRODUCT_ID).trim().equalsIgnoreCase(pgmID.trim())) {
                                 	entry = inAppArray.getJSONObject(i);
         						}
         					}
         				} 
                		 //
                		 if(entry!=null && entry.has(NotificationConstants.ORIGINAL_TRANSACTION_ID)) {
                			 orgTxnId = entry.getString(NotificationConstants.ORIGINAL_TRANSACTION_ID);
                		 }
                		 JSONArray pendingRenewal = payload.getJSONArray(NotificationConstants.PENDING_RENEWAL_INFO);
                         log.info("pendingRenewal lenth :: {}", pendingRenewal.length());
                         log.info(" orgTxnId :{}" , orgTxnId);
					for (int i = 0; i < pendingRenewal.length(); i++) {
						if (pendingRenewal.getJSONObject(i).getString(NotificationConstants.ORIGINAL_TRANSACTION_ID).trim().equalsIgnoreCase(orgTxnId.trim())
								&& pendingRenewal.getJSONObject(i).getString(NotificationConstants.PRODUCT_ID).trim().equalsIgnoreCase(pgmID.trim())) {
							JSONObject pendingRenewalObject = pendingRenewal.getJSONObject(i);
							log.info("pendingRenewalObject ::{}", pendingRenewalObject);
							String renew = pendingRenewalObject.getString(NotificationConstants.AUTO_RENEW);
							if (renew.equalsIgnoreCase("1")) {
								autorenew = true;
							} else if (renew.equalsIgnoreCase("0")) {
								autorenew = false;
							}
							iResponseView.setIsAutorenew(String.valueOf(autorenew));
							iosReceipt.setAutoRenewStatus(autorenew);
						}
					}
				}
				log.info("Getting transactions : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
				profilingEndTimeMillis = new Date().getTime();
                
                //------------------------------------------------------------------------------------------------------------
                if(entry!=null) {
                	programid = programFormat(entry.getString(NotificationConstants.PRODUCT_ID));
                	iResponseView.setProductId(programid);
                    weborderlineitem = entry.getString(NotificationConstants.SUBSCRIPTION_ID);
                   
                    purchaseDt = dateFormat(entry.getString(NotificationConstants.PURCHASE_DT));
                    iResponseView.setPurchaseDate(purchaseDt);
                    iResponseView.setPurchaseDateFormatted(fitwiseUtils.formatDate(purchaseDt));
                    if (entry.getString(NotificationConstants.EXPIRES_DT_PST) != null) {
                        iResponseView
                                .setExpiresDate(dateFormat(entry.getString(NotificationConstants.EXPIRES_DT_PST)));
                        iResponseView.setExpiresDateFormatted(fitwiseUtils.formatDateWithTime(
                                dateFormat(entry.getString(NotificationConstants.EXPIRES_DT_PST))));
                    } else if (entry.getString(NotificationConstants.EXPIRES_DT) != null) {
                        iResponseView.setExpiresDate(dateFormat(entry.getString(NotificationConstants.EXPIRES_DT)));
                        iResponseView.setExpiresDateFormatted(fitwiseUtils
                                .formatDateWithTime(dateFormat(entry.getString(NotificationConstants.EXPIRES_DT))));
                    }
                    transactionId = entry.getString(NotificationConstants.TRANSACTION_ID);
                    iResponseView.setTransactionId(transactionId);
                    orgTxnId = entry.getString(NotificationConstants.ORIGINAL_TRANSACTION_ID);
                    iResponseView.setOriginalTransactionId(orgTxnId);
                    iResponseView.setWebOrderLineitemId(weborderlineitem);
                    String isTrailPeriod = entry.getString(NotificationConstants.IS_TRIAL);
                    if (isTrailPeriod != null && isTrailPeriod.equalsIgnoreCase("true")) {
                        isTrial = true;
                    }
                    iosReceipt.setIsTrialPeriod(isTrial);
                    if(entry.has(NotificationConstants.IS_IN_INTRO_OFFER)) {
                    	String isIntroOffer = entry.getString(NotificationConstants.IS_IN_INTRO_OFFER);
                    	if (isIntroOffer != null && isIntroOffer.equalsIgnoreCase("true")) {
                        	isInIntroOffer = true;
                        }
                    }
                    iosReceipt.setIsInIntroOffer(isInIntroOffer);
                    //Offer Id
                    if(entry.has(NotificationConstants.OFFER_ID)) {
                    	iosReceipt.setOfferId(entry.getString(NotificationConstants.OFFER_ID));
                    }
                    //Offer Name
                    if(entry.has(NotificationConstants.OFFER_NAME)) {
                    	iosReceipt.setOfferName(entry.getString(NotificationConstants.OFFER_NAME));
                    }

                    // Populate Pojo for Payment Entry
                    iosReceipt.setTransactionId(transactionId);
                    iosReceipt.setWeborderLineitemId(weborderlineitem);
                    iosReceipt.setOriginalTransactionId(orgTxnId);
                    iosReceipt.setPurchaseDate(purchaseDt);
                    if (entry.getString(NotificationConstants.EXPIRES_DT_PST) != null) {
                        iosReceipt
                                .setExpiresDate(dateFormat(entry.getString(NotificationConstants.EXPIRES_DT_PST)));
                    } else if (entry.getString(NotificationConstants.EXPIRES_DT) != null) {
                        iosReceipt.setExpiresDate(dateFormat(entry.getString(NotificationConstants.EXPIRES_DT)));
                    }

                    iosReceipt.setMessage(message);
                    message = receiptStatus(Integer.parseInt(payload.get(NotificationConstants.RCPT_ST).toString()));
                    iosReceipt.setStatus(payload.get(NotificationConstants.RCPT_ST).toString());
             
                    log.info("Purchase entry ::{}", entry);
                    

                    // * New Purchase:Apple will be sending 2 notifications.(Same Transaction Id )
                    // One is for purchase and another for Auto Renew Flag set (INITIAL_BUY & DID_CHANGE_RENEWAL_STATUS (ON))
                    // * After Some break user buys a same program. Apple will be sending 2 notifications.(Same Transaction Id )
                    // One is for Auto Renew Flag set and another for purchase (DID_CHANGE_RENEWAL_STATUS (ON) & INTERACTIVE_RENEWAL)
                   //txn start
                    if (!programid.isEmpty()) {

                        boolean isAlreadyRenewed = true;
                        // Check whether auto renew already happened via scheduler job.Because if the app is open 'renewal' will triggered via 'INTERACTIVE_RENEWAL' event.
                        // By using this check we can avoid 2 different orders for same transaction id.

                        AppleProductSubscription applePrSubscription = aRepository
                                .findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(
                                        iosReceipt.getTransactionId(), iosReceipt.getOriginalTransactionId());
                        if (applePrSubscription != null) {
                            if (applePrSubscription.getEvent() != null
                                    && (applePrSubscription.getEvent().equalsIgnoreCase(NotificationConstants.REN_ON))) {
                                // allow entry to populate data's
                                isAlreadyRenewed = false;
                            } else if (applePrSubscription.getEvent() != null
                                    && (applePrSubscription.getEvent().equalsIgnoreCase(NotificationConstants.RENEWAL)
                                    || applePrSubscription.getEvent()
                                    .equalsIgnoreCase(NotificationConstants.INTERACTIVE_RENEWAL))
                                    || applePrSubscription.getEvent().equalsIgnoreCase(NotificationConstants.REN_OFF)) {
                                // not allow
							}
                        } else {
                            // allow entry to populate data's (new Purchase)
                            isAlreadyRenewed = false;
                        }
                        if (!isAlreadyRenewed) {
                        	// Offers                        	
                        	//Mapping offers with Orders
							OfferCodeDetail offerCode = null;
							if (iosReceipt.getOfferId() != null) {
								String promoOfferId = iosReceipt.getOfferId();
								if (!StringUtils.isEmpty(promoOfferId)) {
									offerCode = offerCodeDetailRepository.findByOfferCodeAndIsInUse(promoOfferId, true);
								}
							} else {
								offerCode=findIntroOffer(Long.valueOf(programid), user.getUserId(), iosReceipt.getIsTrialPeriod(), iosReceipt.getIsInIntroOffer());
							}//else part
                        	
                        	// Program Subscription
                            ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(),program.getProgramId());
                            if (subscribedProgram != null) {
                                eventType = NotificationConstants.INTERACTIVE_RENEWAL;
                            } else {
                                eventType = NotificationConstants.INITIAL_BUY;
                            }
                            AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
                            saveAppleProgramSubscription(userComponents.getUser(), program, transactionId, weborderlineitem,orgTxnId, appleSubscriptionStatus, eventType);
							VerifyReceipt verifyReceipt = verifyReceiptRepository.findTop1ByOriginalTxnIdAndProgramProgramIdAndUserUserIdOrderByIdDesc(iosReceipt.getOriginalTransactionId(), program.getProgramId(),user.getUserId());
                            if (verifyReceipt != null) {
                                verifyReceipt.setReceiptData(requestView.getReceiptdata());
                                verifyReceiptRepository.save(verifyReceipt);
                            } else {
                                saveEncodedLatestReceipt(requestView.getReceiptdata(), program, orgTxnId, programTitle,user);
                            }
                            log.info("autorenew =====>"+autorenew);
                            if (existingOrd != null) {
                                newOrder = createOrderManagement(program, autorenew, user, existingOrd.getOrderId(),KeyConstants.KEY_PROCESSING,offerCode);
                                orderid = newOrder.getOrderId();
                                iResponseView.setOrderId(existingOrd.getOrderId());
                                log.info("Apple Existing ord id >> "+orderid);
                            } else {
                                newOrder = createOrderManagement(program, autorenew, user, "", KeyConstants.KEY_PROCESSING,offerCode);
                                orderid = newOrder.getOrderId();
                                iResponseView.setOrderId(newOrder.getOrderId());
                                log.info(" Apple Order id >> "+orderid);
                            }
                            // Direct Logic: Populate Payments & Subscription info by using receipt data
                            // instead of waiting for Server Notification.

                            if (subscribedProgram != null) {
                                log.info(" ********* INTERACTIVE_RENEWAL *********************");
                                updateSubscribedPgm(user, subscribedProgram, true, autorenew, purchaseDt,
                                        NotificationConstants.INTERACTIVE_RENEWAL,newOrder);
                            } else {
                                log.info(" ********* INITIAL_BUY *********************");
                                addNewSubscribedProgram(user, program, true, autorenew, purchaseDt, isTrial,newOrder);
                            }
                            addPaymentInfo(iosReceipt, newOrder, eventType, "");
							updateOrderStatus(orderid,KeyConstants.KEY_SUCCESS);
                            //
                            responseCode = Constants.SUCCESS_STATUS;
                        }
                    }
                    //txn end
                } else {
                    message = receiptStatus(Integer.parseInt(payload.get(NotificationConstants.RCPT_ST).toString()));
                }
				log.info("Apple program subscription : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
				profilingEndTimeMillis = new Date().getTime();
            }
			try{
				//Sending mail to member
				String subject = EmailConstants.PROGRAM_SUBSCRIPTION_SUBJECT.replace(EmailConstants.PROGRAM_TITLE,  program.getTitle() );
				String mailBody = EmailConstants.PROGRAM_SUBSCRIPTION_CONTENT.replace(EmailConstants.PROGRAM_TITLE, "<b>" + program.getTitle() + "</b>");
				String userName = fitwiseUtils.getUserFullName(user);
				User instructor = program.getOwner();
				String memberProgram = EmailConstants.MEMBER_PROGRAM_LINK.replace(EmailConstants.LITERAL_APP_URL, dynamicLinkService.constructProgramLinkForMember(program.getProgramId(),instructor));
				mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",").replace(EmailConstants.EMAIL_BODY, mailBody)
						.replace(EmailConstants.EMAIL_SUPPORT_URL, memberProgram);
				mailBody = emailContentUtil.replaceMemberAppUrl(mailBody);

				try {
					if(newOrder != null){
						InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(newOrder);
						if(invoiceManagement != null){
							File file = invoicePDFGenerationService.generateInvoicePdf(newOrder.getOrderId(), StringConstants.INVOICE + invoiceManagement.getInvoiceNumber());
							List<String> fileList = Collections.singletonList(file.getAbsolutePath());
							asyncMailer.sendHtmlMailWithAttachment(user.getEmail(),null, subject, mailBody, fileList);
						}

					}

				} catch (Exception e) {
					if(newOrder != null){
						log.error("Invoice PDF generation failed for subscription mail. Order id : " + newOrder.getOrderId());
					}
					log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
					asyncMailer.sendHtmlMail(user.getEmail(), subject, mailBody);
				}

				/*
				 * Stripe connect onboarding reminder mail
				 * */
				boolean isOnboardingDetailsSubmitted = false;
				StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
				if(stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsDetailsSubmitted() != null && stripeAccountAndUserMapping.getIsDetailsSubmitted().booleanValue()){
					isOnboardingDetailsSubmitted = true;
				}
				boolean isOnBoardedViaPayPal = false;
				UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
				if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
					isOnBoardedViaPayPal = true;
				}
				if (!isOnboardingDetailsSubmitted && !isOnBoardedViaPayPal) {
					userName = fitwiseUtils.getUserFullName(instructor);
					subject = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_SUBJECT;
					String trainnrDashboard = EmailConstants.STRIPE_CONNECT_ONBOARD_DASHBOARD_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl() + RestUrlConstants.APP_INSTRUCTOR_DASHBOARD);
					mailBody = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_CONTENT;
					mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
							.replace(EmailConstants.EMAIL_BODY, mailBody)
							.replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrDashboard);
					mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
					asyncMailer.sendHtmlMail(instructor.getEmail(), subject, mailBody);
				}
			}catch(Exception e){
				log.error(MessageConstants.MSG_ERR_EXCEPTION + e.getMessage());
			}
			log.info("Sending mail to member : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        }
		log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
		log.info("Capture new buy details ends.");
        return new ResponseModel(responseCode, message, iResponseView);
    }

    private String findProductIdetifier() {
        String pdtIdetifier="";
        try {
            AppConfigKeyValue appCKeyValue=appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
            pdtIdetifier=appCKeyValue.getValueString();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return pdtIdetifier;
    }

    @Transactional
    private void updateOrderStatus(String orderid, String status) {
        log.info("---------- Method:: updateOrderStatus --------------");
        try {
            OrderManagement updatedOrder=orderManagementRepo.findTop1ByOrderIdOrderByCreatedDateDesc(orderid);
            log.info("updatedOrder  ---------->"+updatedOrder.getOrderId());
            updatedOrder.setOrderStatus(status);
            OrderManagement order=orderManagementRepo.save(updatedOrder);
            InvoiceManagement invoiceManagement = invoiceManagementRepository.findByOrderManagement(order);
            fitwiseQboEntityService.createAndSyncQboInvoice(invoiceManagement);
            log.info("Order Status Saved successfully :{}",order.getOrderStatus());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Save Program Subscription status for user
     * @param appleSubscriptionStatus  */
    @Transactional
    private void saveAppleProgramSubscription(User user,Programs program, String transactionId, String weborderlineitem,String orgTxnId, AppleSubscriptionStatus appleSubscriptionStatus,String event) {
        log.info("---------- Method:: saveAppleProgramSubscription --------------");
        try {
            PlatformType platformType = platformsRepo.findByPlatformTypeId(NotificationConstants.APPLE_PLATFORM);
            AppleProductSubscription aSubscription=new AppleProductSubscription();
            aSubscription.setUser(user);
            aSubscription.setProgram(program);
            aSubscription.setTransactionId(transactionId);
            aSubscription.setAppleSubscriptionId(weborderlineitem);
            aSubscription.setOriginalTransactionId(orgTxnId);
            aSubscription.setSubscribedViaPlatform(platformType);
            aSubscription.setAppleSubscriptionStatus(appleSubscriptionStatus);
            aSubscription.setEvent(event);
            aRepository.save(aSubscription);
        }catch(Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Exception in saveAppleProgramSubscription",e.getMessage());
        }

    }

    /**
     * Store Encoded Latest Receipt for successful payment entry while initial purchase.
     */
    @Transactional
    private void saveEncodedLatestReceipt(String receiptdata, Programs program, String originalTxnID,String programTitle,User user) {
        log.info("---------- Method:: saveEncodedLatestReceipt --------------");
        VerifyReceipt verifyReceipt = new VerifyReceipt();
        try {
            log.info("User id :{} ",user.getUserId());
            log.info("Program Id :{} ",program.getProgramId());
            log.info("OrgTxnId :{}",originalTxnID);

            verifyReceipt.setUser(user);
            verifyReceipt.setProgram(program);
            verifyReceipt.setOriginalTxnId(originalTxnID);
            verifyReceipt.setProgramName(programTitle);
            verifyReceipt.setReceiptData(receiptdata);
            verifyReceiptRepository.save(verifyReceipt);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Exception in saveEncodedLatestReceipt",e.getMessage());
        }
    }

    public ResponseModel serverNotification(Object webHookView) {
        log.info("--------------------Method:: ServerToServer Notification -----------------------------");
        long startTimeInMillis = new Date().getTime();
        long profilingStartTimeInMillis;
        long profilingEndTimeInMillis;
        //System.out.println("webHookView >>>>>>>>>>>>> "+webHookView);
        String programId;
        profilingStartTimeInMillis = new Date().getTime();
        Gson gson = new Gson();
        String json = gson.toJson(webHookView);
        JSONObject jsonObject = new JSONObject(json);
        profilingEndTimeInMillis = new Date().getTime();
        log.info("webhookview to json conversion : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
        //
        if (jsonObject.has(NotificationConstants.AUTO_RENEW_PDT)) {
        	profilingStartTimeInMillis = new Date().getTime();
            String programIdentifier = findProductIdetifier();
            profilingEndTimeInMillis = new Date().getTime();
			log.info("Getting product identifier : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
			programId=jsonObject.getString(NotificationConstants.AUTO_RENEW_PDT);
            log.info("Notification Program ID ::{}",programId);
            if (programId.contains(programIdentifier)) {
            	profilingStartTimeInMillis = new Date().getTime();
                serverNotificationBasedOnNotificationType(jsonObject);
                profilingEndTimeInMillis = new Date().getTime();
				log.info("subscription update based on notification type : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
			}
            else{
            	profilingStartTimeInMillis = new Date().getTime();
                redirectNotificationObject(programId,jsonObject);
                profilingEndTimeInMillis = new Date().getTime();
				log.info("Redirecting notification based on environment : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
			}
        }
        profilingEndTimeInMillis = new Date().getTime();
		log.info("Server notification api : total time taken in milliseconds : " +(profilingEndTimeInMillis-startTimeInMillis));
		return new ResponseModel();
    }

    private void redirectNotificationObject(String programId, JSONObject notification) {
    	log.info(" ********** redirectNotificationObject from Apple *****************");
        String receiptURL = "";
        String value =NotificationConstants.VALUE_ENDPOINT;
        try {
            if (programId.contains(NotificationConstants.DEV_PDT)) {
                receiptURL=appleProperties.notificationToDev+value;
            } else if (programId.contains(NotificationConstants.QA_PDT)) {
                receiptURL=appleProperties.notificationToQa+value;
            } else if (programId.contains(NotificationConstants.STG_PDT)) {
                receiptURL=appleProperties.notificationToDev+value;
            } else if (programId.contains(NotificationConstants.PRD_PDT)) {
                receiptURL=appleProperties.notificationToPrd+value;
            }
            log.info("receiptURL **************************** "+receiptURL);
            Map< String, Object >jsonValues = new HashMap<>();

            jsonValues.put("notification", notification);
            JSONObject json = new JSONObject(jsonValues);

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(receiptURL);
            StringEntity entity = new StringEntity(json.toString(), "UTF8");
            httpPost.setEntity(entity);
            httpPost.setHeader(NotificationConstants.CONT_TYP, NotificationConstants.JSON);
            HttpResponse response = client.execute(httpPost);
            log.info("Response: " + response.getStatusLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverNotificationBasedOnNotificationType(JSONObject jsonObject) {
        log.info("-------------Method: serverNotificationBasedOnNotificationType ----------");
        String eventType ="";
		log.info("Server notification based on notification type starts.");
        long apiStartTime = new Date().getTime();
        long profilingStartTimeInMillis = new Date().getTime();
        long profilingEndTimeInMillis;
        if(jsonObject.has(NotificationConstants.NOTIFICATION)) {
            eventType = jsonObject.getString(NotificationConstants.NOTIFICATION);
        }
        profilingEndTimeInMillis = new Date().getTime();
		log.info("Getting notification event type : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
		log.info(" serverNotification Type ----------- {}", eventType);
        if (eventType.equalsIgnoreCase(NotificationConstants.INITIAL_BUY)) {
        	profilingStartTimeInMillis = new Date().getTime();
            newSubscription(eventType, jsonObject);
            profilingEndTimeInMillis = new Date().getTime();
			log.info("Adding new subscription : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
		}
        /**
         * DID_RECOVER or RENEWAL :Expired subscription recovered by App Store through a billing retry. RENEWAL: Deprecated
         * INTERACTIVE_RENEWAL : Customer upgrades their subscription.
         * CANCEL :User cancels their Auto Renewal subscription through Apple customer care support.
         * DID_FAIL_TO_RENEW :Subscription fails auto renew at a first attempt to renew in a subscription period.
         * 					  Indicates a subscription that failed to renew due to a billing issue.
         * DID_CHANGE_RENEWAL_STATUS :  when the user turns on/off their Auto Renewal subscription through manage subscription settings
         * 						or Customer down grades their subscription No need of capturing Subscription,Audit and Payment informations.
         */
        else if (eventType.equalsIgnoreCase(NotificationConstants.DID_RECOVER)
                || eventType.equalsIgnoreCase(NotificationConstants.INTERACTIVE_RENEWAL)
                || eventType.equalsIgnoreCase(NotificationConstants.DID_FAIL_TO_RENEW)
                || eventType.equalsIgnoreCase(NotificationConstants.CANCEL)
                || eventType.equalsIgnoreCase(NotificationConstants.DID_CHANGE_RENEWAL_STATUS)
                || eventType.equalsIgnoreCase(NotificationConstants.DID_CHANGE_RENEWAL_PREF)
                || eventType.equalsIgnoreCase(NotificationConstants.REFUND)
                || eventType.equalsIgnoreCase(NotificationConstants.DID_RENEW)) {
        	profilingStartTimeInMillis = new Date().getTime();
            updateSubscription(eventType, jsonObject);
            profilingEndTimeInMillis = new Date().getTime();
			log.info("Update existing subscription : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

		}
		log.info("Adding new subscription : time taken in milliseconds : " +(new Date().getTime() - apiStartTime));
		log.info("Server notification based on notification type ends.");
    }


    /**
     * Program Subscription /Audit entry will be created for newly Purchased program.
     * Payment entry will be added.
     * @param eventType
     * @param notification
     */
    private void newSubscription(String eventType, JSONObject notification) {
        log.info(" ---------- Method : newSubscription ----------- {}",eventType);
        if (notification.has(NotificationConstants.AUTO_RENEW_PDT)) {
            String programIdentifier = findProductIdetifier();
            if (notification.getString(NotificationConstants.AUTO_RENEW_PDT).contains(programIdentifier)) {
                IosReceiptInfo info = saveEventChange(notification);
                log.info("eventType : {}",eventType);
                Long programId=info.getProgramId();
                String orginalTxnId=info.getOriginalTransactionId();
                String susbcriptionId=info.getWeborderLineitemId();
                String transactionId=info.getTransactionId();
				log.info("Program id : {}",programId);
                log.info("Org Txn Id : {}",orginalTxnId);
                log.info("Subcs Id : {}",susbcriptionId);
                log.info("Txn Id : {}",transactionId);
                //TODO: The following steps are not required when implementing Direct Logic in method captureNewBuyDetails.
                //All subscription and Payment related informations captured by validate receipt API during purchase event.
		/*
		// Find User info By using Transaction Id and Program
		VerifyReceipt receipt=verifyReceiptRepository.findByTransactionIdAndOriginalTxnId(transactionId,orginalTxnId);
		log.info("Rec User id : {}",receipt.getUser().getUserId());
		// Fetch Order
		OrderManagement orderManagement = orderManagementRepo.findTop1ByProgramProgramIdAndUserUserIdAndModeOfPaymentOrderByCreatedDateDesc(programId,receipt.getUser().getUserId(),NotificationConstants.MODE_OF_PAYMENT);//findTop1ByProgramAndUser
		log.info("Initial Buy OrderId : {}",orderManagement.getOrderId());
		// Populate Subscription Data
		addNewSubscribedProgram(receipt.getUser(), receipt.getProgram(), true, autoRenew,purchaseDate,isTrail);
		addPaymentInfo(info, orderManagement, eventType, "");
		// Apple Program Subscription
		AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
		saveAppleProgramSubscription(receipt.getUser(),receipt.getProgram(),transactionId,susbcriptionId,orginalTxnId,appleSubscriptionStatus);
		// Update Order Status
		orderManagement.setOrderStatus(KeyConstants.KEY_SUCCESS);
		orderManagementRepo.save(orderManagement);
		*/
            }
        }
    }

    /**
     * Update Program Subscription and New Audit entry will be created for Each subscription Renewal.
     * Payment entry will be added.
     * @param eventType
     * @param notification
     */
    @Transactional
    private void updateSubscription(String eventType, JSONObject notification) {
        log.info(" ---------- Method : updateSubscription -----------{}",eventType);
        long profilingStartTimeInMillis;
        long profilingEndTimeInMillis;
        try {
            if (notification.has(NotificationConstants.AUTO_RENEW_PDT)) {
                String programIdentifier = findProductIdetifier();
                if (notification.getString(NotificationConstants.AUTO_RENEW_PDT).contains(programIdentifier)) {

                	profilingStartTimeInMillis = new Date().getTime();
                    IosReceiptInfo info = saveEventChange(notification);
                    profilingEndTimeInMillis = new Date().getTime();
					log.info("Saving event change in apple subscription : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
					log.info("eventType : {}",eventType);
                    Long programId=info.getProgramId();
                    String orginalTxnId=info.getOriginalTransactionId();
                    String susbcriptionId=info.getWeborderLineitemId();
                    String transactionId=info.getTransactionId();
                    Boolean autoRenew=info.getAutoRenewStatus();
                    Boolean isBillingRetry=info.getBillingRetry();
                    String expiranIntent=info.getExpirationIntent();
                    User user=null;
                    //
                    log.info("Program id : {} {}",programId ,orginalTxnId);
                    log.info("Txn Id : {}",transactionId);
                    log.info("Purchase Date : {}",info.getPurchaseDate());
                    log.info("Expiry Date : {}",info.getExpiresDate());
                    log.info("Auto renew Flag : {}",autoRenew);
                    //Finding User
                    Programs program = programsRepo.findByProgramId(programId);
                    String programstatus=program.getStatus();
                    VerifyReceipt receipt = verifyReceiptRepository.findTop1ByProgramProgramIdAndOriginalTxnIdAndProgramNameOrderByIdDesc(programId, orginalTxnId,program.getTitle());
                    log.info("program Status :{}",programstatus);
                    //AppleProductSubscription appSubs=aRepository.findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(transactionId, orginalTxnId);
                    if(receipt!=null) {
                        user=receipt.getUser();
                        log.info("user :{}",user.getUserId());
                    }

                    //Fetch Offer                    
					OfferCodeDetail offerCode = null;
					if (info.getOfferId() != null) {
						String promoOfferId = info.getOfferId();
						if (!StringUtils.isEmpty(promoOfferId)) {
							offerCode = offerCodeDetailRepository.findByOfferCodeAndIsInUse(promoOfferId, true);
						}
					} else {
						if(user != null) {
							profilingStartTimeInMillis = new Date().getTime();
							offerCode=findIntroOffer(program.getProgramId(), user.getUserId(), info.getIsTrialPeriod(), info.getIsInIntroOffer());
							profilingEndTimeInMillis = new Date().getTime();
							log.info("Finding intro offer : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
						}
					}
                    // Events
					long start;
                    if (eventType.equalsIgnoreCase(NotificationConstants.DID_CHANGE_RENEWAL_STATUS)) {
                    	log.info("autoRenew Status from DID_CHANGE_RENEWAL_STATUS :{}",autoRenew);
                        if (Boolean.FALSE.equals(info.getAutoRenewStatus())) {//Auto Subscription OFF
							AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
                            //Check for Existing cancel Entry
                            User findUser=null;
                            AppleProductSubscription existSubs=aRepository.findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(transactionId, orginalTxnId);
                            if(existSubs!=null && !existSubs.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_CANCELLED)) {
                            	findUser=existSubs.getUser();
                            	profilingStartTimeInMillis = new Date().getTime();
                            	saveAppleProgramSubscription(findUser, program, transactionId, susbcriptionId,orginalTxnId, appleSubscriptionStatus,NotificationConstants.REN_OFF);
                            	profilingEndTimeInMillis = new Date().getTime();
								log.info("Saving apple subscription in case of renewal off : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
							}
                            //
                            ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(findUser.getUserId(), program.getProgramId());
                            if(subscribedProgram!=null) {
                                subscribedProgram.setAutoRenewal(false);
                                pgmSubscriptionRepo.save(subscribedProgram);
                            }
                            profilingEndTimeInMillis = new Date().getTime();
							log.info("Auto renewal off for apple subscription : total time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

						}
                        //Scenario : After purchase there is a possibility of unpublish/edit a program immediately.Then we should change subscription as Cancelled.
                        //Apple used to send 'Renewal ON' notification after each purchase.At that time there is a possibility of turning on subscription for unpublish/block programs.
                        if (Boolean.TRUE.equals(autoRenew) && programstatus.equalsIgnoreCase(InstructorConstant.PUBLISH)) { //Auto Subscription ON
							AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
                            if(user!=null) {
                            	profilingStartTimeInMillis = new Date().getTime();
                            	saveAppleProgramSubscription(user, program, transactionId, susbcriptionId,orginalTxnId, appleSubscriptionStatus,NotificationConstants.REN_ON);
                            	profilingEndTimeInMillis = new Date().getTime();
								log.info("Saving apple subscription in case of renewal on : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

								ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                                 if(subscribedProgram!=null) {
                                     subscribedProgram.setAutoRenewal(true);
                                     pgmSubscriptionRepo.save(subscribedProgram);
                                 }
                            }
                            profilingEndTimeInMillis = new Date().getTime();
							log.info("Auto renewal on for apple subscription : total time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
						}
                    }else if (eventType.equalsIgnoreCase(NotificationConstants.DID_RENEW)) {
                    	//TODO
                    }

                    //This block (INTERACTIVE_RENEWAL) is not required to handle.It will be handled in captureNewBuyDetails/cron Job.
			/*if (eventType.equalsIgnoreCase(NotificationConstants.INTERACTIVE_RENEWAL)) {
				Long userId=receipt.getUser().getUserId();
				log.info("Interactive Renewal user id: {}",userId);
				OrderManagement orderManagement = orderManagementRepo.findTop1ByProgramProgramIdAndUserUserIdAndModeOfPaymentOrderByCreatedDateDesc(programId,userId,NotificationConstants.MODE_OF_PAYMENT);
				log.info("Interactive Renewal OrderId : {}",orderManagement.getOrderId());

				// Update Program Subscription & Create Subscription Audit
				ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findByUserUserIdAndProgramProgramId(userId, programId);
				if(subscribedProgram!=null) {
					updateSubscribedPgm(receipt.getUser(), subscribedProgram, true, autoRenew,purchaseDate, eventType);
				}else {
					addNewSubscribedProgram(receipt.getUser(), receipt.getProgram(), true, autoRenew,purchaseDate,isTrail);
				}

				addPaymentInfo(info, orderManagement, eventType, "");
				// Update Order Status
				orderManagement.setOrderStatus(KeyConstants.KEY_SUCCESS);
				orderManagementRepo.save(orderManagement);

				// Update Apple Subscription Table
				AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
				saveAppleProgramSubscription(receipt.getUser(),receipt.getProgram(),transactionId,susbcriptionId,orginalTxnId,appleSubscriptionStatus);
			}
			*/
                    //DID_RECOVER sends for expired subscription as well (auto renew off/billing retry failed)
                    if (eventType.equalsIgnoreCase(NotificationConstants.DID_RECOVER)){
                        if(expiranIntent==null || expiranIntent.isEmpty()) {
                            /** successful automatic renewal of an expired subscription.**/
                            /** Reason For Commenting: Renewal happened via cron Job.(every 1 minute job will run)
                             * If we handle DID_RECOVER here 2 sets (order,invoice,payment) of data will created for same transaction id**/
					/*AppleProductSubscription aSubscription = aRepository.findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(transactionId,orginalTxnId);
					if (aSubscription != null && !aSubscription.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_SUBSCRIPTION_ACTIVE)) {
						Long userId = receipt.getUser().getUserId();
						log.info("Renewal user id: {}", userId);
						OrderManagement orderManagement = createOrderManagement(receipt.getProgram(), autoRenew,receipt.getUser(), "", KeyConstants.KEY_PROCESSING);
						log.info("Renewal Order id: {}", orderManagement.getOrderId());

						// Update Program Subscription & Create Subscription Audit
						ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(userId, programId);
						updateSubscribedPgm(receipt.getUser(), subscribedProgram, true, autoRenew, purchaseDate,eventType);
						addPaymentInfo(info, orderManagement, eventType, "");

						// Update Apple Subscription Table
						AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
						saveAppleProgramSubscription(receipt.getUser(), receipt.getProgram(), transactionId,susbcriptionId, orginalTxnId, appleSubscriptionStatus, NotificationConstants.RENEWAL);

						// Update Order Status
						updateOrderStatus(orderManagement.getOrderId());
					}*/

                        }else {
                        	profilingStartTimeInMillis = new Date().getTime();
                            //Expired transaction. No need to create Order,Payment Entry
							log.info("expirant_intent : {}",expiranIntent);
							log.info("Reason for Expiry : {}",info.getMessage());
							/** Update Apple Subscription Table */
                            AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
                            AppleProductSubscription appProductSubscription=aRepository.findTop1ByProgramAndUserAndAppleSubscriptionStatusOrderByModifiedDateDesc(program, user, appleSubscriptionStatus);
                            if(appProductSubscription==null) {
                                saveAppleProgramSubscription(user, program, transactionId, susbcriptionId,orginalTxnId, appleSubscriptionStatus,NotificationConstants.REN_OFF);
                            }

                            ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                            if(subscribedProgram!=null) {
                                subscribedProgram.setAutoRenewal(false);
                                pgmSubscriptionRepo.save(subscribedProgram);
                            }
                            profilingEndTimeInMillis = new Date().getTime();
							log.info("DID_RECOVER Event : total time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

						}
                    }

                    if (eventType.equalsIgnoreCase(NotificationConstants.DID_FAIL_TO_RENEW)) {
                    	start = new Date().getTime();
                        /** Apple Payment Failed Scenario. Order ,Invoice and Payment entry will be created */
                        Long userId=user.getUserId();
                        log.info("Payment Failed user id: {}",userId);
                        OrderManagement orderManagement = createOrderManagement(program,autoRenew, user,"",KeyConstants.KEY_FAILURE,offerCode);
                        log.info("Payment Failed Order id: {}",orderManagement.getOrderId());
                        profilingStartTimeInMillis = new Date().getTime();
                        addPaymentInfo(info, orderManagement, eventType, "");
                        profilingEndTimeInMillis = new Date().getTime();
						log.info("Adding payment info incase of payment failure : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));


						/** Update Apple Subscription Table */
                        AppleSubscriptionStatus appleSubscriptionStatus=null;
                        if(Boolean.TRUE.equals(isBillingRetry) ){
                            appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_SUSPENDED);

                        }else if(Boolean.FALSE.equals(isBillingRetry) ){ //Subscription is terminated since no more billing retry.
                            appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_TERMINATED);

                        }
                        AppleProductSubscription appProductSubscription=aRepository.findTop1ByProgramAndUserAndAppleSubscriptionStatusOrderByModifiedDateDesc(program, user, appleSubscriptionStatus);
                        if(appProductSubscription==null) {
                        	profilingStartTimeInMillis = new Date().getTime();
                            saveAppleProgramSubscription(user, program, transactionId, susbcriptionId,orginalTxnId, appleSubscriptionStatus,NotificationConstants.REN_OFF);
                            profilingEndTimeInMillis = new Date().getTime();
							log.info("Saving apple program subscription in case of payment failure : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

						}

                        //Auto Subscription as OFF
                        ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                        if(subscribedProgram!=null) {
                            subscribedProgram.setAutoRenewal(false);
                            pgmSubscriptionRepo.save(subscribedProgram);
                        }
                        profilingEndTimeInMillis = new Date().getTime();
						log.info("Saving Event change in case of payment failure : total time taken in milliseconds : " +(profilingEndTimeInMillis-start));

					}
                    if ((eventType.equalsIgnoreCase(NotificationConstants.CANCEL) && info.getCancellationDate() != null)) {
                    	start = new Date().getTime();
                        /** AppleCare refunded a subscription.Cancel Order via Customer Care Support. Order,Invoice and payment entry will be created. Refund initiated*/
                        //CANCEL, DID_CHANGE_RENEWAL_STATUS - Send from Apple
                        ProgramSubscription subscribedProgram = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), program.getProgramId());
                        if(subscribedProgram!=null) {
                            subscribedProgram.setAutoRenewal(false);
                            pgmSubscriptionRepo.save(subscribedProgram);
                        }
                        /** Update Apple Subscription Table */
                        AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);

                        AppleProductSubscription appProductSubscription=aRepository.findTop1ByProgramAndUserAndAppleSubscriptionStatusOrderByModifiedDateDesc(program, user, appleSubscriptionStatus);
                        if(appProductSubscription==null) {
                        	profilingStartTimeInMillis = new Date().getTime();
                            saveAppleProgramSubscription(user, program, transactionId, susbcriptionId,orginalTxnId, appleSubscriptionStatus,NotificationConstants.REN_OFF);
                            profilingEndTimeInMillis = new Date().getTime();
							log.info("Saving apple program subscription  in case of cancel and refund : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));

						}

                        //Requests a refund :Cancellation Date is present
                        OrderManagement orderManagement = createOrderManagement(program,autoRenew, user,"",KeyConstants.KEY_PROCESSING,offerCode);
                        log.info("Cancellation OrderId : {}",orderManagement.getOrderId());
                        eventType=KeyConstants.KEY_REFUND_INITIATED;
                        profilingStartTimeInMillis = new Date().getTime();
                        addPaymentInfo(info, orderManagement, eventType, "");
                        profilingEndTimeInMillis = new Date().getTime();
						log.info("Adding payment info in case of cncel and refund :  time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));


						/** Update Order Status */
						profilingStartTimeInMillis = new Date().getTime();
                        updateOrderStatus(orderManagement.getOrderId(),KeyConstants.KEY_REFUND_INITIATED);
                        profilingEndTimeInMillis = new Date().getTime();
						log.info("update order status in case of cancel and refund : time taken in milliseconds : " +(profilingEndTimeInMillis-profilingStartTimeInMillis));
						log.info("Saving Event change in case of cancel and refund : total time taken in milliseconds : " +(profilingEndTimeInMillis-start));


					}else if ((eventType.equalsIgnoreCase(NotificationConstants.CANCEL))){
                        /** User upgrade subscription to higher tier. & Cancel Existing subscription*/
                        AppleProductSubscription appleSubscription = aRepository.findTop1ByProgramAndUserOrderByModifiedDateDesc(program,user);
                        if (appleSubscription == null) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, null);
                        }

                        if (appleSubscription.getAppleSubscriptionStatus() != null &&
                                !appleSubscription.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_ACTIVE)) {
                            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_AUTO_SUBSCRIPTION_ALREADY_INACTIVE, null);
                        }

                        AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
                        saveAppleProgramSubscription(user, program, appleSubscription.getTransactionId(), appleSubscription.getAppleSubscriptionId(),appleSubscription.getOriginalTransactionId(), appleSubscriptionStatus,KeyConstants.KEY_SUBSCRIPTION_CANCELLED);

                        ProgramSubscription programSubscription = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
                        if (programSubscription != null) {
                            programSubscription.setAutoRenewal(false);
                            pgmSubscriptionRepo.save(programSubscription);
                        }

                    }
                    /** App Store successfully refunded a transaction **/
                    if (eventType.equalsIgnoreCase(NotificationConstants.REFUND)) {
                    	start = new Date().getTime();
                        // update the refund status in Payment table
                        ApplePayment payment=applePaymentRepository.findTop1ByTransactionIdAndOriginalTransactionIdOrderByModifiedDateDesc(transactionId,orginalTxnId);
                        if(payment != null){
                            payment.setTransactionStatus(KeyConstants.KEY_TRANSACTION_REFUND_SETTLED);
                            payment.setIsPaymentSettled(true);
                            applePaymentRepository.save(payment);
                            profilingStartTimeInMillis = new Date().getTime();
                            updateOrderStatus(payment.getOrderManagement().getOrderId(),KeyConstants.KEY_REFUNDED);
                            profilingEndTimeInMillis = new Date().getTime();
							log.info("Updating orderv status in case of successful refund : time taken in milliseconds : " +(profilingEndTimeInMillis-start));


							//
                            //Refund for Apple purchase
							profilingStartTimeInMillis = new Date().getTime();
                            fitwiseQboEntityService.createAndSyncAppleRefund(payment);
                            profilingEndTimeInMillis = new Date().getTime();
							log.info("Syncing apple refund in QBO : time taken in milliseconds : " +(profilingEndTimeInMillis-start));

						}
                        profilingEndTimeInMillis = new Date().getTime();
						log.info("Saving Event change in case of successful refund : total time taken in milliseconds : " +(profilingEndTimeInMillis-start));

					}
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Exception in updateSubscription", e.getMessage());
        }
    }

    @Transactional
    private OrderManagement createOrderManagement(Programs program, Boolean autorenew, User user,String exstOrdId,String orderStatus,OfferCodeDetail offerCodeDetail) {
        log.info("--------------Method :: Create Order Management --------------------");
        OrderManagement order ;
        try {
            OrderManagement orderManagement = new OrderManagement();
            PlatformType subscribedViaPlatform = platformsRepo.findByPlatformTypeId(NotificationConstants.APPLE_PLATFORM);

            /** Auto Generate Order Number for each transaction */
            if (exstOrdId.isEmpty()) {
                String orderNumber = OrderNumberGenerator.generateOrderNumber();
                log.info(" ******** Apple Order id **********{} ", orderNumber);
                orderManagement.setOrderId(orderNumber);
            } else {
                orderManagement.setOrderId(exstOrdId);
            }

            SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);
            orderManagement.setSubscriptionType(subscriptionType);

            orderManagement.setModeOfPayment(NotificationConstants.MODE_OF_PAYMENT);
            orderManagement.setIsAutoRenewable(autorenew);
            orderManagement.setProgram(program);
            if (user != null) {
                orderManagement.setUser(user);
            } else {
                User usr = userComponents.getUser();
                orderManagement.setUser(usr);
            }
            orderManagement.setOrderStatus(orderStatus);
            orderManagement.setSubscribedViaPlatform(subscribedViaPlatform);
            order = orderManagementRepo.save(orderManagement);

            /** Creating invoice for the Valid Order **/
            log.info("---------------{}" , exstOrdId.isEmpty());
            log.info("****Invoice Creation***");
            createInvoice(order, exstOrdId);
            
            log.info("Order-Offer Mapping");
			if (offerCodeDetail != null) {
				log.info(" Offer Code ::: "+offerCodeDetail.getOfferCode());
				OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = new OfferCodeDetailAndOrderMapping();
				offerCodeDetailAndOrderMapping.setOfferCodeDetail(offerCodeDetail);
				offerCodeDetailAndOrderMapping.setOrderManagement(order);
				offerCodeDetailAndOrderMappingRepository.save(offerCodeDetailAndOrderMapping);
			}
			
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Error in createOrderManagement method"+e.getMessage(),
                    e.getMessage());
        }
        log.info(" Order Id Created {}", order.getOrderId());
        return order;
    }

    @Transactional
    private InvoiceManagement createInvoice(OrderManagement orderManagement,String exstOrdId) {
        log.info("--------------Method :: Create Invoice Management --------------------");
        InvoiceManagement invoiceManagement;
        try {
            if (!exstOrdId.isEmpty()) {
                invoiceManagement=invoiceManagementRepository.findByOrderManagementOrderId(exstOrdId);
                invoiceManagement.setOrderManagement(orderManagement);
                invoiceManagementRepository.save(invoiceManagement);
            } else {
                invoiceManagement = new InvoiceManagement();
                invoiceManagement.setInvoiceNumber(OrderNumberGenerator.generateInvoiceNumber());
                invoiceManagement.setOrderManagement(orderManagement);
                invoiceManagementRepository.save(invoiceManagement);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Error in createInvoice method", e.getMessage());
        }
        log.info(" Invoice Id Created {}", invoiceManagement.getInvoiceNumber());
        return invoiceManagement;
    }

    /**
     * Create Program Subscription & Subscription Audit
     *
     * @param user
     * @param program
     * @param isPaymentSuccess
     * @param autoRenew
     */
    @Transactional
    private void addNewSubscribedProgram(User user, Programs program, boolean isPaymentSuccess, Boolean autoRenew,Date purchaseDate,Boolean isTrail,OrderManagement orderManagement) {
        SubscriptionStatus newSubscriptionStatus = null;
        log.info(" --------------- Method : addNewlySubscribedProgramData ----------------");
        try {
            if (isPaymentSuccess) {

                // TODO Naveen

                /** Default 1 Month */
                SubscriptionPlan subscriptionPlan = subscriptionPlansRepo.findByDuration(NotificationConstants.DURATION);
                SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);
                PlatformType platformType = platformsRepo.findByPlatformTypeId(NotificationConstants.APPLE_PLATFORM);

                ProgramSubscription pSubscription = new ProgramSubscription();
                pSubscription.setAutoRenewal(autoRenew);
                pSubscription.setSubscribedViaPlatform(platformType);
                pSubscription.setProgram(program);
                pSubscription.setUser(user);
                pSubscription.setSubscriptionPlan(subscriptionPlan);

                pSubscription.setSubscribedDate(purchaseDate);
               /* if (Boolean.TRUE.equals(isTrail)) {
                    newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_TRIAL);
                    if(newSubscriptionStatus!=null) {
                        pSubscription.setSubscriptionStatus(newSubscriptionStatus);
                    }

                } else {
                    newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
                    pSubscription.setSubscriptionStatus(newSubscriptionStatus);
                    if(newSubscriptionStatus!=null) {
                        pSubscription.setSubscriptionStatus(newSubscriptionStatus);
                    }
                }*/
                newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
                if(newSubscriptionStatus!=null) {
                    pSubscription.setSubscriptionStatus(newSubscriptionStatus);
                }
                pgmSubscriptionRepo.save(pSubscription);

                //Saving revenueAudit table to store all tax details
                ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
                programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
                subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);


                /** Auditing the subscription */
                SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
                subscriptionAudit.setUser(user);
                subscriptionAudit.setSubscriptionType(subscriptionType);
                subscriptionAudit.setProgramSubscription(pSubscription);
                subscriptionAudit.setSubscriptionPlan(subscriptionPlan);
                subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
                subscriptionAudit.setSubscribedViaPlatform(platformType);
                subscriptionAudit.setSubscriptionDate(purchaseDate);
                subscriptionAudit.setAutoRenewal(autoRenew);
                subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
                subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);

                subscriptionAuditRepo.save(subscriptionAudit);
            }
        } catch (Exception e) {
            throw new ApplicationException(Constants.ERROR_STATUS, "Error in Method : addNewlySubscribedProgramData",
                    e.getMessage());
        }

    }

    /**
     * Update Program Subscription & Create Subscription Audit
     *
     * @param user
     * @param subscribedProgram
     * @param isPaymentSuccess
     * @param autoRenew
     * @param eventType
     */
    @Transactional
    public void updateSubscribedPgm(User user, ProgramSubscription subscribedProgram, boolean isPaymentSuccess,
                                    Boolean autoRenew,Date purchaseDate, String eventType,OrderManagement orderManagement) {
        log.info("----------------- Method: updateSubscribedPgm -----------------------");
        SubscriptionStatus newSubscriptionStatus = null;
        try {
            if (isPaymentSuccess) {
                log.info("eventType {}, purchase date {}",eventType ,purchaseDate);
                // TODO Naveen
                PlatformType platformType = platformsRepo.findByPlatformTypeId(NotificationConstants.APPLE_PLATFORM);
                if (eventType.equalsIgnoreCase(NotificationConstants.DID_RECOVER)//Renewal
                        || eventType.equalsIgnoreCase(NotificationConstants.INTERACTIVE_RENEWAL)
                        || eventType.equalsIgnoreCase(NotificationConstants.RENEWAL)) {
                    newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
                }else if (eventType.equalsIgnoreCase(NotificationConstants.REFUND)) {
                    newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_REFUND);
                }else if (eventType.equalsIgnoreCase(KeyConstants.KEY_EXPIRED)){
                    newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_EXPIRED);
                }else if (eventType.equalsIgnoreCase(NotificationConstants.DID_CHANGE_RENEWAL_STATUS)){
                    if(Boolean.TRUE.equals(autoRenew)) {
                        newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_PAID);
                    }else if(Boolean.FALSE.equals(autoRenew)){
                        newSubscriptionStatus = subscriptionStatusRepo.findBySubscriptionStatusNameIgnoreCaseContaining(KeyConstants.KEY_EXPIRED);
                    }
                }

                log.info("New Subscription status {} :", newSubscriptionStatus);

                subscribedProgram.setSubscriptionPlan(subscribedProgram.getSubscriptionPlan());
                subscribedProgram.setSubscribedViaPlatform(platformType);
                subscribedProgram.setSubscribedDate(purchaseDate); // info.getPurchaseDate()
                if(newSubscriptionStatus!=null) {
                    subscribedProgram.setSubscriptionStatus(newSubscriptionStatus);
                }

                ProgramSubscription saveProgramSubscription = pgmSubscriptionRepo.save(subscribedProgram);

                /** SubscriptionAudit */
                SubscriptionType subscriptionType = subscriptionTypesRepo.findByNameIgnoreCase(KeyConstants.KEY_PROGRAM);

                //Saving revenueAudit table to store all tax details
                ProgramSubscriptionPaymentHistory programSubscriptionPaymentHistory = new ProgramSubscriptionPaymentHistory();
                programSubscriptionPaymentHistory.setOrderManagement(orderManagement);
                subscriptionPaymentHistoryRepository.save(programSubscriptionPaymentHistory);


                SubscriptionAudit subscriptionAudit = new SubscriptionAudit();
                subscriptionAudit.setUser(user);
                subscriptionAudit.setSubscriptionType(subscriptionType);
                subscriptionAudit.setProgramSubscription(saveProgramSubscription);
                subscriptionAudit.setSubscriptionPlan(subscribedProgram.getSubscriptionPlan());
                subscriptionAudit.setSubscriptionStatus(newSubscriptionStatus);
                subscriptionAudit.setSubscribedViaPlatform(platformType);
                subscriptionAudit.setSubscriptionDate(purchaseDate);
                subscriptionAudit.setProgramSubscriptionPaymentHistory(programSubscriptionPaymentHistory);
                subscriptionAudit.setAutoRenewal(autoRenew);

                SubscriptionAudit subscriptionAuditOfProgram = subscriptionAuditRepo
                        .findBySubscriptionTypeNameAndProgramSubscriptionProgramSubscriptionIdOrderBySubscriptionDateDesc(
                                KeyConstants.KEY_PROGRAM,subscribedProgram.getProgramSubscriptionId())
                        .get(0);

                if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW) && subscriptionAuditOfProgram.getSubscriptionStatus() != null &&
                        subscriptionAuditOfProgram.getSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_TRIAL)) {
                    // If renewal status is new and subscription status is trial, then next paid subscription will be set to new
                    subscriptionAudit.setRenewalStatus(KeyConstants.KEY_NEW);
                } else if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_NEW)) {
                    // Already the renewal status is new! So setting it has renew on the second time
                    subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);

                } else if (subscriptionAuditOfProgram.getRenewalStatus().equalsIgnoreCase(KeyConstants.KEY_RENEWAL)) {
                    // Already the renewal status is renew! So will be set as renew in next coming
                    // times
                    subscriptionAudit.setRenewalStatus(KeyConstants.KEY_RENEWAL);
                }

                subscriptionAuditRepo.save(subscriptionAudit);


            }
        } catch (Exception e) {
            throw new ApplicationException(Constants.ERROR_STATUS, "Exception in updateSubscribedPgm", e.getMessage());
        }

    }

    /**
     * Apple Payment ; Track Payment info during Buy/Renew product.
     *
     * @param iosReceiptInfo
     * @param orderManagement
     * @param eventType
     * @param erMsg
     */
    @Transactional
    private void addPaymentInfo(IosReceiptInfo iosReceiptInfo, OrderManagement orderManagement, String eventType,
                                String erMsg) {
        log.info("--------------- Method : addTransactionToApplePaymentTable   ---------------------");
        ApplePayment applePayment = new ApplePayment();
        try {
            applePayment.setOrderManagement(orderManagement);
            
            /*OfferCodeDetailAndOrderMapping offerCodeDetailAndOrderMapping = offerCodeDetailAndOrderMappingRepository.findTop1ByOrderManagementOrderByCreatedDateDesc(orderManagement);
			if (offerCodeDetailAndOrderMapping != null) {
				OfferCodeDetail offerCodeDetail = offerCodeDetailAndOrderMapping.getOfferCodeDetail();
				double offerPrice = DiscountsConstants.MODE_FREE.equalsIgnoreCase(offerCodeDetail.getOfferMode()) ? 0.00
						: offerCodeDetail.getOfferPrice().getPrice();
				applePayment.setProgramPrice(offerPrice);
			} else {
				applePayment.setProgramPrice(orderManagement.getProgram().getProgramPrice());
			}*/
			applePayment.setProgramPrice(orderManagement.getProgram().getProgramPrice());
            if (!erMsg.isEmpty()) {
                applePayment.setErrorMessage(erMsg);
            }
            if (iosReceiptInfo != null) {
                if (iosReceiptInfo.getStatus() != null && iosReceiptInfo.getStatus()
                        .equalsIgnoreCase(NotificationConstants.APPLE_TRANSACTION_SUCCESS_RESPONSE_CODE)) {
                    applePayment.setReceiptNumber(OrderNumberGenerator.generateReceiptNumber());
                }

                if (iosReceiptInfo.getTransactionId() != null)
                    applePayment.setTransactionId(iosReceiptInfo.getTransactionId());

                if (iosReceiptInfo.getOriginalTransactionId() != null)
                    applePayment.setOriginalTransactionId(iosReceiptInfo.getOriginalTransactionId());

                if (iosReceiptInfo.getWeborderLineitemId() != null)
                    applePayment.setSubscriptionId(iosReceiptInfo.getWeborderLineitemId());

                if (iosReceiptInfo.getStatus() != null)
                    applePayment.setStatusCode(iosReceiptInfo.getStatus());

                if (iosReceiptInfo.getAutoRenewStatus() != null)
                    applePayment.setIsAutoRenew(iosReceiptInfo.getAutoRenewStatus());

                if (iosReceiptInfo.getMessage() != null) {
                    applePayment.setErrorMessage(iosReceiptInfo.getMessage());
                }
                if (iosReceiptInfo.getPurchaseDate() != null)
                    applePayment.setPurchaseDate(iosReceiptInfo.getPurchaseDate());

                if (iosReceiptInfo.getExpiresDate() != null)
                    applePayment.setExpiryDate(iosReceiptInfo.getExpiresDate());

                applePayment.setIsPaymentSettled(false);
            }

            if (eventType.equalsIgnoreCase(NotificationConstants.INITIAL_BUY)
                    || eventType.equalsIgnoreCase(NotificationConstants.RENEWAL)
                    || eventType.equalsIgnoreCase(NotificationConstants.DID_RECOVER)
                    || eventType.equalsIgnoreCase(NotificationConstants.INTERACTIVE_RENEWAL)) {
                applePayment.setTransactionStatus(KeyConstants.KEY_PAYMENT_SUCCESS);
            }
            if (eventType.equalsIgnoreCase(NotificationConstants.DID_FAIL_TO_RENEW)) {
                applePayment.setTransactionStatus(KeyConstants.KEY_PAYMENT_FAILURE);
            }
            if (eventType.equalsIgnoreCase(KeyConstants.KEY_PAYMENT_PENDING)) {
                applePayment.setTransactionStatus(KeyConstants.KEY_PAYMENT_PENDING);
            }
            if (eventType.equalsIgnoreCase(NotificationConstants.CANCEL)) {
                applePayment.setTransactionStatus(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
            }
            if (eventType.equalsIgnoreCase(KeyConstants.KEY_REFUND_INITIATED)) {
                applePayment.setTransactionStatus(KeyConstants.KEY_REFUND_INITIATED);
            }

            applePaymentRepository.save(applePayment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS,
                    "Error in Method : addTransactionToApplePaymentTable" + e.getMessage(), e.getMessage());
        }

    }

    /**
     * Create Entry for each Transaction for Better tracking in 'ios_receipt_info'
     * Construct an Object from receipt array.
     * @param notification
     * @return IosReceiptInfo
     */
    private IosReceiptInfo saveEventChange(JSONObject notification) {
        log.info("--------------- Method : saveEventChange   ---------------------");
        IosReceiptInfo iosReceiptInfo = null;
        JSONObject unifiedReceipt = null;
        JSONObject latestReceipt = null;
        JSONArray pendingRenewalArray = null;
        JSONObject pendingRenewal = null;
        String expirantIntent = "";
        Boolean autoRenew = false;
        String notificationType= "";
        String environment="";
        String originalTransactionId= "";
        JSONArray latestReceiptArray=null;
        try {
            iosReceiptInfo = new IosReceiptInfo();
            // NotificationType
            if (notification.has(NotificationConstants.NOTIFICATION)) {
                notificationType=notification.getString(NotificationConstants.NOTIFICATION);
                log.info(" ***** notificationType ***** {}", notificationType);
                iosReceiptInfo.setNotificationType(notificationType);
            }
            // Program id
            if (notification.has(NotificationConstants.AUTO_RENEW_PDT)) {
                //programId = programFormat(notification.getString(NotificationConstants.AUTO_RENEW_PDT));
                log.info(" ***** Program Id ***** {}", notification.getString(NotificationConstants.AUTO_RENEW_PDT));
            }
            // Auto renew status
            if (notification.has(NotificationConstants.AUTO_RENEW)) {
                if(notification.getString(NotificationConstants.AUTO_RENEW).equalsIgnoreCase("true")) {
                    autoRenew=true;
                }else if(notification.getString(NotificationConstants.AUTO_RENEW).equalsIgnoreCase("false")) {
                    autoRenew=false;
                }
                iosReceiptInfo.setAutoRenewStatus(autoRenew);
            }
            // Auto renew status (on /off) change date
            if (notification.has(NotificationConstants.AUTO_RENEW_DT)) {
                iosReceiptInfo.setAutorenewStatusChangeDate(
                        dateFormat(notification.getString(NotificationConstants.AUTO_RENEW_DT)));
            }
            if (notification.has(NotificationConstants.ENVIRONMENT)) {
            	environment=notification.getString(NotificationConstants.ENVIRONMENT);
            	log.info(" ***** environment ***** {}", environment);
            }
            if (notification.has(NotificationConstants.UNIFIED_RCPT)) {
                unifiedReceipt = notification.getJSONObject(NotificationConstants.UNIFIED_RCPT);
                /** Fetching Pending Renewal Info Array */
                if (unifiedReceipt.has(NotificationConstants.PENDING_RENEWAL_INFO)) {
                    pendingRenewalArray = unifiedReceipt.getJSONArray(NotificationConstants.PENDING_RENEWAL_INFO);
                }
                if (unifiedReceipt.has(NotificationConstants.RCPT_ST)) {
                    iosReceiptInfo.setStatus(String.valueOf(unifiedReceipt.getInt(NotificationConstants.RCPT_ST)));
                }
                if (unifiedReceipt.has(NotificationConstants.ENVIRONMENT)) {
                	environment=unifiedReceipt.getString(NotificationConstants.ENVIRONMENT);
                	log.info(" ***** unifiedReceipt environment ***** {}", environment);
                }
            }

            if (pendingRenewalArray != null) {
                for (int i = 0; i < pendingRenewalArray.length(); i++) {
                    if (pendingRenewalArray.getJSONObject(i).getString(NotificationConstants.PRODUCT_ID)
                            .equalsIgnoreCase(notification.getString(NotificationConstants.AUTO_RENEW_PDT))) {
                        pendingRenewal = pendingRenewalArray.getJSONObject(i);
                    }
                }

                // Program Subscription is in Trial
                if (pendingRenewal != null && pendingRenewal.has(NotificationConstants.IS_BILLING_RETRY)) {
                    iosReceiptInfo.setBillingRetry(
                            Boolean.valueOf(pendingRenewal.getString(NotificationConstants.IS_BILLING_RETRY)));
                }
                // Program Subscription is Expired
                if (pendingRenewal != null && pendingRenewal.has(NotificationConstants.EXPIRANT_INTENT)) {
                    expirantIntent = pendingRenewal.getString(NotificationConstants.EXPIRANT_INTENT);
                    iosReceiptInfo.setExpirationIntent(expirantIntent);
                    if (!expirantIntent.isEmpty()) {
                        iosReceiptInfo.setMessage(expirationStatus(expirantIntent));
                    }

                }
                if (pendingRenewal != null && pendingRenewal.has(NotificationConstants.ORIGINAL_TRANSACTION_ID)) {
                	originalTransactionId=pendingRenewal.getString(NotificationConstants.ORIGINAL_TRANSACTION_ID);
                }
            }
            //
            log.info(" ***** Notification Environment ***** {}", environment);
            if(!environment.isEmpty() && environment.equalsIgnoreCase(NotificationConstants.ENV_SANDBOX)) {
            	 if (unifiedReceipt.has(NotificationConstants.LATEST_RCPT)) {
                     latestReceiptArray = unifiedReceipt.getJSONArray(NotificationConstants.LATEST_RCPT);
                     for (int i = 0; i < latestReceiptArray.length(); i++) {
                         if (latestReceiptArray.getJSONObject(i).getString(NotificationConstants.PRODUCT_ID).trim().equalsIgnoreCase(notification.getString(NotificationConstants.AUTO_RENEW_PDT))
                                 && latestReceiptArray.getJSONObject(i).getString(NotificationConstants.ORIGINAL_TRANSACTION_ID).trim().equalsIgnoreCase(originalTransactionId.trim())) {
                        	 latestReceipt=latestReceiptArray.getJSONObject(i);
                             break;
                         }
                     }
                 }
            	 // 
			} else {
				/** Active Transaction contains 'latest_receipt_info . */
				if (notification.has(NotificationConstants.LATEST_RCPT)) {
					latestReceipt = notification.getJSONObject(NotificationConstants.LATEST_RCPT);
					log.info(" *** Received  from iOS *** ", latestReceipt);
				}

				/** Expired Transaction contains 'latest_expired_receipt_info . */
				if (notification.has(NotificationConstants.LATEST_EXPIRED_RCPT)) {
					latestReceipt = notification.getJSONObject(NotificationConstants.LATEST_EXPIRED_RCPT);
					log.info(" *** Received Expired from iOS *** ", latestReceipt);
				}

			}
            /** Constructing Object **/
            if (latestReceipt != null) {
                //Check for product Identifier
                iosReceiptInfo.setLatestReceiptData(latestReceipt.toString());
                //Program Id
                if (latestReceipt.has(NotificationConstants.PRODUCT_ID)) {
                    iosReceiptInfo.setProgramId(
                            Long.valueOf(programFormat(latestReceipt.getString(NotificationConstants.PRODUCT_ID))));
                }
                //Transaction Id
                if (latestReceipt.has(NotificationConstants.TRANSACTION_ID)) {
                    iosReceiptInfo.setTransactionId(latestReceipt.getString(NotificationConstants.TRANSACTION_ID));
                }
                // Original Transaction Id
                if (latestReceipt.has(NotificationConstants.ORIGINAL_TRANSACTION_ID)) {
                    iosReceiptInfo.setOriginalTransactionId(
                            latestReceipt.getString(NotificationConstants.ORIGINAL_TRANSACTION_ID));
                }
                // WebOrderLineItemId (Subscription Id)
                if (latestReceipt.has(NotificationConstants.SUBSCRIPTION_ID)) {
                    iosReceiptInfo
                            .setWeborderLineitemId(latestReceipt.getString(NotificationConstants.SUBSCRIPTION_ID));
                }
                // Program Purchase Date
                if (latestReceipt.has(NotificationConstants.PURCHASE_DT)) {
                    iosReceiptInfo
                            .setPurchaseDate(dateFormat(latestReceipt.getString(NotificationConstants.PURCHASE_DT)));
                }
                // Program Expire Date
                if (latestReceipt.has(NotificationConstants.EXPIRES_DT_PST)) {
                    iosReceiptInfo.setExpiresDate(dateFormat(latestReceipt.getString(NotificationConstants.EXPIRES_DT_PST)));
                }else if (latestReceipt.has(NotificationConstants.EXPIRES_DT)) {
                    iosReceiptInfo
                            .setExpiresDate(dateFormat(latestReceipt.getString(NotificationConstants.EXPIRES_DT)));
                }
                
               // Program Subscription Cancellation Date
                if (latestReceipt.has(NotificationConstants.CANCEL_DT)) {
                    iosReceiptInfo
                            .setCancellationDate(dateFormat(latestReceipt.getString(NotificationConstants.CANCEL_DT)));
                }

                /** Set Cancellation Reason Message */
                // Program Subscription Cancellation Reason
                if (latestReceipt.has(NotificationConstants.CANCEL_REASON)) {
                    if (latestReceipt.getString(NotificationConstants.CANCEL_REASON).equalsIgnoreCase("0")) {
                        iosReceiptInfo.setMessage(NotificationConstants.CANCEL_0);
                    } else if (latestReceipt.getString(NotificationConstants.CANCEL_REASON).equalsIgnoreCase("1")) {
                        iosReceiptInfo.setMessage(NotificationConstants.CANCEL_1);
                    }
                }

                // Program is in Trial
                if (latestReceipt.has(NotificationConstants.IS_TRIAL)) {
                    iosReceiptInfo
                            .setIsTrialPeriod(Boolean.valueOf(latestReceipt.getString(NotificationConstants.IS_TRIAL)));
                }
                if (latestReceipt.has(NotificationConstants.IS_IN_INTRO_OFFER)) {
                    iosReceiptInfo
                            .setIsInIntroOffer(Boolean.valueOf(latestReceipt.getString(NotificationConstants.IS_IN_INTRO_OFFER)));
                } 
                //Offer Id
                if(latestReceipt.has(NotificationConstants.OFFER_ID)) {
                    iosReceiptInfo.setOfferId(latestReceipt.getString(NotificationConstants.OFFER_ID));
                }
                //Offer Name
                if(latestReceipt.has(NotificationConstants.OFFER_NAME)) {
                    iosReceiptInfo.setOfferName(latestReceipt.getString(NotificationConstants.OFFER_NAME));
                }
            }
            //
            
            iosReceiptInfo=iReceiptInfoRepository.save(iosReceiptInfo);

            AppleSubscriptionStatus appSubscriptionStatus = findSubcriptionStatus(notification.getString(NotificationConstants.NOTIFICATION),iosReceiptInfo);
            if (appSubscriptionStatus != null) {
                iosReceiptInfo.setSubscriptionStatus(appSubscriptionStatus);
            }
            iosReceiptInfo = iReceiptInfoRepository.save(iosReceiptInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Exception in saveEventChange", e.getMessage());
        }
        return iosReceiptInfo;
    }

    private AppleSubscriptionStatus findSubcriptionStatus(String status,IosReceiptInfo iosReceiptInfo) {
        log.info("-------- Entering in Method : findSubcriptionStatus -----------{} ",status);
        AppleSubscriptionStatus appleSubscriptionStatus = null;
        try {

            if (status.equalsIgnoreCase(NotificationConstants.INITIAL_BUY)
                    || status.equalsIgnoreCase(NotificationConstants.RENEWAL)
                    || status.equalsIgnoreCase(NotificationConstants.DID_RENEW)
                    || status.equalsIgnoreCase(NotificationConstants.INTERACTIVE_RENEWAL)) {
                appleSubscriptionStatus = appleSubscriptionStatusRepo
                        .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
            } else if (status.equalsIgnoreCase(NotificationConstants.DID_CHANGE_RENEWAL_STATUS)) {
                if (Boolean.TRUE.equals(iosReceiptInfo.getAutoRenewStatus())) {
                    appleSubscriptionStatus = appleSubscriptionStatusRepo
                            .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);

                } else if (Boolean.FALSE.equals(iosReceiptInfo.getAutoRenewStatus())) {
                    appleSubscriptionStatus = appleSubscriptionStatusRepo
                            .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
                }
            } else if (status.equalsIgnoreCase(NotificationConstants.DID_FAIL_TO_RENEW)) {
                if (Boolean.TRUE.equals(iosReceiptInfo.getBillingRetry())) {
                    appleSubscriptionStatus = appleSubscriptionStatusRepo
                            .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_SUSPENDED);

                } else if (Boolean.FALSE.equals(iosReceiptInfo.getBillingRetry())) { // Subscription is terminated since
                    // no more billing retry.
                    appleSubscriptionStatus = appleSubscriptionStatusRepo
                            .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_TERMINATED);
                }
            } else if (status.equalsIgnoreCase(NotificationConstants.DID_RECOVER)) {
                if (!iosReceiptInfo.getExpirationIntent().isEmpty()) {
                    appleSubscriptionStatus = appleSubscriptionStatusRepo
                            .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
                } else {
                    appleSubscriptionStatus = appleSubscriptionStatusRepo
                            .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_ACTIVE);
                }
            } else if (status.equalsIgnoreCase(NotificationConstants.CANCEL)) {
                appleSubscriptionStatus = appleSubscriptionStatusRepo
                        .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
            } else if (status.equalsIgnoreCase(NotificationConstants.REFUND)) {
                appleSubscriptionStatus = appleSubscriptionStatusRepo
                        .findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in findSubcriptionStatus Method :{} ", e.getMessage());
        }
        return appleSubscriptionStatus;
    }

    public Date dateFormat(String sDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        //Receipt has America/Los_Angeles time format.In server we have UTC time format.
        //America/Los_Angeles is a UTC -08:00 timezone offset where as UTC is a UTC 0:0 timezone offset.
        try {
            String inDate=sDate.substring(0, 20);
            inDate=inDate.trim();
            //Step 1: Converting String into Date (America/Los_Angeles)
            String format = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat estFormatter = new SimpleDateFormat(format);
            estFormatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            Date dt= estFormatter.parse(inDate);
            //Step 2: Convert America/Los_Angeles date format into UTC date format
            DateFormat utcFormatter = new SimpleDateFormat(format);
            utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String covertedUTCtime=utcFormatter.format(dt);
            date=sdf.parse(covertedUTCtime);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return date;
    }

    private String programFormat(String program) {
        log.info("-------- Entering in Method : programFormat ----------- ");
        String programName = "";
        try {
			/*AppConfigKeyValue app = appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
			log.info("**** PDT_IDENTIFIER {} *********", app.getValueString());
			String[] productId = program.split(app.getValueString().concat("."));
			programName = productId[1].trim();*/
            programName=program.substring(program.lastIndexOf(".")+1, program.length());
            log.info("program name ---------- "+programName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApplicationException(Constants.ERROR_STATUS, "Error in Method programFormat", e.getMessage());
        }
        return programName;
    }

    private String expirationStatus(String status) {
        String msg = "";
        try {
            switch (status) {
                case "1":
                    /**Auto Subscription Off */
                    msg = "The customer voluntarily cancelled their subscription.";
                    log.info("\n  1 : The customer voluntarily canceled their subscription. ");

                    break;
                case "2":
                    msg = "Billing error. The customer's payment information was no longer valid.";
                    log.info("\n  2 : Billing error. The customer's payment information was no longer valid.");
                    break;
                case "3":
                    msg = "The customer did not agree to a recent price increase.";
                    log.info("\n  3 : The customer did not agree to a recent price increase.");
                    break;
                case "4":
                    msg = "The product was not available for purchase at the time of renewal.";
                    log.info("\n  4 : The product was not available for purchase at the time of renewal.");
                    break;
                case "5":
                    msg = "Unknown error.";
                    log.info("\n  5 : Unknown error.");
                    break;

                default:
                    msg = "";
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return msg;
    }
    public ResponseModel cancelSubscription(Long programId,Long platformId,User user) {
        try {
            log.info(" Environment {}",appleProperties.getEnvironment());
            if(!appleProperties.getEnvironment().equalsIgnoreCase(KeyConstants.KEY_PROD)) {
                Programs program = validationService.validateProgramIdBlocked(programId);

                AppleProductSubscription appleSubscription = aRepository.findTop1ByProgramAndUserOrderByModifiedDateDesc(program,user);
                if (appleSubscription == null) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PROGRAM_NOT_SUBSCRIBED, null);
                }

                if (appleSubscription.getAppleSubscriptionStatus() != null &&
                        !appleSubscription.getAppleSubscriptionStatus().getSubscriptionStatusName().equalsIgnoreCase(KeyConstants.KEY_ACTIVE)) {
                    throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_AUTO_SUBSCRIPTION_ALREADY_INACTIVE, null);
                }

                AppleSubscriptionStatus appleSubscriptionStatus = appleSubscriptionStatusRepo.findBySubscriptionStatusName(KeyConstants.KEY_SUBSCRIPTION_CANCELLED);
                saveAppleProgramSubscription(user, program, appleSubscription.getTransactionId(), appleSubscription.getAppleSubscriptionId(),appleSubscription.getOriginalTransactionId(), appleSubscriptionStatus,KeyConstants.KEY_SUBSCRIPTION_CANCELLED);

                ProgramSubscription programSubscription = pgmSubscriptionRepo.findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(user.getUserId(), programId);
                if (programSubscription != null) {
                    programSubscription.setAutoRenewal(false);
                    pgmSubscriptionRepo.save(programSubscription);
                }
            }

        }catch(Exception e) {
            throw new ApplicationException(Constants.ERROR_STATUS, "Exception in cancelSubscription method.", e.getMessage());
        }
        return new ResponseModel(Constants.SUCCESS_STATUS, "Subscription has been Cancelled", null);

    }

    @Scheduled(cron=InAppPurchaseConstants.PAYMENT_SETTLEMENT)
	@Transactional
	public ResponseModel paymentSettlementProcess() {
        log.info("-------- Entering in Method : paymentSettlementProcess ----------- ");
        try {
            //Get the list of unSettled Payments
            List<ApplePayment> unSettledPayments= applePaymentRepository.findByTransactionStatusAndIsPaymentSettled(KeyConstants.KEY_PAYMENT_SUCCESS,false);
            // Settlement Day:45th day after the end of that calendar month(where the purchase happened)
            // Example ->Purchase Date July1=> July 31+45 Days
            // Getting the current subscription date
	        if(unSettledPayments!=null && !unSettledPayments.isEmpty()) {
                for(ApplePayment payment:unSettledPayments) {
                    LocalDate todayDate = LocalDate.now();
                    LocalDate subscribedDate = new java.sql.Date(payment.getPurchaseDate().getTime()).toLocalDate();
                    LocalDate lastDateOfSubscribedMonth = subscribedDate.withDayOfMonth(subscribedDate.lengthOfMonth());
                    LocalDate applePaymentSettlementDate = lastDateOfSubscribedMonth.plusDays(45);
                    Period intervalPeriod = Period.between(todayDate, applePaymentSettlementDate);
                    if(intervalPeriod.isZero() || intervalPeriod.isNegative()) {
                        payment.setIsPaymentSettled(true);
                        applePaymentRepository.save(payment);
                        //Sync settlement to QBO
                        fitwiseQboEntityService.createAndSyncApplePayment(payment);
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        log.info("-------- End Process : paymentSettlementProcess ----------- ");
        return new ResponseModel(Constants.SUCCESS_STATUS, "Method paymentSettlementProcess executed successfully", null);
    }

	public SignatureResponseView generateSignature(String productIdentifier,String offerIdentifier, String applicationUsrname) throws Exception {
		SignatureResponseView reponseView=new SignatureResponseView();
		//User user = userComponents.getUser();
		if(productIdentifier==null || productIdentifier.isEmpty()) {
			 throw new ApplicationException(Constants.BAD_REQUEST, DiscountsConstants.MSG_PRODUCT_IDENTIFIER_NULL_EMPTY, null);
		}
		AppConfigKeyValue idConfigKeyValue=appConfigKeyValueRepository.findByKeyString(NotificationConstants.PDT_IDENTIFIER);
		String pdtIdentifier=idConfigKeyValue.getValueString();
		if(!productIdentifier.contains(pdtIdentifier)) {
			throw new ApplicationException(Constants.BAD_REQUEST, DiscountsConstants.MSG_PRODUCT_IDENTIFIER_INVALID, null);
		}
		if(offerIdentifier==null || offerIdentifier.isEmpty()) {
			 throw new ApplicationException(Constants.BAD_REQUEST, DiscountsConstants.MSG_OFFER_IDENTIFIER_NULL_EMPTY, null);
		}
		
		OfferCodeDetail offerCodeDetail = offerCodeDetailRepository.findByOfferCodeAndIsInUse(offerIdentifier, true);
        if(offerCodeDetail == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_OFFER_CODE_ID_INVALID, null);
        }	
		
		//String productIdentifier=signatureRequestView.getProductIdentifier();
    	//String offerIdentifier=signatureRequestView.getOfferIdentifier();
   
    	//UserProfile userProfile=uProfileRepository.findByUser(user);
    	//String applicationUsername=userProfile.getFirstName().concat(userProfile.getLastName());
    	//String applicationUsername="ShanS";
    	if (privateKey == null) {
    		privateKey = getPrivateKey();
        }
    	String appBundleId=mobileAppProperties.getMemberiOSPackage();
    	String keyIdentifier=appleProperties.getKeyIdentifier();
    	UUID nonce=UUID.randomUUID();
    	long timestamp=System.currentTimeMillis() + (1000 * 60 * 5);
        String combinedString=new String(appBundleId + '\u2063' + keyIdentifier + '\u2063' + productIdentifier + '\u2063' + offerIdentifier + '\u2063' + applicationUsrname + '\u2063' + nonce + '\u2063' + timestamp);
    	byte[] arr = combinedString.getBytes("UTF-8");
    	
    	String signECDSA = signECDSA(privateKey, arr);
    	//
    	reponseView.setBundleIdentifier(appBundleId);
    	reponseView.setProductIdentifier(productIdentifier);
    	reponseView.setKeyIdentifier(keyIdentifier);
    	reponseView.setOfferIdentifier(offerIdentifier);
    	reponseView.setApplicationUsername(applicationUsrname);
    	reponseView.setNonce(nonce);
    	reponseView.setTimestamp(timestamp);
    	reponseView.setSignature(signECDSA);
		return reponseView;
	}
	 public static String signECDSA(PrivateKey privateKey, byte[] data) {
	        String result = "";
	        try {
	            Signature signature = Signature.getInstance(SIGNALGORITHMS);
	            signature.initSign(privateKey);
	            signature.update(data);
	            result= Base64.getEncoder().encodeToString(signature.sign());
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return result;
	    }

	 private static PrivateKey getPrivateKey() throws Exception {
	        //read your key
	        InputStream inputStream = new ClassPathResource("apple/SubscriptionKey_FF7BD6NJ7T.p8").getInputStream();

	        final PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream));
	        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
	        final PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
	        final PrivateKey pKey = converter.getPrivateKey(object);

	        return pKey;
	    }
	 
	 public String getJson(PayloadJsonRequestView view) throws ClientProtocolException, IOException {
		String receiptURL = "";
		String appSecretKey = "";

		CloseableHttpResponse response = null;
		CloseableHttpClient client = HttpClients.createDefault();
		JSONObject requestData = new JSONObject();
		requestData.put(NotificationConstants.RECIEPT_DATA, view.getData());
		requestData.put(NotificationConstants.RECIEPT_PD, appSecretKey);
		requestData.put(NotificationConstants.EXCL_OLD_TXN, false);
		
		HttpPost httpPost = new HttpPost(receiptURL);
		StringEntity entity = new StringEntity(requestData.toString());
		httpPost.setEntity(entity);
		httpPost.setHeader(NotificationConstants.CONT_TYP, NotificationConstants.JSON);
		response = client.execute(httpPost);
		String json = EntityUtils.toString(response.getEntity());
		JSONObject payload = new JSONObject(json);
		
		
		return json;//"success";
	 }
}

package com.fitwise.components;

import com.fitwise.constants.EmailConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.RestUrlConstants;
import com.fitwise.constants.SecurityFilterConstants;
import com.fitwise.constants.StringArrayConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.StripeConstants;
import com.fitwise.constants.VideoUploadStatus;
import com.fitwise.entity.Images;
import com.fitwise.entity.InstructorOutstandingPayment;
import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.NotifyEmail;
import com.fitwise.entity.User;
import com.fitwise.entity.UserActiveInactiveTracker;
import com.fitwise.entity.UserRole;
import com.fitwise.entity.VideoManagement;
import com.fitwise.entity.payments.paypal.UserAccountAndPayPalIdMapping;
import com.fitwise.entity.payments.stripe.connect.AppleSettlementByStripe;
import com.fitwise.entity.payments.stripe.connect.StripeAccountAndUserMapping;
import com.fitwise.entity.payments.stripe.connect.StripeTransferAndReversalMapping;
import com.fitwise.entity.subscription.PackageSubscription;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.exercise.service.VimeoService;
import com.fitwise.properties.GeneralProperties;
import com.fitwise.properties.StripeProperties;
import com.fitwise.repository.InstructorOutstandingPaymentRepository;
import com.fitwise.repository.NotifyEmailRepository;
import com.fitwise.repository.UserActiveInactiveTrackerRepository;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.UserRoleRepository;
import com.fitwise.repository.VideoManagementRepo;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.payments.stripe.connect.AppleSettlementByStripeRepository;
import com.fitwise.repository.payments.stripe.connect.StripeAccountAndUserMappingRepository;
import com.fitwise.repository.payments.stripe.connect.StripeTransferAndReversalMappingRepository;
import com.fitwise.repository.payments.stripe.paypal.UserAccountAndPayPalIdMappingRepository;
import com.fitwise.repository.subscription.PackageSubscriptionRepository;
import com.fitwise.repository.subscription.ProgramSubscriptionRepo;
import com.fitwise.service.NotificationTriggerService;
import com.fitwise.service.instructor.InstructorAnalyticsService;
import com.fitwise.service.payment.authorizenet.PaymentService;
import com.fitwise.specifications.jpa.UserRoleMappingJPA;
import com.fitwise.utils.EmailContentUtil;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.ValidationUtils;
import com.fitwise.utils.mail.MailSender;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Topup;
import com.stripe.model.Transfer;
import com.stripe.model.TransferReversal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    @Autowired
    private GeneralProperties generalProperties;

    /**
     * Accessing the video management table
     */
    @Autowired
    private VideoManagementRepo videoManagementRepo;

    /**
     * Vimeo service to get vimeo details
     */
    @Autowired
    private VimeoService vimeoService;

    @Autowired
    UserActiveInactiveTrackerRepository userActiveInactiveTrackerRepository;

    @Autowired
    private NotifyEmailRepository notifyEmailRepository;

    @Autowired
    private MailSender mailSender;

    @Autowired
    NotificationTriggerService notificationTriggerService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    private StripeProperties stripeProperties;

    @Autowired
    private StripeAccountAndUserMappingRepository stripeAccountAndUserMappingRepository;

    @Autowired
    private AppleSettlementByStripeRepository appleSettlementByStripeRepository;

    @Autowired
    private FitwiseUtils fitwiseUtils;
    @Autowired
    StripeTransferAndReversalMappingRepository stripeTransferAndReversalMappingRepository;

    private final UserRoleRepository userRoleRepository;

    private final UserRepository userRepository;

    private final EmailContentUtil emailContentUtil;

    private final ProgramSubscriptionRepo programSubscriptionRepo;

    private final PackageSubscriptionRepository packageSubscriptionRepository;

    private final UserAccountAndPayPalIdMappingRepository userAccountAndPayPalIdMappingRepository;
    
    private final InstructorAnalyticsService instructorAnalyticsService;

    private final InstructorOutstandingPaymentRepository instructorOutstandingPaymentRepository;

    private final InstructorPaymentRepository instructorPaymentRepository;

    private final UserRoleMappingJPA userRoleMappingJPA;

    /**
     * Method used to monitor user activity
     * Schedule will be at 7.00am server time
     */
    @Scheduled(cron = "0 7 0 * * ?")
    public void monitorUserActiveness() {
        updateUserActivenessTracking();
    }

    private void updateUserActivenessTracking() {

        LocalDate today = LocalDate.now();
        LocalDate fiveDaysAgo = today.plusDays(-5);
        LocalDateTime fiveDaysAgoTime = fiveDaysAgo.atStartOfDay();
        Date fiveDaysAgoTimeDate = Date.from(fiveDaysAgoTime.atZone(ZoneId.systemDefault()).toInstant());

        //List of users not active for 5 days are marked as inactive.
        List<UserActiveInactiveTracker> inactiveUsers = userActiveInactiveTrackerRepository.findByIsActiveAndModifiedDateLessThan(true, fiveDaysAgoTimeDate);
        for (UserActiveInactiveTracker activeUser : inactiveUsers) {
            activeUser.setActive(false);
            Date lastOnlineDate = activeUser.getModifiedDate();
            activeUser.setModifiedDate(lastOnlineDate);
            userActiveInactiveTrackerRepository.save(activeUser);
        }

    }

    /**
     * Method used to send list of recent sign-ups as mail.
     * <p>
     * Scheduler invoked at 07:05:01 am EST - everyday
     * Scheduler invoked at 11:05:01 am server time - everyday
     * Scheduler invoked at 4:35:01 pm IST - everyday
     */
    @Scheduled(cron = "1 5 11 * * * ")
    public void invokedScheduleNotifications() {
        notificationTriggerService.invokeScheduledNotifications();
    }

    /**
     * Method used to send list of recent sign-ups as mail.
     * <p>
     * Scheduler invoked at 07:01:01 am EST - everyday
     * Scheduler invoked at 11:01:01 am server time - everyday
     * Scheduler invoked at 4:31:01 pm IST - everyday
     */
    //@Scheduled(cron = "1 1 */4 * * * ")
    public void mailLeadTackingData() {

        log.info("Notify email collection started : " + new Date());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = calendar.getTime();

        //Yesterday's first second
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startTime = calendar.getTime();

        //Yesterday's last second
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endTime = calendar.getTime();

        List<NotifyEmail> yesterdaySignups = notifyEmailRepository.findByCreatedDateBetween(startTime, endTime);

        List<NotifyEmail> newInstructors = yesterdaySignups.stream()
                .filter(signup -> signup.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR))
                .collect(Collectors.toList());

        List<NotifyEmail> newMembers = yesterdaySignups.stream()
                .filter(signup -> signup.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER))
                .collect(Collectors.toList());

        String to = generalProperties.getSignUpTrackingMailToAddr();
        String cc = generalProperties.getSignUpTrackingMailCcAddr();

        String subject = "Fitwise : New sign up on " + simpleDateFormat.format(yesterday);
        String mailBody;
        StringBuilder tempBody = new StringBuilder();

        if (!newInstructors.isEmpty()) {
            tempBody.append("New Instructor Sign up : ");
            tempBody.append("\n");

            for (int i = 0; i < newInstructors.size(); i++) {
                NotifyEmail newInstructor = newInstructors.get(i);
                tempBody.append(i + 1).append(") ");
                tempBody.append(newInstructor.getEmail());
                tempBody.append("\n");
            }
            tempBody.append("\n");
        }

        if (!newMembers.isEmpty()) {
            tempBody.append("New Member Sign up : ");
            tempBody.append("\n");

            for (int i = 0; i < newMembers.size(); i++) {
                NotifyEmail newMember = newMembers.get(i);
                tempBody.append(i + 1).append(") ");
                tempBody.append(newMember.getEmail());
                tempBody.append("\n");
            }
        }

        mailBody = tempBody.toString();

        if (mailBody.isEmpty()) {
            mailBody = "No new sign up for the day";
        }

        List<String> fileList = null;
        String attachmentFile = "/tmp/SignUpList.xlsx";
        try {
            generateSheetForSignupData(attachmentFile);
            fileList = Arrays.asList(new String[]{attachmentFile});
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        }

        mailSender.sendReminderAttachmentMail(to, cc, subject, mailBody, fileList);

        log.info("Notify email collection ended : " + new Date());
    }


    /**
     * Every 15 minutes video management will collect data and update the vimeo upload status
     *
     * @throws InterruptedException
     */
    @Scheduled(cron = "1 */10 * * * *")
    public void validateAndUpdateVimeoStatus() throws InterruptedException {
        log.info("Vimeo processing");
        int pageNumber = 0;
        int recordCount = 50;
        List<VideoManagement> videoManagementList;
        do {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            videoManagementList = videoManagementRepo.findByUploadStatusIsNullOrUploadStatusNotIn(StringArrayConstants.VIDEO_UPLOAD_ONCE_STATUS, PageRequest.of(pageNumber, recordCount));
            for (VideoManagement videoManagement : videoManagementList) {
                try {
                    String thumbnailUrl = "";
                    int videoDuration = 0;
                    try {
                        JSONObject vimeoStatusObject = vimeoService.getVimeoStatus(videoManagement.getUrl());
                        videoDuration = (int) vimeoStatusObject.get(KeyConstants.KEY_VIMEO_DURATION);
                        JSONObject videoThumbNailObject = (JSONObject) vimeoStatusObject.get(KeyConstants.KEY_PICTURES);
                        JSONArray picturesWithSizes = (JSONArray) videoThumbNailObject.get(KeyConstants.KEY_SIZES);
                        for (int i = 0; i < picturesWithSizes.length(); i++) {
                            JSONObject json = picturesWithSizes.getJSONObject(i);
                            if (json.getInt(KeyConstants.KEY_WIDTH) == KeyConstants.KEY_THUMBS_640) {
                                thumbnailUrl = json.getString(KeyConstants.KEY_VIMEO_LINK);
                                break;
                            }
                        }
                    } catch (Exception exception) {
                        log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
                    }
                    if (!thumbnailUrl.isEmpty() && videoDuration > 0) {
                        if (videoManagement.getThumbnail() != null && videoManagement.getThumbnail().getImageId() != null) {
                            if (ValidationUtils.isEmptyString(videoManagement.getThumbnail().getImagePath()))
                                videoManagement.getThumbnail().setImagePath(thumbnailUrl);
                        } else {
                            Images images = new Images();
                            images.setImagePath(thumbnailUrl);
                            videoManagement.setThumbnail(images);
                        }
                        videoManagement.setDuration(videoDuration);
                        videoManagement.setUploadStatus(VideoUploadStatus.COMPLETED);
                        videoManagementRepo.save(videoManagement);
                    } else {
                        // If the video is still in processing state for more than 3 hours, marking it as upload state
                        if (videoManagement.getModifiedDate() != null) {
                            log.info("Diff : " + diffInHrs(videoManagement.getModifiedDate(), new Date()));
                            if (diffInHrs(videoManagement.getModifiedDate(), new Date()) > 180) {
                                /**
                                 * If the video processing was failed in Vimeo for first time, marking it as Vimeo Processing failed
                                 * If the video processing was failed more than one time, marking it as Vimeo re-upload processing failed
                                 */
                                if (videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.INPROGRESS)) {
                                    videoManagement.setUploadStatus(VideoUploadStatus.VIMEO_PROCESSING_FAILED);
                                } else if (videoManagement.getUploadStatus().equalsIgnoreCase(VideoUploadStatus.REUPLOAD_INPROGRESS)) {
                                    videoManagement.setUploadStatus(VideoUploadStatus.VIMEO_REUPLOAD_PROCESSING_FAILED);
                                }
                                videoManagementRepo.save(videoManagement);
                            }
                        }
                    }
                } catch (Exception exception) {
                    log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
                }
            }
            pageNumber++;
            stopWatch.stop();
            if (stopWatch.getTotalTimeSeconds() < 60) {
                Thread.sleep((long) (60 - stopWatch.getTotalTimeSeconds()) * 1000);
            }
        } while (videoManagementList.size() == 50);
    }

    /**
     * Calculating the time difference between two dates in hours
     *
     * @param startDate - Video management entity creation time
     * @param endDate   - Current time
     * @return
     */
    private int diffInHrs(Date startDate, Date endDate) {
        long duration = endDate.getTime() - startDate.getTime();
        long diffInHours = TimeUnit.MILLISECONDS.toMinutes(duration);
        return Math.toIntExact(diffInHours);
    }

    private void generateSheetForSignupData(String attachmentFileName) {
        boolean isDateAvailable = false;
        List<NotifyEmail> totalSignUps = notifyEmailRepository.findAll();
        if (!totalSignUps.isEmpty()) {
            isDateAvailable = true;
        }
        log.info("Total sign ups : " + totalSignUps.size() + " On " + new Date());
        Workbook workbook = null;
        FileOutputStream fileOut = null;
        try {
            if (isDateAvailable) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(StringConstants.PATTERN_DATE);
                workbook = new XSSFWorkbook();
                List<NotifyEmail> instructorSignUps = totalSignUps.stream()
                        .filter(signup -> signup.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_INSTRUCTOR))
                        .collect(Collectors.toList());
                List<NotifyEmail> memberSignUps = totalSignUps.stream()
                        .filter(signup -> signup.getRole().equalsIgnoreCase(SecurityFilterConstants.ROLE_MEMBER))
                        .collect(Collectors.toList());
                Map<String, List<NotifyEmail>> map = new HashMap<>();
                if (!instructorSignUps.isEmpty()) {
                    map.put(SecurityFilterConstants.ROLE_INSTRUCTOR, instructorSignUps);
                }
                if (!memberSignUps.isEmpty()) {
                    map.put(SecurityFilterConstants.ROLE_MEMBER, memberSignUps);
                }
                for (Map.Entry<String, List<NotifyEmail>> entry : map.entrySet()) {
                    String sheetName = entry.getKey() + " Sign up";
                    List<NotifyEmail> signUps = entry.getValue();
                    // Create a Sheet
                    Sheet sheet = workbook.createSheet(sheetName);
                    // Create header row
                    Row headerRow = sheet.createRow(0);
                    String columns[] = {"S.No", "EMAIL", "DATE"};
                    // Create cells
                    for (int i = 0; i < columns.length; i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(columns[i].toUpperCase());
                    }
                    // Create Other rows and cells with data
                    for (int i = 0; i < signUps.size(); i++) {
                        NotifyEmail signUp = signUps.get(i);
                        Row row = sheet.createRow(i + 1);
                        row.createCell(0).setCellValue(i + 1);
                        row.createCell(1).setCellValue(signUp.getEmail());
                        row.createCell(2).setCellValue(simpleDateFormat.format(signUp.getCreatedDate()));
                    }
                    // Resize all columns to fit the content size
                    for (int i = 0; i < columns.length; i++) {
                        sheet.autoSizeColumn(i);
                    }
                }
                // Write the output to a file
                fileOut = new FileOutputStream(attachmentFileName);
                workbook.write(fileOut);
            }
        } catch (Exception exception) {
            log.info(MessageConstants.MSG_ERR_EXCEPTION + exception.getMessage());
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException ioException) {
                log.info("IO Exception : " + ioException.getMessage());
            }
        }
    }

    /**
     * Method used to check the current status of transactions in Authorize.net
     * <p>
     * Scheduler invoked at 11.30 AM server time UTC - everyday
     * Scheduler invoked at 4:30 am server time PDT - everyday
     * Scheduler invoked at 5:00 pm server time IST - everyday
     */
    @Scheduled(cron = "1 30 11 * * * ")
    //@Scheduled(cron = "1 35 18 * * * ") // Local 06.35 pm
    public void checkAndUpdateAuthNetTransactionStatus() {
        log.info("*************************************************************Settlement scheduler started*****************************************************");
        paymentService.checkAndUpdateAuthNetTransactionStatus();
        log.info("Settlement scheduler end*****************************************************************************************************************");
    }


    /**
     * Method used to pay instructors their share for the programs subscribed via Apple payments
     * Since we didn't have any webhook event for payment settlement state from Apple, we will assume 45 days from
     * subscribed date as settlement date to Fitwise bank account.
     * <p>
     * Scheduler invoked at 1:30 AM server time UTC - everyday
     * Scheduler invoked at 5:30 AM server time PDT - everyday
     * Scheduler invoked at 7:00 pm server time IST - everyday
     */
    /*@Scheduled(cron = "1 41 21 * * * ")*/
    public void processInstructorApplePayments() {
        // Fetching the transactions to be paid out to instructor
        List<InstructorPayment> pendingInstructorPayments = instructorPaymentRepository.findByIsTransferDoneFalseAndIsTopUpInitiatedFalse();
        for (InstructorPayment instructorPayment : pendingInstructorPayments) {
            // Checking whether the order is subscribed via iOS platform
            if (instructorPayment.getOrderManagement().getSubscribedViaPlatform().getPlatformTypeId() == 2) {
                //Checking whether today's date is due date
                if (fitwiseUtils.isSameDay(instructorPayment.getDueDate(), new Date(), null)) {

                    StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.
                            findByUserUserId(instructorPayment.getOrderManagement().getProgram().getOwner().getUserId());

                    //Checking whether the instructor is on-boarded in stripe
                    if (stripeAccountAndUserMapping != null) {
                        Stripe.apiKey = stripeProperties.getApiKey();

                        Map<String, Object> params = new HashMap<>();
                        int instructorShare = (int) (instructorPayment.getInstructorShare() * 100);
                        //This value must be greater than or equal to 1.; else will throw error
                        // code: parameter_invalid_integer;
                        if (instructorShare <= 1) {
                            instructorShare = 1;
                        }
                        params.put(KeyConstants.KEY_AMOUNT, instructorShare); //TODO
                        params.put(StripeConstants.STRIPE_PROP_CURRENCY, "usd");
                        params.put(StringConstants.JSON_PROPERTY_KEY_DESCRIPTION, instructorPayment.getOrderManagement().getOrderId());
                        //TODO TO FILL THIS DATA
                        // com.stripe.exception.InvalidRequestException: The statement descriptor must be at most 15 characters
                        //params.put("statement_descriptor", "Top-up for" + instructorPayment.getOrderManagement().getOrderId());
                        AppleSettlementByStripe appleSettlementByStripe = new AppleSettlementByStripe();

                        try {
                            /**
                             * Topping up Fitwise stripe account from Fitwise bank account
                             */
                            Topup topup = Topup.create(params);
                            if (topup != null) {
                                appleSettlementByStripe.setInstructorPayment(instructorPayment);
                                appleSettlementByStripe.setStripeTopUpId(topup.getId());
                                appleSettlementByStripe.setStripeTopUpStatus(topup.getStatus());
                                appleSettlementByStripeRepository.save(appleSettlementByStripe);

                                instructorPayment.setIsTopUpInitiated(true);
                                instructorPaymentRepository.save(instructorPayment);
                            }
                        } catch (StripeException stripeException) {
                            instructorPayment.setIsTransferDone(false);
                            instructorPayment.setIsTransferFailed(true);
                            instructorPaymentRepository.save(instructorPayment);

                            appleSettlementByStripe.setErrorCode(stripeException.getCode());
                            appleSettlementByStripe.setErrorMessage(stripeException.getMessage());
                            appleSettlementByStripe.setStripeErrorCode(stripeException.getStripeError().getCode());
                            appleSettlementByStripe.setStripeErrorMessage(stripeException.getStripeError().getMessage());
                            appleSettlementByStripeRepository.save(appleSettlementByStripe);
                            log.error("Error in creating Top-up ---------> " + stripeException.getMessage());
                            log.info("Stripe Exception : " + stripeException.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Scheduler task to re-initiate failed reverse transfers
     * <p>
     * Scheduler invoked at 9:40 PM server time UTC - everyday
     * Scheduler invoked at 2:40 PM server time PDT - everyday
     * Scheduler invoked at 3:10 AM server time IST - everyday
     */
    /*
    * Scheduler removed - since credits are involved, reversals should not be re-initiated
    * */
    /*@Scheduled(cron = "1 40 21 * * * ")*/
    public void reinitiateFailedReverseTransfers() {
        List<StripeTransferAndReversalMapping> stripeTransferAndReversalMappingList = stripeTransferAndReversalMappingRepository.findByStatus(KeyConstants.KEY_FAILURE);

        for (StripeTransferAndReversalMapping stripeTransferAndReversalMapping : stripeTransferAndReversalMappingList) {
            try {
                Transfer transfer = Transfer.retrieve(stripeTransferAndReversalMapping.getStripeTransferId());

                Map<String, Object> params = new HashMap<>();
                params.put(KeyConstants.KEY_AMOUNT, (int) (stripeTransferAndReversalMapping.getReversalAmount() * 100));

                TransferReversal transferReversal = transfer.getReversals().create(params);

                stripeTransferAndReversalMapping.setStripeTransferReversalId(transferReversal.getId());
                stripeTransferAndReversalMapping.setStatus(KeyConstants.KEY_SUCCESS);
                stripeTransferAndReversalMappingRepository.save(stripeTransferAndReversalMapping);
            } catch (StripeException stripeException) {
                log.error("Exception while reversing transfer id = " + stripeTransferAndReversalMapping.getStripeTransferId() + " : " + stripeException.getMessage());
                log.error("Stripe error message " + stripeException.getStripeError().getMessage());
            }
        }
    }

    /**
     * Stripe connect onboarding reminder mails
     *
     * Scheduler invoked at 7:00 AM server time UTC - everyday
     */
    @Scheduled(cron = "0 0 7 * * MON")
    public void sendStripeOnboardReminderMail() {
        List<String> statusList = new ArrayList<>();
        statusList.add(KeyConstants.KEY_PAID);
        statusList.add(KeyConstants.KEY_PAYMENT_PENDING);

        UserRole roleInstructor = userRoleRepository.findByName(SecurityFilterConstants.ROLE_INSTRUCTOR);
        List<User> instructors = userRepository.findByUserRoleMappingsUserRole(roleInstructor);
        for (User instructor : instructors) {

            //All subscriptions of instructor
            //If the instructor has atleast one subscription
            List<ProgramSubscription> totalProgramSubscriptions = programSubscriptionRepo.findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(instructor.getUserId(), statusList);
            List<PackageSubscription> totalPackageSubscriptions = packageSubscriptionRepository.findBySubscriptionPackageOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(instructor.getUserId(), statusList);
            if (!totalProgramSubscriptions.isEmpty() || !totalPackageSubscriptions.isEmpty()) {

                boolean isOnboardingDetailsSubmitted = false;
                StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
                if (stripeAccountAndUserMapping != null && stripeAccountAndUserMapping.getIsDetailsSubmitted() != null && stripeAccountAndUserMapping.getIsDetailsSubmitted()) {
                    isOnboardingDetailsSubmitted = true;
                }

                boolean isOnBoardedViaPayPal = false;
                UserAccountAndPayPalIdMapping userAccountAndPayPalIdMapping = userAccountAndPayPalIdMappingRepository.findByUserUserId(instructor.getUserId());
                if (userAccountAndPayPalIdMapping != null && !userAccountAndPayPalIdMapping.getPayPalId().isEmpty()) {
                    isOnBoardedViaPayPal = true;
                }

                if (!isOnboardingDetailsSubmitted && !isOnBoardedViaPayPal) {
                    String userName = fitwiseUtils.getUserFullName(instructor);
                    String subject = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_SUBJECT;
                    String trainnrDashboard = EmailConstants.STRIPE_CONNECT_ONBOARD_DASHBOARD_LINK.replace(EmailConstants.LITERAL_APP_URL, generalProperties.getInstructorBaseUrl() + RestUrlConstants.APP_INSTRUCTOR_DASHBOARD);
                    String mailBody = EmailConstants.STRIPE_CONNECT_ONBOARD_REMINDER_CONTENT;
                    mailBody = EmailConstants.BODY_HTML_TEMPLATE_WITH_BUTTON.replace(EmailConstants.EMAIL_GREETINGS, "Hi " + userName + ",")
                            .replace(EmailConstants.EMAIL_BODY, mailBody)
                            .replace(EmailConstants.EMAIL_SUPPORT_URL, trainnrDashboard);
                    mailBody = emailContentUtil.replaceInstructorAppUrl(mailBody);
                    mailSender.sendHtmlReminderMail(instructor.getEmail(), subject, mailBody);
                }

            }
        }

    }

    /**
     * Task to update Outstanding Balance of every instructor
     * Task runs every 30 min
     */
    @Scheduled(cron = "1 */30 * * * *")
    public void updateOutstandingBalance() {
        log.info("updateOutstandingBalance scheduler starts");
        List<User> instructorList = userRoleMappingJPA.getUsersByRoleName(KeyConstants.KEY_INSTRUCTOR);

        for (User instructor : instructorList) {
            try {
                StripeAccountAndUserMapping stripeAccountAndUserMapping = stripeAccountAndUserMappingRepository.findByUserUserId(instructor.getUserId());
                if (stripeAccountAndUserMapping != null && !stripeAccountAndUserMapping.getStripeAccountId().isEmpty()) {
                    /*Stripe connect onboarded instructor*/

                    double upcomingPayment = instructorAnalyticsService.calculateOutstandingPaymentOfAnInstructor(instructor.getUserId(), null);

                    InstructorOutstandingPayment instructorOutstandingPayment = instructorOutstandingPaymentRepository.findByUser(instructor);
                    if (instructorOutstandingPayment == null) {
                        instructorOutstandingPayment = new InstructorOutstandingPayment();
                        instructorOutstandingPayment.setUser(instructor);
                    }
                    instructorOutstandingPayment.setOutstandingPayment(upcomingPayment);
                    instructorOutstandingPaymentRepository.save(instructorOutstandingPayment);
                } else {
                    /*Instructors who have not onboarded stripe*/
                    InstructorOutstandingPayment instructorOutstandingPayment = instructorOutstandingPaymentRepository.findByUser(instructor);
                    if (instructorOutstandingPayment == null) {
                        /*InstructorOutstandingPayment updated for first time*/
                        double upcomingPayment = instructorAnalyticsService.calculateOutstandingPaymentOfAnInstructor(instructor.getUserId(), null);
                        instructorOutstandingPayment = new InstructorOutstandingPayment();
                        instructorOutstandingPayment.setUser(instructor);
                        instructorOutstandingPayment.setOutstandingPayment(upcomingPayment);
                        instructorOutstandingPaymentRepository.save(instructorOutstandingPayment);
                    } else {
                        /*InstructorOutstandingPayment updated if any paid subscription was done after last update*/
                        Date lastUpdateTime = instructorOutstandingPayment.getModifiedDate();

                        InstructorPayment lastInstructorPayment = instructorPaymentRepository.findTop1ByInstructorOrderByInstructorPaymentIdDesc(instructor);
                        if (lastInstructorPayment != null && lastInstructorPayment.getOrderManagement().getCreatedDate().after(lastUpdateTime)) {
                            double upcomingPayment = instructorAnalyticsService.calculateOutstandingPaymentOfAnInstructor(instructor.getUserId(), null);
                            instructorOutstandingPayment.setOutstandingPayment(upcomingPayment);
                            instructorOutstandingPaymentRepository.save(instructorOutstandingPayment);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception while updating instructorOutstandingPayment for user : " + instructor.getUserId());
                log.error("Exception Message : " + e.getMessage());
            }
        }
        log.info("updateOutstandingBalance scheduler ends");
    }

}
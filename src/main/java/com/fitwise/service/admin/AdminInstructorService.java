package com.fitwise.service.admin;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringArrayConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.User;
import com.fitwise.entity.view.ViewInstructor;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.UserRepository;
import com.fitwise.repository.view.ViewInstructorRepository;
import com.fitwise.response.AdminListResponse;
import com.fitwise.response.InstructorResponse;
import com.fitwise.service.instructor.InstructorSubscriptionService;
import com.fitwise.specifications.view.ViewInstructorSpecification;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.utils.RequestParamValidator;
import com.fitwise.view.InstructorClientResponseView;
import com.fitwise.view.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInstructorService {

    private final UserRepository userRepository;
    private final FitwiseUtils fitwiseUtils;
    private final InstructorSubscriptionService instructorSubscriptionService;
    private final ViewInstructorRepository viewInstructorRepository;

    /**
     * Get Subscribed clients of an instructor
     *
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @param searchName
     * @param type
     * @return
     */
    public ResponseModel getSubscribedClientsOfAnInstructor(int pageNo, int pageSize, Long instructorId, Optional<String> searchName, Optional<String> type) {
        log.info("Get subscribed clients of an instructor starts.");
        long apiStartTimeMillis = new Date().getTime();
        User instructor = userRepository.findByUserId(instructorId);
        if (instructor == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, null);
        }
        if (!fitwiseUtils.isInstructor(instructor)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, null);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        InstructorClientResponseView instructorClientResponseView = instructorSubscriptionService.getSubscribedClientsOfAnInstructor(pageNo, pageSize, searchName, type, instructor);
        log.info("Get instructor client response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get subscribed clients of an instructor ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_SUBSCRIBED_CLIENTS_FETCHED, instructorClientResponseView);
    }

    /**
     * Get Subscribed clients of an instructor
     *
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @param searchName
     * @param type
     * @return
     */
    public ResponseModel getLapsedClientsOfAnInstructor(int pageNo, int pageSize, Long instructorId, Optional<String> searchName, Optional<String> type) throws ParseException {
        log.info("Get lapsed clients of an instructor starts.");
        long apiStartTimeMillis = new Date().getTime();
        User instructor = userRepository.findByUserId(instructorId);
        if (instructor == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, null);
        }
        if (!fitwiseUtils.isInstructor(instructor)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, null);
        }
        log.info("Basic validations : Time taken in millis : " + (new Date().getTime() - apiStartTimeMillis));
        long profilingEndTimeMillis = new Date().getTime();
        InstructorClientResponseView instructorClientResponseView = instructorSubscriptionService.getLapseClientsOfAnInstructor(pageNo, pageSize, searchName, type, instructor);
        log.info("Get instructor client response view : Time taken in millis : " + (new Date().getTime() - profilingEndTimeMillis));
        log.info(StringConstants.LOG_API_DURATION_TEXT + (new Date().getTime() - apiStartTimeMillis));
        log.info("Get lapsed clients of an instructor ends.");
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_LAPSE_CLIENTS_FETCHED, instructorClientResponseView);
    }

    /**
     * Get Subscribed clients of an instructor
     *
     * @param pageNo
     * @param pageSize
     * @param instructorId
     * @param searchName
     * @return
     */
    public ResponseModel getTrialClientsOfAnInstructor(int pageNo, int pageSize, Long instructorId, Optional<String> searchName) throws ParseException {
        User instructor = userRepository.findByUserId(instructorId);
        if (instructor == null) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_FOUND, null);
        }
        if (!fitwiseUtils.isInstructor(instructor)) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_USER_NOT_INSTRUCTOR, null);
        }
        InstructorClientResponseView instructorClientResponseView = instructorSubscriptionService.getTrialClientsOfAnInstructor(pageNo, pageSize, searchName, instructor);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_TRIAL_CLIENTS_FETCHED, instructorClientResponseView);
    }

    /**
     * Get admin instructor L1 details
     * @param pageNo
     * @param pageSize
     * @param sortOrder
     * @param sortBy
     * @param searchName
     * @param blockStatus
     * @return
     */
    public AdminListResponse<InstructorResponse> getInstructor(final int pageNo, final int pageSize, String sortOrder, String sortBy, Optional<String> searchName, String blockStatus) {
        long apiStartTimeMillis = new Date().getTime();
        long temp = new Date().getTime();
        log.info("Admin instructor L1 starts.");
        RequestParamValidator.pageSetup(pageNo, pageSize);
        RequestParamValidator.sortList(StringArrayConstants.SORT_ADMIN_INSTRUCTOR, sortBy, sortOrder);
        if (blockStatus == null || blockStatus.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        }
        if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)
            || blockStatus.equalsIgnoreCase(DBConstants.TIER_FREE) || blockStatus.equalsIgnoreCase(DBConstants.TIER_SILVER) || blockStatus.equalsIgnoreCase(DBConstants.TIER_GOLDEN))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }
        log.info("Field validation : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Specification<ViewInstructor> instructorSpecification = ViewInstructorSpecification.getInstructorSortSpecification(sortBy, sortOrder);
        if(blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)){
            instructorSpecification = instructorSpecification.and(ViewInstructorSpecification.getBlockStatusSpecification(true));
        }else if(blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)){
            instructorSpecification = instructorSpecification.and(ViewInstructorSpecification.getBlockStatusSpecification(false));
        } else if (blockStatus.equalsIgnoreCase(DBConstants.TIER_FREE) || blockStatus.equalsIgnoreCase(DBConstants.TIER_SILVER) || blockStatus.equalsIgnoreCase(DBConstants.TIER_GOLDEN)) {
            instructorSpecification = instructorSpecification.and(ViewInstructorSpecification.getTierType(blockStatus));
        }
        if(searchName.isPresent() && !searchName.get().isEmpty()){
            Specification<ViewInstructor> searchSpecification = ViewInstructorSpecification.getSearchByNameSpecification(searchName.get()).
                    or(ViewInstructorSpecification.getSearchByEmailSpecification(searchName.get()));
            instructorSpecification = instructorSpecification.and(searchSpecification);
        }
        log.info("Spec construction : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Page<ViewInstructor> viewInstructorPage = viewInstructorRepository.findAll(instructorSpecification, PageRequest.of(pageNo - 1, pageSize));
        if(viewInstructorPage.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", "");
        }
        log.info("Get data from db : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        AdminListResponse<InstructorResponse> adminListResponse = new AdminListResponse<>();
        adminListResponse.setTotalSizeOfList(viewInstructorPage.getTotalElements());
        List<InstructorResponse> instructorResponses = new ArrayList<>();
        for(ViewInstructor viewInstructor : viewInstructorPage.getContent()){
            InstructorResponse instructorResponse = new InstructorResponse();
            instructorResponse.setUserId(viewInstructor.getUserId());
            instructorResponse.setInstructorName(viewInstructor.getName());
            instructorResponse.setEmail(viewInstructor.getEmail());
            if(!StringUtils.isEmpty(viewInstructor.getImageUrl())){
                instructorResponse.setImageUrl(viewInstructor.getImageUrl());
            }
            double upcomingPayment = viewInstructor.getOutstandingBalance() != null ? viewInstructor.getOutstandingBalance() : 0.0;
            instructorResponse.setUpcomingPayment(upcomingPayment);
            instructorResponse.setUpcomingPaymentFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(upcomingPayment));
            instructorResponse.setPublishedPackageCount(viewInstructor.getPublishedPackages());
            instructorResponse.setPackageSubscriptionCount(viewInstructor.getActivePackageSubscriptions());
            instructorResponse.setPublishedProgram(viewInstructor.getPublishedPrograms());
            instructorResponse.setTotalSubscription(viewInstructor.getActiveProgramSubscriptions());
            instructorResponse.setTotalExercises(viewInstructor.getExercises());
            instructorResponse.setOnboardedDate(viewInstructor.getOnboardedOn());
            instructorResponse.setOnboardedDateFormatted(fitwiseUtils.formatDate(viewInstructor.getOnboardedOn()));
            instructorResponse.setBlocked(viewInstructor.getBlocked());
            instructorResponse.setOnboardedPaymentMode(viewInstructor.getOnboardedPaymentMode());
            instructorResponse.setLastAccess(viewInstructor.getLastUserAccess());
            instructorResponse.setLastAccessFormatted(fitwiseUtils.formatDate(viewInstructor.getLastUserAccess()));
            if(viewInstructor.getTierType() != null ){
                instructorResponse.setTier(viewInstructor.getTierType());
            }else{
                instructorResponse.setTier("Not Subscribed");
            }
            instructorResponses.add(instructorResponse);
        }
        log.info("Response construction : " + (new Date().getTime() - temp));
        adminListResponse.setPayloadOfAdmin(instructorResponses);
        log.info("Admin instructor L1 ends."  + (new Date().getTime() - apiStartTimeMillis));
        return adminListResponse;
    }

    /**
     * Export all the instructors details
     * @param sortOrder
     * @param sortBy
     * @param searchName
     * @param blockStatus
     * @return
     */
    public ByteArrayInputStream export(String sortOrder, String sortBy, Optional<String> searchName, String blockStatus) {
        long apiStartTimeMillis = new Date().getTime();
        long temp = new Date().getTime();
        RequestParamValidator.sortList(StringArrayConstants.SORT_ADMIN_INSTRUCTOR, sortBy, sortOrder);
        if (blockStatus == null || blockStatus.isEmpty()) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_NULL, null);
        }
        if (!(blockStatus.equalsIgnoreCase(KeyConstants.KEY_ALL) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN) || blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)
                || blockStatus.equalsIgnoreCase(DBConstants.TIER_FREE) || blockStatus.equalsIgnoreCase(DBConstants.TIER_SILVER) || blockStatus.equalsIgnoreCase(DBConstants.TIER_GOLDEN))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_BLOCK_STATUS_PARAM_INCORRECT, null);
        }
        log.info("Field validation : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        Specification<ViewInstructor> instructorSpecification = ViewInstructorSpecification.getInstructorSortSpecification(sortBy, sortOrder);
        if(blockStatus.equalsIgnoreCase(KeyConstants.KEY_BLOCKED)){
            instructorSpecification = instructorSpecification.and(ViewInstructorSpecification.getBlockStatusSpecification(true));
        }else if(blockStatus.equalsIgnoreCase(KeyConstants.KEY_OPEN)){
            instructorSpecification = instructorSpecification.and(ViewInstructorSpecification.getBlockStatusSpecification(false));
        } else if (blockStatus.equalsIgnoreCase(DBConstants.TIER_FREE) || blockStatus.equalsIgnoreCase(DBConstants.TIER_SILVER) || blockStatus.equalsIgnoreCase(DBConstants.TIER_GOLDEN)) {
            instructorSpecification = instructorSpecification.and(ViewInstructorSpecification.getTierType(blockStatus));
        }
        if(searchName.isPresent() && !searchName.get().isEmpty()){
            Specification<ViewInstructor> searchSpecification = ViewInstructorSpecification.getSearchByNameSpecification(searchName.get()).
                    or(ViewInstructorSpecification.getSearchByEmailSpecification(searchName.get()));
            instructorSpecification = instructorSpecification.and(searchSpecification);
        }
        log.info("Spec construction : " + (new Date().getTime() - temp));
        temp = new Date().getTime();
        List<ViewInstructor> instructorList = viewInstructorRepository.findAll(instructorSpecification);
        log.info("Get data from db : " + (new Date().getTime() - temp));
        if(instructorList.isEmpty()){
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "", "");
        }
        temp = new Date().getTime();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);
        ByteArrayInputStream byteArrayInputStream;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
            csvPrinter.printRecord(ExportConstants.EXPORT_HEADER_INSTRUCTOR);
            for (ViewInstructor viewInstructor : instructorList) {
                List<java.io.Serializable> rowData = new ArrayList<>();
                // Instructor Name
                rowData.add(viewInstructor.getName() != null ? viewInstructor.getName() : "");
                // Blocked
                String userBlockStatus = (viewInstructor.getBlocked() != null && viewInstructor.getBlocked()) ? KeyConstants.KEY_BLOCKED : KeyConstants.KEY_UNBLOCKED;
                rowData.add(userBlockStatus);
                // Instructor Payout Mode
                rowData.add(viewInstructor.getOnboardedPaymentMode());
                // Total Outstanding
                double outstandingBalance = viewInstructor.getOutstandingBalance() != null ? viewInstructor.getOutstandingBalance() : 0.0;
                rowData.add(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(outstandingBalance));
                // Packages Published
                rowData.add(viewInstructor.getPublishedPackages());
                // Packages Subscriptions
                rowData.add(viewInstructor.getActivePackageSubscriptions());
                // Programs Published
                rowData.add(viewInstructor.getPublishedPrograms());
                // Programs Subscriptions
                rowData.add(viewInstructor.getActiveProgramSubscriptions());
                // Total Exercises
                rowData.add(viewInstructor.getExercises());
                // Onboarded On
                rowData.add(fitwiseUtils.formatDate(viewInstructor.getOnboardedOn()));
                // Last Active
                rowData.add(fitwiseUtils.formatDate(viewInstructor.getLastUserAccess()));
                rowData.add(viewInstructor.getEmail());
                rowData.add(viewInstructor.getContactNumber());
                rowData.add(viewInstructor.getTierType());
                csvPrinter.printRecord(rowData);
            }
            csvPrinter.flush();
            byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
            log.info("Response construction " + (new Date().getTime() - temp));
        } catch (Exception e) {
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAYOUTS_CSV_GENERATION_FAILED, MessageConstants.ERROR);
        }
        log.info("Response construction : " + (new Date().getTime() - temp));
        log.info("Admin instructor Export."  + (new Date().getTime() - apiStartTimeMillis));
        return byteArrayInputStream;
    }
}

package com.fitwise.service.payment.stripe;

import com.fitwise.constants.Constants;
import com.fitwise.constants.DBConstants;
import com.fitwise.constants.ExportConstants;
import com.fitwise.constants.KeyConstants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.constants.SearchConstants;
import com.fitwise.constants.StringConstants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.PlatformTypeRepository;
import com.fitwise.repository.order.InstructorPaymentRepository;
import com.fitwise.repository.payments.stripe.billing.StripePaymentRepository;
import com.fitwise.specifications.jpa.InstructorPaymentJPA;
import com.fitwise.specifications.jpa.dao.PayoutDao;
import com.fitwise.utils.FitwiseUtils;
import com.fitwise.view.payment.stripe.admin.PayoutsTileResponseView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Created by Vignesh.G on 29/06/21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminPayoutService {

    private final InstructorPaymentJPA instructorPaymentJPA;
    private final PlatformTypeRepository platformTypeRepository;
    private final FitwiseUtils fitwiseUtils;
    private final InstructorPaymentRepository instructorPaymentRepository;
    private final StripePaymentRepository stripePaymentRepository;

    /**
     * @param pageNo
     * @param pageSize
     * @param filterType
     * @param sortBy
     * @param sortOrder
     * @param platform
     * @param search
     * @return
     */
    public Map<String, Object> getPayouts(int pageNo, int pageSize, String filterType, String sortBy, String sortOrder, String platform, Optional<String> search) {
        log.info("getPayouts starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        if (pageNo <= 0 || pageSize <= 0) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ERROR, null);
        }
        List<String> sortByList = Arrays.asList(new String[]{SearchConstants.DUE_DATE, SearchConstants.PAID_DATE, SearchConstants.PLATFORM, SearchConstants.INSTRUCTOR_SHARE, SearchConstants.USER_EMAIL});
        boolean isSortValid = sortByList.stream().anyMatch(sortBy::equalsIgnoreCase);
        if (!isSortValid) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(SearchConstants.ORDER_DSC.equalsIgnoreCase(sortOrder) || SearchConstants.ORDER_ASC.equalsIgnoreCase(sortOrder))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        List<String> filterTypeList = Arrays.asList(new String[]{KeyConstants.KEY_ALL, KeyConstants.KEY_PAID, KeyConstants.KEY_FAILURE, SearchConstants.NOT_PAID, KeyConstants.KEY_PROCESSING});
        boolean isfilterTypeFound = filterTypeList.stream().anyMatch(filterType::equalsIgnoreCase);
        if (!isfilterTypeFound) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_PAYOUT_FILTER_TYPE, null);
        }
        if (!(DBConstants.IOS.equalsIgnoreCase(platform) || SearchConstants.ANDROID_AND_WEB.equalsIgnoreCase(platform))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_PAYOUT_PLATFORM, null);
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        List<PlatformType> platformTypeList = new ArrayList<>();
        if (platform.equalsIgnoreCase(DBConstants.IOS)) {
            PlatformType iOSPlatform = platformTypeRepository.findByPlatform(DBConstants.IOS);
            platformTypeList.add(iOSPlatform);
        } else {
            PlatformType androidPlatform = platformTypeRepository.findByPlatform(DBConstants.ANDROID);
            PlatformType webPlatform = platformTypeRepository.findByPlatform(DBConstants.WEB);
            platformTypeList.add(androidPlatform);
            platformTypeList.add(webPlatform);
        }
        List<Long> platformTypeIdList = platformTypeList.stream().map(PlatformType::getPlatformTypeId).collect(Collectors.toList());

        List<PayoutDao> payoutDaoList = instructorPaymentJPA.getPayouts(pageNo - 1, pageSize, platformTypeIdList, filterType, search, sortBy, sortOrder);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        if (payoutDaoList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        Map<String, Boolean> refundMap = getRefundStatus(payoutDaoList, platform);

        profilingStartTimeMillis = System.currentTimeMillis();
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);
        List<PayoutsTileResponseView> payouts = new ArrayList<>();
        for (PayoutDao payoutDao : payoutDaoList) {
            PayoutsTileResponseView payoutsTileResponseView = new PayoutsTileResponseView();
            payoutsTileResponseView.setSubscriptionType(payoutDao.getSubscriptionType());
            payoutsTileResponseView.setInstructorPaymentId(payoutDao.getInstructorPaymentId());

            String instructorName = payoutDao.getInstructorName() != null ? payoutDao.getInstructorName() : KeyConstants.KEY_ANONYMOUS;
            payoutsTileResponseView.setInstructorName(instructorName);
            payoutsTileResponseView.setInstructorShare(decimalFormat.format(payoutDao.getInstructorShare()));
            payoutsTileResponseView.setInstructorShareFormatted(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(payoutDao.getInstructorShare()));
            payoutsTileResponseView.setDueDate(fitwiseUtils.formatDate(payoutDao.getDueDate()));
            payoutsTileResponseView.setDueDateTimeStamp(payoutDao.getDueDate());
            if (KeyConstants.KEY_TRANSFER_FAILED.equalsIgnoreCase(payoutDao.getStripeTransferStatus())) {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_FAILURE);
            } else if (payoutDao.getIsTransferDone() != null && payoutDao.getIsTransferDone().booleanValue()) {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_PAID);
                payoutsTileResponseView.setTransactionId(payoutDao.getStripeTransferId());
            } else if ((payoutDao.getIsTopUpInitiated() != null && payoutDao.getIsTopUpInitiated().booleanValue()) && (payoutDao.getIsTransferDone() != null && !payoutDao.getIsTransferDone().booleanValue()) && (payoutDao.getIsTransferFailed() != null && !payoutDao.getIsTransferFailed().booleanValue())) {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_PROCESSING);
            } else {
                payoutsTileResponseView.setStatus(KeyConstants.KEY_NOT_PAID);
            }

            /*
             * Marking 100% refund orders as Paid
             * */
            if (payoutsTileResponseView.getStatus().equals(KeyConstants.KEY_FAILURE) || payoutsTileResponseView.getStatus().equals(KeyConstants.KEY_NOT_PAID)) {
                Boolean isRefund = refundMap.get(payoutDao.getOrderId());
                if (isRefund != null && isRefund.booleanValue()) {
                    payoutsTileResponseView.setStatus(KeyConstants.KEY_REFUNDED);
                }
            }

            //Checking whether the instructor has on-boarded in stripe
            if (payoutDao.getStripeAccountId() == null) {
                payoutsTileResponseView.setInstructorPayoutMode(KeyConstants.KEY_PAYPAL);
            } else {
                payoutsTileResponseView.setInstructorPayoutMode(KeyConstants.KEY_STRIPE);
            }
            payoutsTileResponseView.setSubscribedViaPlatform(payoutDao.getPlatform());
            if (payoutDao.getTransferDate() != null) {
                payoutsTileResponseView.setPaidDate(fitwiseUtils.formatDate(payoutDao.getTransferDate()));
            }

            payoutsTileResponseView.setFailureMessage(payoutDao.getTransferError());

            payoutsTileResponseView.setPayoutPaidVia(payoutDao.getTransferMode());
            payoutsTileResponseView.setTransferBillNumber(payoutDao.getBillNumber());
            payoutsTileResponseView.setEmail(payoutDao.getEmail());

            payouts.add(payoutsTileResponseView);
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        Map<String, Object> payoutsMap = new HashMap<>();
        payoutsMap.put(KeyConstants.KEY_PAYOUTS, payouts);

        profilingStartTimeMillis = System.currentTimeMillis();
        long totalCount = instructorPaymentJPA.countOfPayouts(platformTypeIdList, filterType, search);
        payoutsMap.put(KeyConstants.KEY_TOTAL_COUNT, totalCount);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Count query 1 : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        Date now = new Date();
        long pendingInstructorPayments = instructorPaymentRepository
                .countByIsTransferDoneAndIsTopUpInitiatedAndOrderManagementSubscribedViaPlatformInAndDueDateLessThanAndInstructorShareGreaterThan(false, false, platformTypeList, now, 0);
        payoutsMap.put(KeyConstants.KEY_TOP_UP_COUNT, pendingInstructorPayments);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Count query 2 : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getPayouts ends.");
        return payoutsMap;

    }

    /**
     * Get refund status of instructor payments
     * @param payoutDaoList
     * @param platform
     * @return
     */
    private Map<String, Boolean> getRefundStatus(List<PayoutDao> payoutDaoList, String platform) {
        long profilingStartTimeMillis = System.currentTimeMillis();
        Map<String, Boolean> refundMap = new HashMap<>();
        if (platform.equalsIgnoreCase(DBConstants.IOS)) {
            for (PayoutDao payoutDao : payoutDaoList) {
                boolean isRefund = false;
                if (KeyConstants.KEY_REFUNDED.equals(payoutDao.getOrderStatus())) {
                    isRefund = true;
                }
                refundMap.put(payoutDao.getOrderId(), isRefund);
            }
        } else {
            List<String> orderIdList = payoutDaoList.stream().map(PayoutDao::getOrderId).collect(Collectors.toList());
            List<StripePayment> stripePaymentList = stripePaymentRepository.findByOrderManagementOrderIdInAndTransactionStatus(orderIdList, KeyConstants.KEY_REFUND);
            for (StripePayment stripePayment : stripePaymentList) {
                String orderId = stripePayment.getOrderManagement().getOrderId();
                boolean isRefund = false;
                double amountPaid = stripePayment.getAmountPaid() != null ? stripePayment.getAmountPaid().doubleValue() : 0;
                double refundAmount = stripePayment.getAmountRefunded() != null ? stripePayment.getAmountRefunded().doubleValue() : 0;
                if (amountPaid - refundAmount == 0) {
                    isRefund = true;
                }
                refundMap.put(orderId, isRefund);
            }
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Get Refund status : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        return refundMap;
    }

    /**
     * @param filterType
     * @param sortBy
     * @param sortOrder
     * @param platform
     * @param search
     * @return
     */
    public ByteArrayInputStream getPayoutsCsv(String filterType, String sortBy, String sortOrder, String platform, Optional<String> search) {
        log.info("getPayoutsCsv starts.");
        long apiStartTimeMillis = System.currentTimeMillis();

        long profilingStartTimeMillis = System.currentTimeMillis();
        List<String> sortByList = Arrays.asList(new String[]{SearchConstants.DUE_DATE, SearchConstants.PAID_DATE, SearchConstants.PLATFORM, SearchConstants.INSTRUCTOR_SHARE, SearchConstants.USER_EMAIL});
        boolean isSortValid = sortByList.stream().anyMatch(sortBy::equalsIgnoreCase);
        if (!isSortValid) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_ERROR_SORT_BY, null);
        }
        if (!(SearchConstants.ORDER_DSC.equalsIgnoreCase(sortOrder) || SearchConstants.ORDER_ASC.equalsIgnoreCase(sortOrder))) {
            throw new ApplicationException(Constants.BAD_REQUEST, MessageConstants.MSG_PAGINATION_ORDER_ERROR, null);
        }
        List<String> filterTypeList = Arrays.asList(new String[]{KeyConstants.KEY_ALL, KeyConstants.KEY_PAID, KeyConstants.KEY_FAILURE, SearchConstants.NOT_PAID, KeyConstants.KEY_PROCESSING});
        boolean isfilterTypeFound = filterTypeList.stream().anyMatch(filterType::equalsIgnoreCase);
        if (!isfilterTypeFound) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_PAYOUT_FILTER_TYPE, null);
        }
        if (!(DBConstants.IOS.equalsIgnoreCase(platform) || SearchConstants.ANDROID_AND_WEB.equalsIgnoreCase(platform))) {
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_INVALID_PAYOUT_PLATFORM, null);
        }
        long profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Validation : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        profilingStartTimeMillis = System.currentTimeMillis();
        List<PlatformType> platformTypeList = new ArrayList<>();
        if (platform.equalsIgnoreCase(DBConstants.IOS)) {
            PlatformType iOSPlatform = platformTypeRepository.findByPlatform(DBConstants.IOS);
            platformTypeList.add(iOSPlatform);
        } else {
            PlatformType androidPlatform = platformTypeRepository.findByPlatform(DBConstants.ANDROID);
            PlatformType webPlatform = platformTypeRepository.findByPlatform(DBConstants.WEB);
            platformTypeList.add(androidPlatform);
            platformTypeList.add(webPlatform);
        }
        List<Long> platformTypeIdList = platformTypeList.stream().map(PlatformType::getPlatformTypeId).collect(Collectors.toList());
        DecimalFormat decimalFormat = new DecimalFormat(KeyConstants.KEY_DEFAULT_PRICE_FORMAT);

        List<PayoutDao> payoutDaoList = instructorPaymentJPA.getAllPayouts(platformTypeIdList, filterType, search, sortBy, sortOrder);
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Query : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));
        if (payoutDaoList.isEmpty()) {
            throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, MessageConstants.MSG_DATA_NOT_AVAILABLE, null);
        }

        Map<String, Boolean> refundMap = getRefundStatus(payoutDaoList, platform);

        profilingStartTimeMillis = System.currentTimeMillis();

        CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);

        ByteArrayInputStream byteArrayInputStream = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {

            csvPrinter.printRecord(ExportConstants.EXPORT_HEADER_PAYOUT);
            for (PayoutDao payoutDao : payoutDaoList) {
                List rowData = new ArrayList();

                String instructorName = payoutDao.getInstructorName() != null ? payoutDao.getInstructorName() : KeyConstants.KEY_ANONYMOUS;
                /*Instructor Name*/
                rowData.add(instructorName);

                /*Instructor Payout Mode*/
                String instructorPayoutMode = payoutDao.getStripeAccountId() == null ? KeyConstants.KEY_PAYPAL : KeyConstants.KEY_STRIPE;
                rowData.add(instructorPayoutMode);

                /*Instructor Share*/
                rowData.add(KeyConstants.KEY_CURRENCY_US_DOLLAR + decimalFormat.format(payoutDao.getInstructorShare()));

                /*Status*/
                String status = null;
                String transactionId = null;
                if (KeyConstants.KEY_TRANSFER_FAILED.equalsIgnoreCase(payoutDao.getStripeTransferStatus())) {
                    status = KeyConstants.KEY_FAILURE;
                } else if (payoutDao.getIsTransferDone() != null && payoutDao.getIsTransferDone().booleanValue()) {
                    status = KeyConstants.KEY_PAID;
                    transactionId = payoutDao.getStripeTransferId();
                } else if ((payoutDao.getIsTopUpInitiated() != null && payoutDao.getIsTopUpInitiated().booleanValue()) && (payoutDao.getIsTransferDone() != null && !payoutDao.getIsTransferDone().booleanValue()) && (payoutDao.getIsTransferFailed() != null && !payoutDao.getIsTransferFailed().booleanValue())) {
                    status = KeyConstants.KEY_PROCESSING;
                } else {
                    status = KeyConstants.KEY_NOT_PAID;
                }
                //Refund status computation
                if (status.equals(KeyConstants.KEY_FAILURE) || status.equals(KeyConstants.KEY_NOT_PAID)) {
                    Boolean isRefund = refundMap.get(payoutDao.getOrderId());
                    if (isRefund != null && isRefund.booleanValue()) {
                        status = KeyConstants.KEY_REFUNDED;
                    }
                }

                /*
                * Due Date
                * set as Nil for paid payouts
                * */
                String dueDateformatted = KeyConstants.KEY_PAID.equals(status) ? KeyConstants.KEY_NIL :fitwiseUtils.formatDate(payoutDao.getDueDate());
                rowData.add(dueDateformatted);

                rowData.add(status);

                /*Paid Via*/
                rowData.add(payoutDao.getTransferMode());

                /*Transaction ID*/
                rowData.add(transactionId);

                /*Bill ID*/
                rowData.add(payoutDao.getBillNumber());
                /*Platform Type*/
                rowData.add(payoutDao.getPlatform());
                /*Paid Date*/
                rowData.add(fitwiseUtils.formatDate(payoutDao.getTransferDate()));

                /*Transfer Error*/
                rowData.add(payoutDao.getTransferError());
                /*Subscription Type*/
                rowData.add(payoutDao.getSubscriptionType());
                rowData.add(payoutDao.getEmail());

                csvPrinter.printRecord(rowData);
            }
            csvPrinter.flush();
            byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            log.info("Exception occurred : " + e.getMessage());
            throw new ApplicationException(Constants.ERROR_STATUS, MessageConstants.MSG_PAYOUTS_CSV_GENERATION_FAILED, MessageConstants.ERROR);
        }
        profilingEndTimeMillis = System.currentTimeMillis();
        log.info("Data construction : Time taken in millis : " + (profilingEndTimeMillis - profilingStartTimeMillis));

        long apiEndTimeMillis = System.currentTimeMillis();
        log.info(StringConstants.LOG_API_DURATION_TEXT + (apiEndTimeMillis - apiStartTimeMillis));
        log.info("getPayoutsCsv ends.");

        return byteArrayInputStream;
    }
}

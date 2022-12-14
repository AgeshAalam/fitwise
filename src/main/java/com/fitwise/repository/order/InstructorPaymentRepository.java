package com.fitwise.repository.order;

import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.PlatformType;
import com.fitwise.entity.User;
import com.fitwise.entity.payments.common.OrderManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InstructorPaymentRepository extends JpaRepository<InstructorPayment, Long> , JpaSpecificationExecutor<InstructorPayment> {
    InstructorPayment findByOrderManagement(final OrderManagement orderManagement);

    /**
     * NOT PAID
     * Used to fetch all the records for which payment is not yet done
     *
     * @return
     */
    List<InstructorPayment> findByIsTransferDoneFalseAndIsTopUpInitiatedFalse();

    /**
     * Outstanding amount of instructor
     * @param isTopUpInitiated
     * @param instructorId
     * @return
     */
    List<InstructorPayment> findByIsTransferDoneAndIsTopUpInitiatedAndOrderManagementSubscribedViaPlatformAndOrderManagementProgramOwnerUserId(boolean isTransferDone, boolean isTopUpInitiated, PlatformType platformType, Long instructorId);

    /**
     * @param platformTypeList
     * @return
     */
    List<InstructorPayment> findByIsTransferDoneAndIsTopUpInitiatedAndOrderManagementSubscribedViaPlatformInAndDueDateLessThanAndInstructorShareGreaterThan(boolean isTransferDone, boolean isTopUpInitiated, List<PlatformType> platformTypeList, Date date, double instructorShare);

    /**
     * @param isTransferDone
     * @param isTopUpInitiated
     * @param platformTypeList
     * @param date
     * @param instructorShare
     * @return
     */
    long countByIsTransferDoneAndIsTopUpInitiatedAndOrderManagementSubscribedViaPlatformInAndDueDateLessThanAndInstructorShareGreaterThan(boolean isTransferDone, boolean isTopUpInitiated, List<PlatformType> platformTypeList, Date date, double instructorShare);

    /**
     * Used to fetch the instructor payment record based on the transfer id from Stripe
     *
     * @param transferId
     * @return
     */
    InstructorPayment findTop1ByStripeTransferId(String transferId);

    InstructorPayment findByInstructorPaymentId(Long instructorPaymentId);

    /**
     * @param instructor
     * @return
     */
    InstructorPayment findTop1ByInstructorOrderByInstructorPaymentIdDesc(User instructor);

    /**
     * @param orderManagementId
     * @return
     */
    List<InstructorPayment> findByOrderManagementIdIn(List<Integer> orderManagementId);

}



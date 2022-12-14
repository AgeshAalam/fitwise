package com.fitwise.repository.payments.appleiap;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.payments.appleiap.VerifyReceipt;

public interface VerifyReceiptRepository extends JpaRepository<VerifyReceipt, Long> {

	//VerifyReceipt findTop1ByProgramProgramIdAndOriginalTxnIdOrderByCreatedDateDesc(Long productId,String originalTxnId);
	//VerifyReceipt findTop1ByProgramProgramIdAndUserUserIdOrderByCreatedDateDesc(Long productId,Long userId);
	//VerifyReceipt findByProgramProgramIdAndOriginalTxnIdAndSubscriptionIdAndTransactionId(Long productId,String originalTxnId,String subscriptionId,String txnId);
	//VerifyReceipt findByTransactionIdAndOriginalTxnId(String txnId,String originalTxnId);
	VerifyReceipt findTop1ByProgramProgramIdAndOriginalTxnIdAndProgramNameOrderByIdDesc(Long programId,String originalTxnId,String programName);
	VerifyReceipt findTop1ByOriginalTxnIdAndProgramProgramIdAndUserUserIdOrderByIdDesc(String originalTxnId,Long programId,Long userId);
}

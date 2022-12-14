package com.fitwise.repository.payments.appleiap;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitwise.entity.payments.appleiap.IosReceiptInfo;

public interface IosReceiptInfoRepository extends JpaRepository<IosReceiptInfo,Long>{
	IosReceiptInfo findTop1ByProgramIdOrderByPurchaseDateDesc(String pdtId);
	//IosReceiptInfo findTop1ByProgramIdAndOriginalTransactionIdAndWeborderLineitemIdOrderByCreatedDateDesc(String programId,String appleTransactionId, String webOrderLineItemId);
	IosReceiptInfo findTop1ByProgramIdAndOriginalTransactionIdOrderByCreatedDateDesc(Long programId,String appleTransactionId);

}

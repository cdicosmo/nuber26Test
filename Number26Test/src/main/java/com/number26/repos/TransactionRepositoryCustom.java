package com.number26.repos;

import com.number26.models.Transaction;

public interface TransactionRepositoryCustom {
	
	public Double calcSumFromId(Long transactionId);
	
	public Transaction checkAndSave(Transaction transaction);

}

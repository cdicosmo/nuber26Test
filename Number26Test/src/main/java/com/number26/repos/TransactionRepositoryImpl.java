package com.number26.repos;

import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import com.number26.models.Transaction;

@Transactional
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {
	
	@Autowired
	TransactionRepository transactionRepository;
	
	@Override
	public Double calcSumFromId(Long transactionId) {
		Transaction transaction = transactionRepository.findOne(transactionId);
		if(transaction == null){
			return null;
		}
		Double sum = getSumOfChildren(transaction);
		return sum;
	}

	protected Double getSumOfChildren(Transaction transaction){
		Double sum = transaction.getAmount();
		
		Collection<Transaction> childrenTransactions = transactionRepository.findByParentId(transaction.getId());
		if(childrenTransactions.isEmpty()){
			return sum;
		}
		for (Transaction childTransaction : childrenTransactions) {
			sum += getSumOfChildren(childTransaction);
		}
		return sum;
	}
	

	@Override
	public Transaction checkAndSave(Transaction transaction) {
		if(transaction.getParentId() != null && transactionRepository.findOne(transaction.getParentId()) == null){
			return null;
		}
		if(checkCircularReferenceOnChildren(transaction, transaction.getParentId())){
			return null;
		}else{
			return transactionRepository.save(transaction);			
		}
	}

	protected boolean checkCircularReferenceOnChildren(Transaction transaction, Long parentId){
		if(parentId == null){
			return false;
		}
		boolean found = transaction.getId().equals(parentId);
		Collection<Transaction> childrenTransactions = transactionRepository.findByParentId(transaction.getId());
		if(found || childrenTransactions.isEmpty()){
			return found;
		}
		for (Transaction childTransaction: childrenTransactions) {
			if(checkCircularReferenceOnChildren(childTransaction, parentId)){
				return true;
			}
		}
		
		return found;
	}
}

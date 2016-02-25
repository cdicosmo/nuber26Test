package com.number26.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.number26.models.Transaction;
import com.number26.repos.TransactionRepository;


@RestController
@RequestMapping("/transactionservice")
public class TransactionServiceController {
	
	@Autowired
	TransactionRepository transactionRepo;
	
	
	@RequestMapping(value = "/{transactionId}", method = RequestMethod.PUT)
	public Hashtable<String, String> createTransaction(@RequestBody Transaction transaction,  @PathVariable Long transactionId){
		transaction.setId(transactionId);
		Transaction createdTransaction = transactionRepo.checkAndSave(transaction);
		Hashtable<String, String> result = new Hashtable<>();
		if(createdTransaction != null){
			result.put("status", "ok");			
		}else{
			result.put("status", "ko");
		}
		return result;
	}
	
	@RequestMapping(value = "/{transactionId}", method = RequestMethod.GET)
	public Transaction getTransaction(@PathVariable Long transactionId){
		return transactionRepo.findOne(transactionId);
	}
	
	@RequestMapping(value = "/types/{transactionType}", method = RequestMethod.GET)
	public List<Long> getTransactionsByType(@PathVariable String transactionType){
		Collection<Transaction> transactionsByType = transactionRepo.findByType(transactionType);
		List<Long> ids = new ArrayList<>();
		
		for (Transaction transaction : transactionsByType) {
			ids.add(transaction.getId());
		}
		return ids;
	}
	
	@RequestMapping(value = "/sum/{transactionId}", method = RequestMethod.GET)
	public Hashtable<String, String> getSumTransactions(@PathVariable Long transactionId){
		Double transactionsSum = transactionRepo.calcSumFromId(transactionId);
		Hashtable<String, String> result = new Hashtable<>();
		if(transactionsSum != null){
			result.put("sum", transactionsSum.toString());			
		}else{
			result.put("status", "ko");
		}
		return result;
	}
}
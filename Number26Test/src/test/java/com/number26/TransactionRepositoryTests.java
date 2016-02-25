package com.number26;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.number26.models.Transaction;
import com.number26.repos.TransactionRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Number26TestApplication.class)
@WebAppConfiguration
public class TransactionRepositoryTests {
	
	@Autowired
	private TransactionRepository transactionRepo;

	private List<Transaction> transactions = new ArrayList<>();
	
	@Before
	public void setup() throws Exception {
		Random r = new Random();
		Long transactionId = new Long(0);
		for (int i = 0; i < 10; i++) {
			transactions.add(transactionRepo.save(new Transaction(transactionId++, "Bank Transfer", r.nextDouble(), null)));
		}
		
		for (int i = 0; i < 10; i++) {
			transactions.add(transactionRepo.save(new Transaction(transactionId++, "PayPal", r.nextDouble(), null)));
		}
		
	}
	
	@Test 
	public void testAvoidCircularReference() {
		Long transactionIncrementalId = new Long(1000);
		Long nestedTransactionId = null;
		for(int i = 0; i < 5 ; i++){
			Transaction nestedTransaction = transactionRepo.checkAndSave(new Transaction(transactionIncrementalId++, "Bank Transfer", new Double(200), nestedTransactionId));
			nestedTransactionId = nestedTransaction.getId();
			Assert.assertNotNull(nestedTransaction);			
		}
		
		Transaction expectedNullTransaction = transactionRepo.checkAndSave(new Transaction(new Long(1000), "Bank Transfer", new Double(200), nestedTransactionId));
		Assert.assertNull(expectedNullTransaction);
		
	}
	
	@Test
	public void testAvoidSavingTransactionsIfNoMatchesForParentIdOrSelfReference(){
		Long baseId = new Long(100);
		Transaction transactionWithNotMatchingParent = transactionRepo.checkAndSave(new Transaction(baseId, "Bank Transfer", new Double(200), new Long(10011)));
		Assert.assertNull(transactionWithNotMatchingParent);
		
		Transaction transactionWithSelfReference = transactionRepo.checkAndSave(new Transaction(baseId, "Bank Transfer", new Double(200), baseId));
		Assert.assertNull(transactionWithSelfReference);
	}
	
	@Test
	public void testGetTransactionById() {
		Transaction expectedTransaction = transactions.get(5);
		Transaction transaction = transactionRepo.findOne(expectedTransaction.getId());
		
		Assert.assertEquals(expectedTransaction.getId(), transaction.getId());
	}
	
	@Test
	public void testNullIfNoMatchingId() {
		Transaction transaction = transactionRepo.findOne(new Long("1234"));
		Assert.assertNull(transaction);
	}
	
	@Test
	public void testGetTransactionsByType() {
		String expectedType = "Bank Transfer";
		Collection<Transaction> transactions = transactionRepo.findByType(expectedType);
		
		for (Transaction transaction : transactions) {
			Assert.assertEquals(expectedType, transaction.getType());
		}
	}
	
	@Test
	public void testGetEmptyListIfNoMatchingType() {
		String expectedType = "Bank Transfer";
		Collection<Transaction> transactions = transactionRepo.findByType(expectedType);
		
		Assert.assertFalse(transactions.isEmpty());
	}
	
	@Test
	public void testGetSumOfSingleTransaction() {
		Transaction expectedTransaction = transactions.get(2);
		Double sum = transactionRepo.calcSumFromId(expectedTransaction.getId());
		
		Assert.assertEquals(expectedTransaction.getAmount(), sum);
	}
	
	@Test
	public void testGetSumNullIfNoMatchingTransaction() {
		Double sum = transactionRepo.calcSumFromId(new Long("1234"));
		
		Assert.assertNull(sum);
	}
	
	@Test
	public void testGetSumOfChainedTransactions() {
		Random r = new Random();
		Transaction parentTransaction = transactionRepo.save(new Transaction(new Long(100), "Type", r.nextDouble(), null));
		Double expectedSum = generateChainedtransactions(parentTransaction, 0);
		
		Double sum = transactionRepo.calcSumFromId(parentTransaction.getId());
		
		Assert.assertEquals(expectedSum, sum);
	}
	
	public Double generateChainedtransactions(Transaction parentTransaction, int depth) {
		Double sum = parentTransaction.getAmount();
		if(depth == 3){
			return sum;
		}else{
			depth++;
		}
		Random r = new Random();
		Long transactionId = parentTransaction.getId() + r.nextInt();
		for(int i = 0; i < 2; i++){
			Double amount = r.nextDouble();
			Transaction transaction = transactionRepo.save(new Transaction(transactionId++, "Type", amount, parentTransaction.getId()));
			sum += generateChainedtransactions(transaction, depth);
		}
		return sum;
	}

}

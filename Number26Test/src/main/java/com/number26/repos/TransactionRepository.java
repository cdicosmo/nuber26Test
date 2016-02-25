package com.number26.repos;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.number26.models.Transaction;

@RepositoryRestResource(collectionResourceRel = "transactionservice", path = "transactionservice")
public interface TransactionRepository extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {
	public Collection<Transaction> findByType(String type);
	
	public Collection<Transaction> findByParentId(Long parentId);
}

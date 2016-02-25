package com.number26.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Transaction {
	
	@Id
	private Long id;
	
	private String type;
	
	private Double amount;
	
	private Long parentId;

	public Transaction(Long id, String type, Double amount, Long parentId) {
		this.id = id;
		this.type = type;
		this.amount = amount;
		this.parentId = parentId;
	}
	
	public Transaction(){
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
	
	

}

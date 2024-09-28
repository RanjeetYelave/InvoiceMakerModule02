package com.example.demo;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long invoiceNo;

	private Date date;

	private Double subTotal;

	private Double totalAmount;

	private Double receivedAmount;

	private Double balanceAmount;

	private Double previousBalance; // New field for previous balance

	private String amountInWords;

	private Double discount; // New field for discount

	@ManyToOne
	@JoinColumn(name = "party_id", nullable = false)
	private Party party;

	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InvoiceItem> items;
}

package com.example.demo;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private String contact;

    private Double balanceAmount = 0.0; // New field for balance amount

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices;
}

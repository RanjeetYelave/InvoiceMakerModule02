package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PartyRepository partyRepository;

    /**
     * Create a new invoice. 
     * If a party with the same name exists, assign the invoice to that party.
     * Otherwise, create a new party and assign the invoice.
     */
    public Invoice createInvoice(Invoice invoice) {
        // Check if the party with the given name already exists
        Party party = findOrCreateParty(invoice.getParty());
        
        // Set the party in the invoice
        invoice.setParty(party);
        
        // Set the relationship between invoice and items
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                item.setInvoice(invoice); // Set reference for each item
            }

            // Calculate subtotal
            double subTotal = invoice.getItems().stream()
                    .mapToDouble(InvoiceItem::getAmount)
                    .sum();
            invoice.setSubTotal(subTotal);

            // Apply discount if present
            if (invoice.getDiscount() != null && invoice.getDiscount() > 0) {
                subTotal -= invoice.getDiscount();
            }

            // Set previous balance from party
            invoice.setPreviousBalance(party.getBalanceAmount());

            // Calculate final total amount
            double finalTotal = subTotal + invoice.getPreviousBalance();
            invoice.setTotalAmount(finalTotal);

            // Calculate balance after payment
            invoice.setBalanceAmount(finalTotal - invoice.getReceivedAmount());

            // Update party balance
            party.setBalanceAmount(invoice.getBalanceAmount()); // Update party's balance
        }

        // Save the invoice
        return invoiceRepository.save(invoice);
    }

    /**
     * Update an existing invoice by its ID.
     */
    public Invoice updateInvoice(Long invoiceId, Invoice updatedInvoice) {
        return invoiceRepository.findById(invoiceId)
                .map(invoice -> {
                    invoice.setDate(updatedInvoice.getDate());
                    invoice.setReceivedAmount(updatedInvoice.getReceivedAmount());
                    invoice.setDiscount(updatedInvoice.getDiscount()); // Update discount
                    
                    // Clear the existing items and add updated ones
                    invoice.getItems().clear();
                    if (updatedInvoice.getItems() != null) {
                        for (InvoiceItem item : updatedInvoice.getItems()) {
                            item.setInvoice(invoice); // Maintain reference in items
                            invoice.getItems().add(item);
                        }
                    }

                    // Calculate subtotal
                    double subTotal = invoice.getItems().stream()
                            .mapToDouble(InvoiceItem::getAmount)
                            .sum();
                    invoice.setSubTotal(subTotal);

                    // Apply discount if present
                    if (invoice.getDiscount() != null && invoice.getDiscount() > 0) {
                        subTotal -= invoice.getDiscount();
                    }

                    // Set previous balance from party
                    invoice.setPreviousBalance(invoice.getParty().getBalanceAmount());

                    // Calculate final total amount
                    double finalTotal = subTotal + invoice.getPreviousBalance();
                    invoice.setTotalAmount(finalTotal);

                    // Calculate balance after payment
                    invoice.setBalanceAmount(finalTotal - invoice.getReceivedAmount());

                    // Update party balance
                    invoice.getParty().setBalanceAmount(invoice.getBalanceAmount()); // Update party's balance

                    return invoiceRepository.save(invoice);
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    /**
     * Delete an invoice by its ID.
     */
    public void deleteInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new RuntimeException("Invoice not found");
        }
        invoiceRepository.deleteById(invoiceId);
    }
    
    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    /**
     * Get all invoices.
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Helper method to find an existing party by name or create a new one if not found.
     */
    private Party findOrCreateParty(Party party) {
        // Search for an existing party with the same name
        Optional<Party> existingParty = partyRepository.findAll()
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(party.getName()))
                .findFirst();

        // If the party exists, return it, otherwise save the new party
        return existingParty.orElseGet(() -> {
            Party newParty = new Party();
            newParty.setName(party.getName());
            newParty.setAddress(party.getAddress());
            newParty.setContact(party.getContact());
            return partyRepository.save(newParty);
        });
    }
}

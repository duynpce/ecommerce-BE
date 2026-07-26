package org.example.ticketservice.domain.model;

import org.example.ticketservice.domain.constant.TicketType;

import java.util.UUID;

public class PromotionTicket extends Ticket {

    private UUID ticketId;
    private String identityCardNumber;
    private String bankName;
    private String bankAccountNumber;
    private String taxId;

    public PromotionTicket() {
        super();
        setType(TicketType.PROMOTION);
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public String getIdentityCardNumber() {
        return identityCardNumber;
    }

    public void setIdentityCardNumber(String identityCardNumber) {
        if (identityCardNumber == null || identityCardNumber.isBlank()) {
            throw new IllegalArgumentException("Identity card number cannot be null or blank.");
        }
        this.identityCardNumber = identityCardNumber.trim();
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("Bank name cannot be null or blank.");
        }
        this.bankName = bankName.trim();
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        if (bankAccountNumber == null || bankAccountNumber.isBlank()) {
            throw new IllegalArgumentException("Bank account number cannot be null or blank.");
        }
        this.bankAccountNumber = bankAccountNumber.trim();
    }


    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        if (taxId == null || taxId.isBlank()) {
            throw new IllegalArgumentException("Tax ID cannot be null or blank.");
        }
        this.taxId = taxId.trim();
    }
}

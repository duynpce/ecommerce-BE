package org.example.ticketservice.infrastructure.web.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "promotion_tickets",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_promotion_identity_card",      columnNames = "identity_card_number"),
        @UniqueConstraint(name = "uq_promotion_bank_account",       columnNames = "bank_account_number"),
        @UniqueConstraint(name = "uq_promotion_tax_id",             columnNames = "tax_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionTicketEntity {

    @Id
    @Column(name = "ticket_id", updatable = false, nullable = false)
    private UUID ticketId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", referencedColumnName = "id")
    private TicketEntity ticket;

    @Column(name = "identity_card_number", nullable = false, unique = true)
    private String identityCardNumber;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_account_number", nullable = false, unique = true)
    private String bankAccountNumber;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;
}

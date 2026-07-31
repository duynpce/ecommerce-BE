
package org.example.userservice.infrastructure.web.data.entity;

import jakarta.persistence.*;
        import lombok.*;
        import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "contributor_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_contributor_identity_card",   columnNames = "identity_card_number"),
                @UniqueConstraint(name = "uq_contributor_bank_account",    columnNames = "bank_account_number"),
                @UniqueConstraint(name = "uq_contributor_tax_id",          columnNames = "tax_id"),
                @UniqueConstraint(name = "uq_contributor_account_id",      columnNames = "account_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ContributorProfileEntity {

    @Id
    @Column(name = "account_id", nullable = false, unique = true, updatable = false)
    private UUID accountId;

    @Column(name = "identity_card_number", nullable = false, unique = true)
    private String identityCardNumber;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_account_number", nullable = false, unique = true)
    private String bankAccountNumber;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
package org.example.userservice.infrastructure.web.data.springdata;

import org.example.userservice.infrastructure.web.data.entity.ContributorProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataContributorProfileRepository extends JpaRepository<ContributorProfileEntity, UUID> {

    Optional<ContributorProfileEntity> findByAccountId(UUID accountId);

    boolean existsByAccountId(UUID accountId);

    boolean existsByIdentityCardNumber(String identityCardNumber);

    boolean existsByBankAccountNumber(String bankAccountNumber);

    boolean existsByTaxId(String taxId);
}

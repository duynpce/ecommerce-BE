package org.example.userservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.userservice.application.mapper.ContributorProfileMapper;
import org.example.userservice.application.repository.ContributorProfileRepository;
import org.example.userservice.domain.model.ContributorProfile;
import org.example.userservice.infrastructure.web.data.springdata.SpringDataContributorProfileRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContributorProfileRepositoryAdapter implements ContributorProfileRepository {

    private final SpringDataContributorProfileRepository springDataContributorProfileRepository;
    private final ContributorProfileMapper contributorProfileMapper;

    @Override
    public void save(ContributorProfile contributorProfile) {
        springDataContributorProfileRepository.save(contributorProfileMapper.toEntity(contributorProfile));
    }

    @Override
    public ContributorProfile findByAccountId(UUID accountId) {
        return springDataContributorProfileRepository.findByAccountId(accountId)
                .map(contributorProfileMapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByAccountId(UUID accountId) {
        return springDataContributorProfileRepository.existsByAccountId(accountId);
    }

    @Override
    public boolean existsByIdentityCardNumber(String identityCardNumber) {
        return springDataContributorProfileRepository.existsByIdentityCardNumber(identityCardNumber);
    }

    @Override
    public boolean existsByBankAccountNumber(String bankAccountNumber) {
        return springDataContributorProfileRepository.existsByBankAccountNumber(bankAccountNumber);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return springDataContributorProfileRepository.existsByTaxId(taxId);
    }
}

package org.example.userservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.userservice.application.command.PageCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.application.mapper.AccountProfileMapper;
import org.example.userservice.application.repository.AccountProfileRepository;
import org.example.userservice.domain.model.AccountProfile;
import org.example.userservice.infrastructure.web.data.entity.AccountProfileEntity;
import org.example.userservice.infrastructure.web.data.specification.AccountProfileSpecification;
import org.example.userservice.infrastructure.web.data.springdata.SpringDataAccountProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaAccountProfileRepositoryAdapter implements AccountProfileRepository {

    private final SpringDataAccountProfileRepository springDataAccountProfileRepository;
    private final AccountProfileMapper accountProfileMapper;

    @Override
    public void save(AccountProfile accountProfile) {
        springDataAccountProfileRepository.save(accountProfileMapper.toEntity(accountProfile));
    }

    @Override
    public AccountProfile findById(UUID id) {
        return accountProfileMapper.toDomain(springDataAccountProfileRepository.findById(id).orElse(null));
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataAccountProfileRepository.existsById(id);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return springDataAccountProfileRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public PageCommand<AccountProfile> search(AccountProfileSearchCriteria criteria) {
        Pageable pageable = PageRequest.of(criteria.page(), criteria.limit());
        Specification<AccountProfileEntity> spec = AccountProfileSpecification.fromCriteria(criteria);
        Page<AccountProfileEntity> entityPage = springDataAccountProfileRepository.findAll(spec, pageable);

        List<AccountProfile> accountProfiles = entityPage.getContent().stream()
                .map(accountProfileMapper::toDomain)
                .toList();


        return PageCommand.of(
                accountProfiles,
                entityPage.getTotalElements(),
                entityPage.getNumber(),
                entityPage.getSize()
        );
    }
}
package org.example.userservice.infrastructure.web.data.springdata;

import org.example.userservice.infrastructure.web.data.entity.AccountProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SpringDataAccountProfileRepository extends JpaRepository<AccountProfileEntity, UUID>, JpaSpecificationExecutor<AccountProfileEntity> {
    boolean existsByPhoneNumber(String phoneNumber);
}
package org.example.userservice.infrastructure.web.data.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.userservice.domain.constant.Gender;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountProfileEntity {

    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", nullable = false, unique = true  )
    private String phoneNumber;

    @Column(name = "address",  nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
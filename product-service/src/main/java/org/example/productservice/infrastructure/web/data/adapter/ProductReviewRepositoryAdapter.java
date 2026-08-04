package org.example.productservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.mapper.ProductReviewMapper;
import org.example.productservice.application.repository.ProductReviewRepository;
import org.example.productservice.domain.model.ProductReview;
import org.example.productservice.infrastructure.web.data.entity.ProductReviewEntity;
import org.example.productservice.infrastructure.web.data.springdata.SpringDataProductReviewRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductReviewRepositoryAdapter implements ProductReviewRepository {

    private final SpringDataProductReviewRepository springDataRepo;
    private final ProductReviewMapper productReviewMapper;

    @Override
    public ProductReview save(ProductReview productReview) {
        ProductReviewEntity entity = productReviewMapper.toEntity(productReview);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return productReviewMapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<ProductReview> findById(UUID id) {
        return springDataRepo.findById(id).map(productReviewMapper::toDomain);
    }

    @Override
    public List<ProductReview> findAllByProductId(UUID productId) {
        return springDataRepo.findAllByProductId(productId).stream()
                .map(productReviewMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndTransactionId(UUID userId, UUID transactionId) {
        return springDataRepo.existsByUserIdAndTransactionId(userId, transactionId);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }
}

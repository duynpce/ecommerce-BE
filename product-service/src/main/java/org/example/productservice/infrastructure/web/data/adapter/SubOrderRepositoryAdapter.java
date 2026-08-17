package org.example.productservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.application.mapper.SubOrderMapper;
import org.example.productservice.application.repository.SubOrderRepository;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.infrastructure.web.data.entity.SubOrderEntity;
import org.example.productservice.infrastructure.web.data.specification.SubOrderSpecification;
import org.example.productservice.infrastructure.web.data.springdata.SpringDataSubOrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubOrderRepositoryAdapter implements SubOrderRepository {

    private final SpringDataSubOrderRepository springDataRepo;
    private final SubOrderMapper subOrderMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public SubOrder save(SubOrder subOrder) {
        SubOrderEntity entity = subOrderMapper.toEntity(subOrder);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return subOrderMapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<SubOrder> findById(UUID id) {
        return springDataRepo.findById(id).map(subOrderMapper::toDomain);
    }

    @Override
    public List<SubOrder> findByTransactionId(UUID transactionId) {
        return springDataRepo.findByTransactionId(transactionId).stream()
                .map(subOrderMapper::toDomain)
                .toList();
    }

    @Override
    public List<SubOrder> findByShopId(UUID shopId) {
        return springDataRepo.findByShopId(shopId).stream()
                .map(subOrderMapper::toDomain)
                .toList();
    }

    @Override
    public List<SubOrder> findByCustomerId(UUID customerId) {
        return springDataRepo.findByCustomerId(customerId).stream()
                .map(subOrderMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public PageCommand<SubOrder> search(SubOrderSearchCriteria criteria) {
        Query query = SubOrderSpecification.fromCriteria(criteria);

        long totalCount = mongoTemplate.count(query, SubOrderEntity.class);

        query.with(PageRequest.of(criteria.page(), criteria.limit()));
        List<SubOrder> subOrders = mongoTemplate.find(query, SubOrderEntity.class).stream()
                .map(subOrderMapper::toDomain)
                .toList();

        return PageCommand.of(subOrders, totalCount, criteria.page(), criteria.limit());
    }
}

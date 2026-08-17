package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.web.data.entity.TransactionEntity;
import org.example.productservice.infrastructure.web.dto.transaction.CreateTransactionRequest;
import org.example.productservice.infrastructure.web.dto.transaction.TransactionFilter;
import org.example.productservice.infrastructure.web.dto.transaction.TransactionResponse;
import org.example.productservice.infrastructure.web.dto.transaction.UpdateTransactionRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {ProductMapperMapstruct.class, DateMapper.class})
public interface TransactionMapperMapStruct extends TransactionMapper {

    @Override
    Transaction toDomain(TransactionEntity entity);

    @Override
    Transaction toDomain(CreateTransactionCommand command);

    @Override
    TransactionEntity toEntity(Transaction transaction);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromCommand(UpdateTransactionCommand command, @MappingTarget Transaction transaction);

    @Override
    default CreateTransactionCommand toCommand(CreateTransactionRequest request, UUID customerId) {
        return new CreateTransactionCommand(customerId, request.getItemList());
    }

    @Override
    @Mapping(target = "id", source = "id")
    UpdateTransactionCommand toCommand(UpdateTransactionRequest request, UUID id);

    @Override
    @Mapping(target = "userId",        source = "userId")
    @Mapping(target = "contributorId", ignore = true)
    @Mapping(target = "createdFrom",   source = "filter.createdFrom", qualifiedByName = "localDateToInstantStart")
    @Mapping(target = "createdTo",     source = "filter.createdTo",   qualifiedByName = "localDateToInstantEnd")
    TransactionSearchCriteria toCriteria(TransactionFilter filter, UUID userId);

    @Override
    @Mapping(target = "userId",        ignore = true)
    @Mapping(target = "contributorId", source = "contributorId")
    @Mapping(target = "createdFrom",   source = "filter.createdFrom", qualifiedByName = "localDateToInstantStart")
    @Mapping(target = "createdTo",     source = "filter.createdTo",   qualifiedByName = "localDateToInstantEnd")
    TransactionSearchCriteria toContributorCriteria(TransactionFilter filter, UUID contributorId);

    @Override
    TransactionResponse toResponse(Transaction transaction);
}
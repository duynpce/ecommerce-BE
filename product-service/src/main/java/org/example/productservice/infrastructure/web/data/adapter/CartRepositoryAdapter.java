package org.example.productservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.mapper.CartMapper;
import org.example.productservice.application.repository.CartRepository;
import org.example.productservice.domain.model.Cart;
import org.example.productservice.infrastructure.web.data.entity.CartEntity;
import org.example.productservice.infrastructure.web.data.springdata.SpringDataCartRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final SpringDataCartRepository springDataCartRepository;
    private final CartMapper cartMapper;

    @Override
    public Cart save(Cart cart) {
        CartEntity entity = cartMapper.toEntity(cart);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        entity.setTotalAmount(cart.calculateTotal());
        return cartMapper.toDomain(springDataCartRepository.save(entity));
    }

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return springDataCartRepository.findByUserId(userId)
                .map(cartMapper::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataCartRepository.deleteByUserId(userId);
    }
}

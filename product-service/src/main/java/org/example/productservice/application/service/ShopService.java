package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.CreateShopCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateShopCommand;
import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.example.productservice.application.mapper.ShopMapper;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.usecase.ShopUseCase;
import org.example.productservice.application.usecase.UploadUseCase;
import org.example.productservice.domain.exception.ForbiddenException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Shop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService implements ShopUseCase {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final TokenGeneratorClient tokenGeneratorClient;
    private final UploadUseCase uploadUseCase;

    @Override
    @Transactional
    public Shop create(CreateShopCommand command) {
        log.info("Creating shop with name: {} for contributorId: {}", command.name(), command.contributorId());

        Shop shop = shopMapper.toDomain(command);

        if (command.logo() != null && !command.logo().isEmpty()) {
            String logoUrl = uploadUseCase.uploadImg(command.logo());
            shop.setLogoUrl(logoUrl);
        }

        return shopRepository.save(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public Shop findById(UUID id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shop not found: " + id));
    }

    @Override
    @Transactional
    public Shop update(UpdateShopCommand command) {
        log.info("Updating shop with id: {}", command.id());

        Shop shop = shopRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Shop not found: " + command.id()));

        if (!shop.getContributorId().equals(command.senderId())) {
            throw new ForbiddenException("You are not the owner of this shop");
        }

        shopMapper.updateFromCommand(command, shop);

        if (command.logo() != null && !command.logo().isEmpty()) {
            String logoUrl = uploadUseCase.uploadImg(command.logo());
            shop.setLogoUrl(logoUrl);
        }

        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public void delete(UUID id, String accessToken) {
        Shop shop = findById(id);
        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        Set<String> userAuthorities = tokenGeneratorClient.extractAuthoritiesFromAccessToken(accessToken);

        if (!shop.getContributorId().equals(userId) && !userAuthorities.contains("SHOP:DELETE_ALL")) {
            throw new ForbiddenException("You are not the owner of this shop");
        }

        shopRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageCommand<Shop> search(ShopSearchCriteria criteria) {
        return shopRepository.search(criteria);
    }
}

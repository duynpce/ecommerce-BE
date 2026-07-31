package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.CreateProductCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateProductCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.application.mapper.ProductMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.usecase.ProductUseCase;
import org.example.productservice.application.usecase.UploadUseCase;
import org.example.productservice.domain.exception.ForbiddenException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.Shop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final TokenGeneratorClient tokenGeneratorClient;
    private final UploadUseCase uploadUseCase;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public Product create(CreateProductCommand command) {
        log.info("Creating product with name: {}", command.name());

        Shop shop = shopRepository.findById(command.shopId())
                .orElseThrow(() -> new NotFoundException("Shop not found: " + command.shopId()));

        if(!shop.getContributorId().equals(command.contributorId())){
            throw new ForbiddenException("You are not the owner of this shop");
        }

        List<String> imgUrls = uploadAll(command.imgs());

        Product product = productMapper.toDomain(command);
        product.setImgUrls(imgUrls);

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(UpdateProductCommand command) {
        log.info("Updating product with id: {}", command.id());

        Product product = productRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Product not found: " + command.id()));

        if (!product.getContributorId().equals(command.senderId())) {
            throw new ForbiddenException("You are not the owner of this product");
        }

        productMapper.updateFromCommand(command, product);

        if (command.imgs() != null && !command.imgs().isEmpty()) {
            product.setImgUrls(uploadAll(command.imgs()));
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional
    public void delete(UUID id, String accessToken) {
        Product product = findById(id);
        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        Set<String> userAuthorities = tokenGeneratorClient.extractAuthoritiesFromAccessToken(accessToken);

        if (!product.getContributorId().equals(userId) && !userAuthorities.contains("PRODUCT:DELETE_ALL")) {
            throw new ForbiddenException("You are not the owner of this product");
        }

        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageCommand<Product> search(ProductSearchCriteria criteria) {
        return productRepository.search(criteria);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<String> uploadAll(List<MultipartFile> imgs) {
        return imgs.stream()
                .map(uploadUseCase::uploadImg)
                .toList();
    }
}

package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.command.CreateProductCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateProductCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.application.mapper.ProductMapper;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.usecase.ProductUseCase;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.domain.model.Product;
import org.example.productservice.infrastructure.web.dto.*;
import org.example.productservice.infrastructure.web.dto.product.CreateProductRequest;
import org.example.productservice.infrastructure.web.dto.product.ProductFilter;
import org.example.productservice.infrastructure.web.dto.product.ProductResponse;
import org.example.productservice.infrastructure.web.dto.product.UpdateProductRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping
public class ProductController {

    private final ProductUseCase productUseCase;
    private final ProductMapper productMapper;
    private final TokenGeneratorClient  tokenGeneratorClient;


    @PreAuthorize("hasAuthority('PRODUCT:WRITE_SELF')")
    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<Void>> create(
            @Valid @ModelAttribute CreateProductRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID contributorId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        log.info("Creating product for contributorId: {} and product's name {} ", contributorId, request.name());
        CreateProductCommand command = productMapper.toCommand(request, contributorId);

        productUseCase.create(command);
        return new ResponseEntity<>(ResponseDto.success(null, "Product created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductResponse>> findById(@PathVariable UUID id) {
        ProductResponse data = productMapper.toResponse(productUseCase.findById(id));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<List<ProductResponse>>> findMyProducts(
            @Valid @ModelAttribute PaginationDto paginationDto, @CookieValue(value = "accessToken", required = false) String accessToken) {
        UUID contributorId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);

        // // get product  from the logged-in contributor with pagination
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, contributorId, null, null, null, null, paginationDto.getPage(), paginationDto.getLimit());

        PageCommand<Product> page = productUseCase.search(criteria);
        List<ProductResponse> data = page.getContent().stream()
                .map(productMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ResponseDto.success(data));
    }

    /**
     * Filterable, paginated product search.
     * Example: GET /api/v1/products/search?name=shoes&category=FASHION&minPrice=10&page=0&limit=20
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseDto<List<ProductResponse>>> search(
            @Valid @ModelAttribute ProductFilter filter) {
        ProductSearchCriteria criteria = productMapper.toCriteria(filter);
        PageCommand<Product> page = productUseCase.search(criteria);

        List<ProductResponse> data = page.getContent().stream()
                .map(productMapper::toResponse)
                .toList();

        MetaDto meta = MetaDto.builder()
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .paginationDto(new PaginationDto(filter.page(), filter.limit()))
                .build();

        return ResponseEntity.ok(ResponseDto.success(data, "Products fetched successfully", meta));
    }

    @PreAuthorize("hasAuthority('PRODUCT:WRITE_SELF')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductResponse>> update(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateProductRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        UUID senderId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);

        UpdateProductCommand command = productMapper.toCommand(request, id, senderId);
        ProductResponse data = productMapper.toResponse(productUseCase.update(command));
        return ResponseEntity.ok(ResponseDto.success(data));
    }


    @PreAuthorize("hasAuthority('PRODUCT:DELETE_SELF') or hasAuthority('PRODUCT:DELETE_ALL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        productUseCase.delete(id, accessToken);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('PRODUCT:WRITE_SELF')")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test endpoint is working!");
    }
}

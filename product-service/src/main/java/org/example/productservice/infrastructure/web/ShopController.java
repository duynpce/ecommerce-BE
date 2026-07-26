package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.command.CreateShopCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateShopCommand;
import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.example.productservice.application.mapper.ShopMapper;
import org.example.productservice.application.usecase.ShopUseCase;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.infrastructure.web.dto.*;
import org.example.productservice.infrastructure.web.dto.shop.CreateShopRequest;
import org.example.productservice.infrastructure.web.dto.shop.ShopFilter;
import org.example.productservice.infrastructure.web.dto.shop.ShopResponse;
import org.example.productservice.infrastructure.web.dto.shop.UpdateShopRequest;
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
@RequestMapping("/shops")
public class ShopController {

    private final ShopUseCase shopUseCase;
    private final ShopMapper shopMapper;
    private final TokenGeneratorClient tokenGeneratorClient;

    @PreAuthorize("hasAuthority('SHOP:WRITE_SELF')")
    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<Void>> create(
            @Valid @ModelAttribute CreateShopRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID contributorId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        log.info("Creating shop for contributorId: {} with name: {}", contributorId, request.name());
        CreateShopCommand command = shopMapper.toCommand(request, contributorId);
        shopUseCase.create(command);
        return new ResponseEntity<>(ResponseDto.success(null, "Shop created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ShopResponse>> findById(@PathVariable UUID id) {
        ShopResponse data = shopMapper.toResponse(shopUseCase.findById(id));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<List<ShopResponse>>> me(@CookieValue(value = "accessToken", required = false)  String accessToken) {
        UUID contributorId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);

        // get all shop from the logged-in contributor, no pagination, no filter
        ShopSearchCriteria criteria = new ShopSearchCriteria(null, contributorId, null, 0, Integer.MAX_VALUE);

        List<ShopResponse> data = shopUseCase.search(criteria).getContent().stream()
                .map(shopMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ResponseDto.success(data));
    }

    /**
     * Filterable, paginated shop search.
     * Example: GET /api/v1/shops/search?name=coffee&status=ACTIVE&page=0&limit=20
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseDto<List<ShopResponse>>> search(
            @Valid @ModelAttribute ShopFilter filter) {
        ShopSearchCriteria criteria = shopMapper.toCriteria(filter);
        PageCommand<Shop> page = shopUseCase.search(criteria);

        List<ShopResponse> data = page.getContent().stream()
                .map(shopMapper::toResponse)
                .toList();

        MetaDto meta = MetaDto.builder()
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .paginationDto(new PaginationDto(filter.page(), filter.limit()))
                .build();

        return ResponseEntity.ok(ResponseDto.success(data, "Shops fetched successfully", meta));
    }

    @PreAuthorize("hasAuthority('SHOP:WRITE_SELF')")
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<ShopResponse>> update(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateShopRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        UUID senderId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        UpdateShopCommand command = shopMapper.toCommand(request, id, senderId);
        ShopResponse data = shopMapper.toResponse(shopUseCase.update(command));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @PreAuthorize("hasAuthority('SHOP:DELETE_SELF') or hasAuthority('SHOP:DELETE_ALL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        shopUseCase.delete(id, accessToken);
        return ResponseEntity.noContent().build();
    }
}

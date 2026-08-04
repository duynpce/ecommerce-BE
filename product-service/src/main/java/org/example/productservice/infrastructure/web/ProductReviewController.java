package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.command.CreateProductReviewCommand;
import org.example.productservice.application.command.UpdateProductReviewCommand;
import org.example.productservice.application.mapper.ProductReviewMapper;
import org.example.productservice.application.usecase.ProductReviewUseCase;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.domain.model.ProductReview;
import org.example.productservice.infrastructure.web.dto.ResponseDto;
import org.example.productservice.infrastructure.web.dto.productreview.CreateProductReviewRequest;
import org.example.productservice.infrastructure.web.dto.productreview.ProductReviewResponse;
import org.example.productservice.infrastructure.web.dto.productreview.UpdateProductReviewRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reviews")
public class ProductReviewController {

    private final ProductReviewUseCase productReviewUseCase;
    private final ProductReviewMapper productReviewMapper;
    private final TokenGeneratorClient tokenGeneratorClient;

    @PreAuthorize("hasAuthority('REVIEW:WRITE_SELF')")
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<Void>> create(
            @Valid @RequestBody CreateProductReviewRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        log.info("Creating review for productId: {} by userId: {}", request.productId(), userId);

        CreateProductReviewCommand command = productReviewMapper.toCommand(request, userId);
        productReviewUseCase.create(command);

        return new ResponseEntity<>(ResponseDto.success(null, "Review created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductReviewResponse>> findById(@PathVariable UUID id) {
        ProductReviewResponse data = productReviewMapper.toResponse(productReviewUseCase.findById(id));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseDto<List<ProductReviewResponse>>> findAllByProductId(
            @PathVariable UUID productId) {

        List<ProductReviewResponse> data = productReviewUseCase.findAllByProductId(productId)
                .stream()
                .map(productReviewMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @PreAuthorize("hasAuthority('REVIEW:WRITE_SELF')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductReviewResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductReviewRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID senderId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        log.info("Updating review id: {} by userId: {}", id, senderId);

        UpdateProductReviewCommand command = productReviewMapper.toCommand(request, id, senderId);
        ProductReviewResponse data = productReviewMapper.toResponse(productReviewUseCase.update(command));

        return ResponseEntity.ok(ResponseDto.success(data));
    }
}

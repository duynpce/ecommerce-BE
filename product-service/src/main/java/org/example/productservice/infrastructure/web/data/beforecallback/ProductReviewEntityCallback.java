package org.example.productservice.infrastructure.web.data.beforecallback;

import org.bson.Document;
import org.example.productservice.infrastructure.web.data.entity.ProductReviewEntity;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class ProductReviewEntityCallback implements BeforeSaveCallback<ProductReviewEntity> {
    @Override
    public ProductReviewEntity onBeforeSave(ProductReviewEntity entity, Document document, String collection) {
        document.remove("product");
        document.remove("transaction");
        return entity;
    }
}
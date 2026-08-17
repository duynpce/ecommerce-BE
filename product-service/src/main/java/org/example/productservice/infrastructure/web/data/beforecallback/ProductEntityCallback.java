package org.example.productservice.infrastructure.web.data.beforecallback;

import org.bson.Document;
import org.example.productservice.infrastructure.web.data.entity.ProductEntity;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityCallback implements BeforeSaveCallback<ProductEntity> {
    @Override
    public ProductEntity onBeforeSave(ProductEntity entity, Document document, String collection) {
        document.remove("shop");
        return entity;
    }
}

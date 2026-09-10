package org.example.productservice.infrastructure.web.data.beforecallback;

import org.bson.Document;
import org.example.productservice.infrastructure.web.data.entity.TransactionEntity;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityCallback implements BeforeSaveCallback<TransactionEntity> {
    @Override
    public TransactionEntity onBeforeSave(TransactionEntity entity, Document document, String collection) {
        document.remove("product");
        return entity;
    }
}

package com.shopfloor.backend.olingo.business.implementations.products;

import com.shopfloor.backend.database.objects.ProductDBO;
import com.shopfloor.backend.database.repositories.ProductRepository;
import com.shopfloor.backend.olingo.business.generics.processors.ODataCollectionProcessor;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import org.springframework.stereotype.Component;

@Component
public class ProductCollectionProcessor extends ODataCollectionProcessor<ProductDBO> {

    public ProductCollectionProcessor(ProductRepository repository, ProductODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<ProductDBO>(mapper));
    }

}

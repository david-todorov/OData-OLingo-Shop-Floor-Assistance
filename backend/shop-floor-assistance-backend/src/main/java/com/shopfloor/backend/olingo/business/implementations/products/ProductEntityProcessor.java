package com.shopfloor.backend.olingo.business.implementations.products;


import com.shopfloor.backend.database.objects.ProductDBO;
import com.shopfloor.backend.olingo.business.generics.processors.ODataEntityProcessor;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;
@Component
public class ProductEntityProcessor extends ODataEntityProcessor<ProductDBO> {

    @Autowired
    public ProductEntityProcessor(JpaSpecificationExecutor<ProductDBO> repository, ProductODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<ProductDBO>(mapper));
    }

}

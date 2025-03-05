package com.shopfloor.backend.olingo.business.implementations.products;


import com.shopfloor.backend.database.objects.ProductDBO;
import com.shopfloor.backend.olingo.business.generics.processors.ODataEntityProcessor;
import com.shopfloor.backend.olingo.database.repositories.ProductODataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Processor for handling ProductDBO entity.
 * Extends the ODataEntityProcessor to provide specific functionality for ProductDBO.
 * This class provides concrete implementations for the abstract methods of the ODataEntityProcessor.
 * For handling ProductDBO entities, the ProductODataRepository and ProductService are used.
 *
 * @author David Todorov (https://github.com/david-todorov)
 * */
@Component
public class ProductEntityProcessor extends ODataEntityProcessor<ProductDBO> {

    /**
     * Constructor for the ProductEntityProcessor.
     *
     * @param repository the ProductODataRepository used for querying the database
     * @param service the ProductService used for processing the data
     */
    @Autowired
    public ProductEntityProcessor(ProductODataRepository repository, ProductService service) {
        super(repository, service);
    }

}

package com.shopfloor.backend.olingo.business.implementations.products;

import com.shopfloor.backend.database.objects.ProductDBO;
import com.shopfloor.backend.olingo.business.generics.processors.ODataCollectionProcessor;
import com.shopfloor.backend.olingo.database.repositories.ProductODataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Processor for handling collections of ProductDBO entities.
 * Extends the ODataCollectionProcessor to provide specific functionality for ProductDBO.
 * This class provides concrete implementations for the abstract methods of the ODataCollectionProcessor.
 * For handling EquipmentDBO entities, the ProductODataRepository and ProductService are used.
 *
 * @author David Todorov (https://github.com/david-todorov)
 */
@Component
public class ProductCollectionProcessor extends ODataCollectionProcessor<ProductDBO> {

    /**
     * Constructor for the ProductCollectionProcessor.
     *
     * @param repository the OrderODataRepository used for querying the database
     * @param service the OrderService used for processing the data
     */
    @Autowired
    public ProductCollectionProcessor(ProductODataRepository repository, ProductService service) {
        super(repository, service);
    }

}

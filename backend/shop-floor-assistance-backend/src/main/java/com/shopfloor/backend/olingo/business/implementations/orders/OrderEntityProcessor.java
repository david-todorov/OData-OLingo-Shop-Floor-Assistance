package com.shopfloor.backend.olingo.business.implementations.orders;

import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.olingo.business.generics.processors.ODataEntityProcessor;
import com.shopfloor.backend.olingo.database.repositories.OrderODataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Processor for handling OrderDBO entity.
 * Extends the ODataEntityProcessor to provide specific functionality for OrderDBO.
 * This class provides concrete implementations for the abstract methods of the ODataEntityProcessor.
 * For handling OrderDBO entities, the OrderODataRepository and OrderService are used.
 *
 * @author David Todorov (https://github.com/david-todorov)
 * */
@Component
public class OrderEntityProcessor extends ODataEntityProcessor<OrderDBO> {

    /**
     * Constructor for the OrderEntityProcessor.
     *
     * @param repository the OrderODataRepository used for querying the database
     * @param service the OrderService used for processing the data
     */
    @Autowired
    public OrderEntityProcessor(OrderODataRepository repository, OrderService service) {
        super(repository, service);
    }
}

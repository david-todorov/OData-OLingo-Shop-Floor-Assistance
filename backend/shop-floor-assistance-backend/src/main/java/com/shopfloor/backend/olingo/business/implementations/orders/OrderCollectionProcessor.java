package com.shopfloor.backend.olingo.business.implementations.orders;

import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.database.repositories.OrderRepository;
import com.shopfloor.backend.olingo.business.generics.processors.ODataCollectionProcessor;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderCollectionProcessor extends ODataCollectionProcessor<OrderDBO> {

    @Autowired
    public OrderCollectionProcessor(OrderRepository repository, OrderODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<OrderDBO>(mapper));
    }

}

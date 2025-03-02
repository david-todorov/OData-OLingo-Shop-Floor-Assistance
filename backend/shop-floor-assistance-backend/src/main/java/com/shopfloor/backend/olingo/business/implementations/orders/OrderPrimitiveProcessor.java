package com.shopfloor.backend.olingo.business.implementations.orders;

import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.olingo.business.generics.processors.ODataPrimitiveProcessor;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

@Component
public class OrderPrimitiveProcessor extends ODataPrimitiveProcessor<OrderDBO> {
    public OrderPrimitiveProcessor(JpaSpecificationExecutor<OrderDBO> repository, OrderODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<OrderDBO>(mapper));
    }
}

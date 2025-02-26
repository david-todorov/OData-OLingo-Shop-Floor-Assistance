package com.shopfloor.backend.olingo.business.processors.orders;

import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.olingo.business.processors.generics.ODataPrimitiveProcessor;
import com.shopfloor.backend.olingo.business.processors.generics.utils.projections.ODataProjectionBuilder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

@Component
public class OrderPrimitiveProcessor extends ODataPrimitiveProcessor<OrderDBO> {
    public OrderPrimitiveProcessor(JpaSpecificationExecutor<OrderDBO> repository, OrderODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<OrderDBO>(mapper));
    }
}

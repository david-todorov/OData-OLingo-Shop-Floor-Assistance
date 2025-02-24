package com.shopfloor.backend.olingo.business.processors.equipments;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.database.repositories.EquipmentRepository;
import com.shopfloor.backend.olingo.business.processors.generics.ODataCollectionProcessor;
import com.shopfloor.backend.olingo.business.processors.generics.utils.projections.ODataProjectionBuilder;
import com.shopfloor.backend.olingo.business.processors.orders.OrderODataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

@Component
public class EquipmentCollectionProcessor extends ODataCollectionProcessor<EquipmentDBO> {

    @Autowired
    public EquipmentCollectionProcessor(EquipmentRepository repository, EquipmentODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<EquipmentDBO>(mapper));
    }
}

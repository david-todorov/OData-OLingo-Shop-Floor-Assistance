package com.shopfloor.backend.olingo.business.implementations.equipments;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.olingo.business.generics.processors.ODataPrimitiveProcessor;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

@Component
public class EquipmentPrimitiveProcessor extends ODataPrimitiveProcessor<EquipmentDBO> {

    public EquipmentPrimitiveProcessor(JpaSpecificationExecutor<EquipmentDBO> repository, EquipmentODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<EquipmentDBO>(mapper));
    }
}

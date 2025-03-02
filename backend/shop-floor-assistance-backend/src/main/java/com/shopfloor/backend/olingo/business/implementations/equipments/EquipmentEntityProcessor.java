package com.shopfloor.backend.olingo.business.implementations.equipments;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.database.repositories.EquipmentRepository;
import com.shopfloor.backend.olingo.business.generics.processors.ODataEntityProcessor;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EquipmentEntityProcessor extends ODataEntityProcessor<EquipmentDBO> {
    @Autowired
    public EquipmentEntityProcessor(EquipmentRepository repository, EquipmentODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<EquipmentDBO>(mapper));
    }
}

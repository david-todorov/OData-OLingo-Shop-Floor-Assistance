package com.shopfloor.backend.olingo.business.implementations.equipments;


import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.database.repositories.EquipmentRepository;
import com.shopfloor.backend.olingo.business.generics.processors.ODataCollectionProcessor;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EquipmentCollectionProcessor extends ODataCollectionProcessor<EquipmentDBO> {

    @Autowired
    public EquipmentCollectionProcessor(EquipmentRepository repository, EquipmentODataMapper mapper) {
        super(repository, new ODataProjectionBuilder<EquipmentDBO>(mapper));
    }
}

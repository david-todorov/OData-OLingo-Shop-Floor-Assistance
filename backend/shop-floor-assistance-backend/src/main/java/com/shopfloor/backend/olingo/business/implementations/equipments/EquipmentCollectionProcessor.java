package com.shopfloor.backend.olingo.business.implementations.equipments;


import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.olingo.business.generics.processors.ODataCollectionProcessor;
import com.shopfloor.backend.olingo.database.repositories.EquipmentODataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Processor for handling collections of EquipmentDBO entities.
 * Extends the ODataCollectionProcessor to provide specific functionality for EquipmentDBO.
 * This class provides concrete implementations for the abstract methods of the ODataCollectionProcessor.
 * For handling EquipmentDBO entities, the EquipmentODataRepository and EquipmentService are used.
 *
 * @author David Todorov (https://github.com/david-todorov)
 */
@Component
public class EquipmentCollectionProcessor extends ODataCollectionProcessor<EquipmentDBO> {

    /**
     * Constructor for the EquipmentCollectionProcessor.
     *
     * @param repository the EquipmentODataRepository used for querying the database
     * @param service the EquipmentService used for processing the data
     */
    @Autowired
    public EquipmentCollectionProcessor(EquipmentODataRepository repository, EquipmentService service) {
        super(repository, service);
    }
}

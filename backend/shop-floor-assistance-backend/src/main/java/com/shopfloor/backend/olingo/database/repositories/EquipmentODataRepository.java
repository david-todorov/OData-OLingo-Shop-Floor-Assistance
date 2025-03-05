package com.shopfloor.backend.olingo.database.repositories;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.olingo.database.ODataRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for EquipmentDBO entities.
 * Extends the generic ODataRepository interface.
 * @Author David Todorov (https://github.com/david-todorov)
 */
@Repository
public interface EquipmentODataRepository extends ODataRepository<EquipmentDBO> {
}

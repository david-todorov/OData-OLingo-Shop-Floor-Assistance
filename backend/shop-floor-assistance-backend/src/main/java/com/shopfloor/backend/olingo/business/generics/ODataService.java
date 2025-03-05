package com.shopfloor.backend.olingo.business.generics;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;

/**
 * Interface for OData services, providing methods to create and update entities and DBOs.
 *
 * @param <T> the type of the DBO (DataBase Object)
 * @author David Todorov (https://github.com/david-todorov)
 */
public interface ODataService<T> {

    /**
     * Creates an OData Entity from the given DBO (DataBase Object).
     *
     * @param dbo the DataBase Object to be converted into an OData Entity
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData Entity
     */
    Entity createEntityFrom(T dbo, int expandDepth);

    /**
     * Creates an OData Entity Collection from the given DBOs (DataBase Objects).
     *
     * @param dbos the DataBase Objects to be converted into an OData Entity Collection
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData Entity Collection
     */
    EntityCollection createEntityCollectionFrom(Iterable<T> dbos, int expandDepth);

    /**
     * Creates a DBO (DataBase Object) from the given OData Entity.
     *
     * @param entity the OData Entity to be converted into a DBO
     * @param creatorId the ID of the user who created the entity
     * @return the created DBO
     */
    T createDBOFrom(Entity entity, long creatorId);

    /**
     * Updates the given DBO (DataBase Object) with the data from the given OData Entity.
     *
     * @param targetDBO the DBO to be updated
     * @param sourceEntity the OData Entity containing the updated data
     * @param updaterId the ID of the user who updated the entity
     * @return the updated DBO
     */
    T updateDBOFrom(T targetDBO, Entity sourceEntity, long updaterId);

    /**
     * Clears the references of the given DBO (DataBase Object).
     *
     * @param dbo the DBO whose references should be cleared
     */
    void clearReferences(T dbo);

}

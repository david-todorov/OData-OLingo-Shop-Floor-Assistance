package com.shopfloor.backend.olingo.business.implementations.equipments;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.olingo.business.generics.ODataService;
import com.shopfloor.backend.olingo.business.implementations.orders.OrderService;
import com.shopfloor.backend.olingo.presentation.EdmProvider;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service for handling EquipmentDBO entities.
 * Implements the ODataService interface to provide specific functionality for EquipmentDBO.
 * This class provides concrete implementations for the abstract methods of the ODataService.
 * For handling EquipmentDBO entities, the EquipmentService is used.
 *
 * @author David Todorov (https://github.com/david-todorov)
 * */
@Service
public class EquipmentService implements ODataService<EquipmentDBO> {


    private String ES_EQUIPMENTS_NAME = EdmProvider.ES_EQUIPMENTS_NAME;

    /**
     * Creates an OData Entity from the given EquipmentDBO.
     *
     * @param dbo the EquipmentDBO to be converted into an OData Entity
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData Entity
     */
    @Override
    public Entity createEntityFrom(EquipmentDBO dbo, int expandDepth) {
        Entity equipmentEntity = new Entity();

        equipmentEntity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, dbo.getId()));
        equipmentEntity.addProperty(new Property(null, "EquipmentNumber", ValueType.PRIMITIVE, dbo.getEquipmentNumber()));
        equipmentEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, dbo.getName()));
        equipmentEntity.addProperty(new Property(null, "Type", ValueType.PRIMITIVE, dbo.getType()));
        equipmentEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, dbo.getDescription()));
        equipmentEntity.addProperty(new Property(null, "CreatedBy", ValueType.PRIMITIVE, dbo.getCreatedBy()));
        equipmentEntity.addProperty(new Property(null, "UpdatedBy", ValueType.PRIMITIVE, dbo.getUpdatedBy()));
        equipmentEntity.addProperty(new Property(null, "CreatedAt", ValueType.PRIMITIVE, dbo.getCreatedAt()));
        equipmentEntity.addProperty(new Property(null, "UpdatedAt", ValueType.PRIMITIVE, dbo.getUpdatedAt()));

        equipmentEntity.setId(createEntityId(equipmentEntity, "Id", null));

        addOrdersCollectionNavigationProperty(equipmentEntity, "Orders", dbo.getOrders(), expandDepth);

        return equipmentEntity;
    }

    /**
     * Creates an OData EntityCollection from the given iterable of EquipmentDBO.
     *
     * @param dbos the iterable of EquipmentDBO to be converted into an OData EntityCollection
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData EntityCollection
     */
    @Override
    public EntityCollection createEntityCollectionFrom(Iterable<EquipmentDBO> dbos, int expandDepth) {
        EntityCollection entityCollection = new EntityCollection();

        for (EquipmentDBO entity : dbos) {
            entityCollection.getEntities().add(this.createEntityFrom(entity, expandDepth));
        }

        return entityCollection;
    }

    /**
     * Creates an EquipmentDBO from the given OData Entity.
     *
     * @param entity the OData Entity to be converted into an EquipmentDBO
     * @param creatorId the ID of the user who created the entity
     * @return the created EquipmentDBO
     */
    @Override
    public EquipmentDBO createDBOFrom(Entity entity, long creatorId) {
        EquipmentDBO equipmentDBO = new EquipmentDBO();

        equipmentDBO.setEquipmentNumber(this.getPropertyAsString(entity, "EquipmentNumber"));
        equipmentDBO.setName(this.getPropertyAsString(entity, "Name"));
        equipmentDBO.setType(this.getPropertyAsString(entity, "Type"));
        equipmentDBO.setDescription(this.getPropertyAsString(entity, "Description"));
        equipmentDBO.setCreatedBy(creatorId);
        equipmentDBO.setCreatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        return equipmentDBO;
    }

    /**
     * Updates the given EquipmentDBO with the data from the given OData Entity.
     *
     * @param targetDBO the EquipmentDBO to be updated
     * @param sourceEntity the OData Entity containing the updated data
     * @param updaterId the ID of the user who updated the entity
     * @return the updated EquipmentDBO
     */
    @Override
    public EquipmentDBO updateDBOFrom(EquipmentDBO targetDBO, Entity sourceEntity, long updaterId) {

        targetDBO.setEquipmentNumber(this.getPropertyAsString(sourceEntity, "EquipmentNumber"));
        targetDBO.setName(this.getPropertyAsString(sourceEntity, "Name"));
        targetDBO.setType(this.getPropertyAsString(sourceEntity, "Type"));
        targetDBO.setDescription(this.getPropertyAsString(sourceEntity, "Description"));

        targetDBO.setUpdatedBy(updaterId);
        targetDBO.setUpdatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        return targetDBO;
    }

    /**
     * Clears the references of the given EquipmentDBO.
     *
     * @param dbo the EquipmentDBO whose references should be cleared
     */
    @Override
    public void clearReferences(EquipmentDBO dbo) {
        dbo.clearOrderReferences();
    }

    /**
     * Creates a URI for the given entity and its ID property.
     *
     * @param entity the entity for which the URI is to be created
     * @param idPropertyName the name of the ID property
     * @param navigationName the name of the navigation property (optional)
     * @return the created URI
     * @throws ODataRuntimeException if the URI syntax is incorrect
     */
    private URI createEntityId(Entity entity, String idPropertyName, String navigationName) {
        try {
            StringBuilder sb = new StringBuilder(ES_EQUIPMENTS_NAME).append("(");
            final Property property = entity.getProperty(idPropertyName);
            sb.append(property.asPrimitive()).append(")");
            if (navigationName != null) {
                sb.append("/").append(navigationName);

                Property navIdProperty = entity.getProperty("Id"); // Adjust this if the ID property has a different name
                if (navIdProperty != null) {
                    sb.append("(").append(navIdProperty.asPrimitive()).append(")");
                }
            }
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create (Atom) id for entity: " + entity, e);
        }
    }

    /**
     * Adds a navigation property for orders to the given equipment entity.
     *
     * @param equipmentEntity the equipment entity to which the navigation property is to be added
     * @param navigationName the name of the navigation property
     * @param orderDBOS the list of OrderDBO to be added as navigation property
     * @param expandDepth the depth to which related entities should be expanded
     */
    private void addOrdersCollectionNavigationProperty(Entity equipmentEntity, String navigationName, List<OrderDBO> orderDBOS, int expandDepth) {
        Link navLink = new Link();
        navLink.setTitle(navigationName);
        navLink.setType(Constants.ENTITY_SET_NAVIGATION_LINK_TYPE);
        navLink.setRel(Constants.NS_ASSOCIATION_LINK_REL + navigationName);

        EntityCollection entityCollection = new EntityCollection();

        if (orderDBOS != null && expandDepth > 0) {
            for (OrderDBO orderDBO : orderDBOS) {
                if (orderDBO != null) {
                    // Convert each OrderDBO to an OData Entity
                    Entity relatedEntity = new OrderService().createEntityFrom(orderDBO, expandDepth - 1);
                    entityCollection.getEntities().add(relatedEntity);
                    relatedEntity.setId(createEntityId(equipmentEntity, "Id", navigationName));
                }
            }
        }

        // Set inline entity set for navigation link
        navLink.setInlineEntitySet(entityCollection);

        // Add navigation link to the product entity
        equipmentEntity.getNavigationLinks().add(navLink);
    }

    /**
     * Retrieves the value of the specified property as a string.
     *
     * @param entity the entity from which the property value is to be retrieved
     * @param propertyName the name of the property
     * @return the property value as a string, or null if the property is not found or has no value
     */
    private String getPropertyAsString(Entity entity, String propertyName) {
        Property property = entity.getProperty(propertyName);
        return (property != null && property.getValue() != null) ? property.getValue().toString() : null;
    }

}

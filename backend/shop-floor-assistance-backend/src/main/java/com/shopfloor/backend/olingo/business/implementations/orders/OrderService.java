package com.shopfloor.backend.olingo.business.implementations.orders;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.database.objects.ProductDBO;
import com.shopfloor.backend.olingo.business.generics.ODataService;
import com.shopfloor.backend.olingo.business.implementations.equipments.EquipmentService;
import com.shopfloor.backend.olingo.business.implementations.products.ProductService;
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
 * Service for handling OrderDBO entities.
 * Implements the ODataService interface to provide specific functionality for OrderDBO.
 * This class provides concrete implementations for the abstract methods of the ODataService.
 * For handling OrderDBO entities, the OrderService is used.
 *
 * @author David Todorov (https://github.com/david-todorov)
 * */
@Service
public class OrderService implements ODataService<OrderDBO> {

    private String ES_ORDERS_NAME = EdmProvider.ES_ORDERS_NAME;

    /**
     * Creates an OData Entity from the given OrderDBO.
     *
     * @param dbo the OrderDBO to be converted into an OData Entity
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData Entity
     */
    @Override
    public Entity createEntityFrom(OrderDBO dbo, int expandDepth) {
        Entity orderEntity = new Entity();

        // Add primitive properties
        orderEntity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, dbo.getId()));
        orderEntity.addProperty(new Property(null, "OrderNumber", ValueType.PRIMITIVE, dbo.getOrderNumber()));
        orderEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, dbo.getName()));
        orderEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, dbo.getDescription()));
        orderEntity.addProperty(new Property(null, "CreatedBy", ValueType.PRIMITIVE, dbo.getCreatedBy()));
        orderEntity.addProperty(new Property(null, "UpdatedBy", ValueType.PRIMITIVE, dbo.getUpdatedBy()));
        orderEntity.addProperty(new Property(null, "CreatedAt", ValueType.PRIMITIVE, dbo.getCreatedAt()));
        orderEntity.addProperty(new Property(null, "UpdatedAt", ValueType.PRIMITIVE, dbo.getUpdatedAt()));
        orderEntity.addProperty(new Property(null, "TotalTimeRequired", ValueType.PRIMITIVE, dbo.getTotalTimeRequired()));

        orderEntity.setId(createEntityId(orderEntity, "Id", null));


        addProductNavigationProperty(orderEntity, "ProductBefore", dbo.getBeforeProduct(), expandDepth);
        addProductNavigationProperty(orderEntity, "ProductAfter", dbo.getAfterProduct(), expandDepth);

        addEquipmentsCollectionNavigationProperty(orderEntity, "Equipments", dbo.getEquipment(), expandDepth);
        return orderEntity;
    }

    /**
     * Creates an OData EntityCollection from the given iterable of OrderDBO.
     *
     * @param dbos the iterable of OrderDBO to be converted into an OData EntityCollection
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData EntityCollection
     */
    @Override
    public EntityCollection createEntityCollectionFrom(Iterable<OrderDBO> dbos, int expandDepth) {
        EntityCollection entityCollection = new EntityCollection();

        for (OrderDBO orderDBO : dbos) {
            entityCollection.getEntities().add(this.createEntityFrom(orderDBO, expandDepth));
        }

        return entityCollection;
    }

    /**
     * Creates an OrderDBO from the given OData Entity.
     *
     * @param entity the OData Entity to be converted into an OrderDBO
     * @param creatorId the ID of the creator
     * @return the created OrderDBO
     */
    @Override
    public OrderDBO createDBOFrom(Entity entity, long creatorId) {

        OrderDBO orderDBO = new OrderDBO();

        orderDBO.setOrderNumber(this.getPropertyAsString(entity, "OrderNumber"));
        orderDBO.setName(this.getPropertyAsString(entity, "Name"));
        orderDBO.setDescription(this.getPropertyAsString(entity, "Description"));
        orderDBO.setCreatedBy(creatorId);
        orderDBO.setCreatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        return orderDBO;
    }

    /**
     * Updates the target OrderDBO with values from the source OData Entity.
     *
     * @param targetDBO the target OrderDBO to be updated
     * @param sourceEntity the source OData Entity containing updated values
     * @param updaterId the ID of the updater
     * @return the updated OrderDBO
     */
    @Override
    public OrderDBO updateDBOFrom(OrderDBO targetDBO, Entity sourceEntity, long updaterId) {

        targetDBO.setOrderNumber(this.getPropertyAsString(sourceEntity, "OrderNumber"));
        targetDBO.setName(this.getPropertyAsString(sourceEntity, "Name"));
        targetDBO.setDescription(this.getPropertyAsString(sourceEntity, "Description"));

        targetDBO.setUpdatedBy(updaterId);
        targetDBO.setUpdatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        return targetDBO;
    }

    /**
     * Clears the references of the given OrderDBO.
     *
     * @param dbo the OrderDBO whose references should be cleared
     */
    @Override
    public void clearReferences(OrderDBO dbo) {
        dbo.clearAfterProduct();
        dbo.clearBeforeProduct();
        dbo.clearEquipmentList();
        dbo.clearExecutions();
    }

    /**
     * Adds a navigation property for a product to the given order entity.
     *
     * @param orderEntity the order entity to which the navigation property is to be added
     * @param navigationName the name of the navigation property
     * @param productDBO the ProductDBO to be added as a navigation property
     * @param expandDepth the depth to which related entities should be expanded
     */
    private void addProductNavigationProperty(Entity orderEntity, String navigationName, ProductDBO productDBO, int expandDepth) {
        if (productDBO != null && expandDepth > 0) {
            // Create the navigation link and set its title
            Link link = new Link();
            link.setTitle(navigationName);
            link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
            link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navigationName);

            // Let Olingo handle URL building
            // Create the related entity for the navigation link
            Entity relatedEntity = new ProductService().createEntityFrom(productDBO, expandDepth - 1);

            relatedEntity.setId(createEntityId(orderEntity, "Id", navigationName));
            // Use Olingo's method to set the inline entity for the navigation link
            link.setInlineEntity(relatedEntity);

            // Add the navigation link to the entity
            orderEntity.getNavigationLinks().add(link);
        }
    }

    /**
     * Adds a navigation property for a list of equipments to the given order entity.
     *
     * @param orderEntity the order entity to which the navigation property is to be added
     * @param navigationName the name of the navigation property
     * @param equipmentDBOS the list of EquipmentDBO to be added as a navigation property
     * @param expandDepth the depth to which related entities should be expanded
     */
    private void addEquipmentsCollectionNavigationProperty(Entity orderEntity, String navigationName, List<EquipmentDBO> equipmentDBOS, int expandDepth) {
        Link navLink = new Link();
        navLink.setTitle(navigationName);
        navLink.setType(Constants.ENTITY_SET_NAVIGATION_LINK_TYPE);
        navLink.setRel(Constants.NS_ASSOCIATION_LINK_REL + navigationName);

        EntityCollection entityCollection = new EntityCollection();

        if (equipmentDBOS != null && expandDepth > 0) {
            for (EquipmentDBO equipmentDBO : equipmentDBOS) {
                if (equipmentDBO != null) {
                    // Convert each OrderDBO to an OData Entity
                    Entity relatedEntity = new EquipmentService().createEntityFrom(equipmentDBO, expandDepth - 1);
                    entityCollection.getEntities().add(relatedEntity);
                    relatedEntity.setId(createEntityId(orderEntity, "Id", navigationName));
                }
            }
        }



        // Set inline entity set for navigation link
        navLink.setInlineEntitySet(entityCollection);

        // Add navigation link to the product entity
        orderEntity.getNavigationLinks().add(navLink);
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
            StringBuilder sb = new StringBuilder(ES_ORDERS_NAME).append("(");
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

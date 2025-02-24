package com.shopfloor.backend.olingo.business.processors.orders;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.database.objects.ProductDBO;
import com.shopfloor.backend.olingo.business.processors.equipments.EquipmentODataMapper;
import com.shopfloor.backend.olingo.business.processors.generics.ODataEntityMapper;
import com.shopfloor.backend.olingo.business.processors.products.ProductODataMapper;
import com.shopfloor.backend.olingo.presentation.EdmProvider;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class OrderODataMapper implements ODataEntityMapper<OrderDBO> {

    private String ES_ORDERS_NAME = EdmProvider.ES_ORDERS_NAME;

    @Override
    public Entity mapEntity(OrderDBO orderDBO, int expandDepth) {
        Entity orderEntity = new Entity();

        // Add primitive properties
        orderEntity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, orderDBO.getId()));
        orderEntity.addProperty(new Property(null, "OrderNumber", ValueType.PRIMITIVE, orderDBO.getOrderNumber()));
        orderEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, orderDBO.getName()));
        orderEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, orderDBO.getDescription()));
        orderEntity.addProperty(new Property(null, "CreatedBy", ValueType.PRIMITIVE, orderDBO.getCreatedBy()));
        orderEntity.addProperty(new Property(null, "UpdatedBy", ValueType.PRIMITIVE, orderDBO.getUpdatedBy()));
        orderEntity.addProperty(new Property(null, "CreatedAt", ValueType.PRIMITIVE, orderDBO.getCreatedAt()));
        orderEntity.addProperty(new Property(null, "UpdatedAt", ValueType.PRIMITIVE, orderDBO.getUpdatedAt()));
        orderEntity.addProperty(new Property(null, "TotalTimeRequired", ValueType.PRIMITIVE, orderDBO.getTotalTimeRequired()));

        orderEntity.setId(createEntityId(orderEntity, "Id", null));


        addProductNavigationProperty(orderEntity, "ProductBefore", orderDBO.getBeforeProduct(), expandDepth);
        addProductNavigationProperty(orderEntity, "ProductAfter", orderDBO.getAfterProduct(), expandDepth);

        addEquipmentsCollectionNavigationProperty(orderEntity, "Equipments", orderDBO.getEquipment(), expandDepth);
        return orderEntity;
    }

    @Override
    public EntityCollection mapEntityCollection(Iterable<OrderDBO> entities, int expandDepth) {
        EntityCollection entityCollection = new EntityCollection();

        for (OrderDBO orderDBO : entities) {
            entityCollection.getEntities().add(mapEntity(orderDBO, expandDepth));
        }

        return entityCollection;
    }

    @Override
    public URI createEntityId(Entity entity, String idPropertyName, String navigationName) {
        try {
            StringBuilder sb = new StringBuilder(ES_ORDERS_NAME).append("(");
            final Property property = entity.getProperty(idPropertyName);
            sb.append(property.asPrimitive()).append(")");
            if(navigationName != null) {
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

    private void addProductNavigationProperty(Entity orderEntity, String navigationName, ProductDBO productDBO, int expandDepth) {
        if (productDBO != null && expandDepth > 0) {
            // Create the navigation link and set its title
            Link link = new Link();
            link.setTitle(navigationName);
            link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
            link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navigationName);

            // Let Olingo handle URL building
            // Create the related entity for the navigation link
            Entity relatedEntity = new ProductODataMapper().mapEntity(productDBO, expandDepth - 1);

            relatedEntity.setId(createEntityId(orderEntity, "Id", navigationName));
            // Use Olingo's method to set the inline entity for the navigation link
            link.setInlineEntity(relatedEntity);

            // Add the navigation link to the entity
            orderEntity.getNavigationLinks().add(link);
        }
    }

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
                    Entity relatedEntity = new EquipmentODataMapper().mapEntity(equipmentDBO, expandDepth - 1);
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
}

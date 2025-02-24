package com.shopfloor.backend.olingo.business.processors.equipments;

import com.shopfloor.backend.database.objects.EquipmentDBO;
import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.olingo.business.processors.generics.ODataEntityMapper;
import com.shopfloor.backend.olingo.business.processors.orders.OrderODataMapper;
import com.shopfloor.backend.olingo.presentation.EdmProvider;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class EquipmentODataMapper implements ODataEntityMapper<EquipmentDBO> {

    private String ES_EQUIPMENTS_NAME = EdmProvider.ES_EQUIPMENTS_NAME;
    @Override
    public Entity mapEntity(EquipmentDBO entity, int expandDepth) {
        Entity equipmentEntity = new Entity();

        equipmentEntity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, entity.getId()));
        equipmentEntity.addProperty(new Property(null, "EquipmentNumber", ValueType.PRIMITIVE, entity.getEquipmentNumber()));
        equipmentEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, entity.getName()));
        equipmentEntity.addProperty(new Property(null, "Type", ValueType.PRIMITIVE, entity.getType()));
        equipmentEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, entity.getDescription()));
        equipmentEntity.addProperty(new Property(null,"CreatedBy", ValueType.PRIMITIVE, entity.getCreatedBy()));
        equipmentEntity.addProperty(new Property(null, "UpdatedBy", ValueType.PRIMITIVE, entity.getUpdatedBy()));
        equipmentEntity.addProperty(new Property(null, "CreatedAt", ValueType.PRIMITIVE, entity.getCreatedAt()));
        equipmentEntity.addProperty(new Property(null, "UpdatedAt", ValueType.PRIMITIVE, entity.getUpdatedAt()));

        equipmentEntity.setId(createEntityId(equipmentEntity, "Id", null));

        addOrdersCollectionNavigationProperty(equipmentEntity, "Orders", entity.getOrders(), expandDepth);

        return equipmentEntity;
    }

    @Override
    public EntityCollection mapEntityCollection(Iterable<EquipmentDBO> entities, int expandDepth) {
        EntityCollection entityCollection = new EntityCollection();

        for (EquipmentDBO entity : entities) {
            entityCollection.getEntities().add(mapEntity(entity, expandDepth));
        }

        return entityCollection;
    }

    @Override
    public URI createEntityId(Entity entity, String idPropertyName, String navigationName) {
        try {
            StringBuilder sb = new StringBuilder(ES_EQUIPMENTS_NAME).append("(");
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
                    Entity relatedEntity = new OrderODataMapper().mapEntity(orderDBO, expandDepth - 1);
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


}

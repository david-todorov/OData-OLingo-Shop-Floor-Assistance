package com.shopfloor.backend.olingo.business.processors.products;

import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.database.objects.ProductDBO;
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
public class ProductODataMapper implements ODataEntityMapper<ProductDBO> {

    private String ES_PRODUCTS_NAME = EdmProvider.ES_PRODUCTS_NAME;

    @Override
    public Entity mapEntity(ProductDBO productDBO, int expandDepth) {

        Entity productEntity = new Entity();


        // Add primitive properties
        productEntity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, productDBO.getId()));
        productEntity.addProperty(new Property(null, "ProductNumber", ValueType.PRIMITIVE, productDBO.getProductNumber()));
        productEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, productDBO.getName()));
        productEntity.addProperty(new Property(null, "Type", ValueType.PRIMITIVE, productDBO.getType()));
        productEntity.addProperty(new Property(null, "Country", ValueType.PRIMITIVE, productDBO.getCountry()));
        productEntity.addProperty(new Property(null, "PackageSize", ValueType.PRIMITIVE, productDBO.getPackageSize()));
        productEntity.addProperty(new Property(null, "PackageType", ValueType.PRIMITIVE, productDBO.getPackageType()));
        productEntity.addProperty(new Property(null, "Language", ValueType.PRIMITIVE, productDBO.getLanguage()));
        productEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, productDBO.getDescription()));
        productEntity.addProperty(new Property(null, "CreatedBy", ValueType.PRIMITIVE, productDBO.getCreatedBy()));
        productEntity.addProperty(new Property(null, "UpdatedBy", ValueType.PRIMITIVE, productDBO.getUpdatedBy()));
        productEntity.addProperty(new Property(null, "CreatedAt", ValueType.PRIMITIVE, productDBO.getCreatedAt()));
        productEntity.addProperty(new Property(null, "UpdatedAt", ValueType.PRIMITIVE, productDBO.getUpdatedAt()));

        productEntity.setId(createEntityId(productEntity, "Id", null));

        // Add navigation properties (e.g., OrdersAsBefore, OrdersAsAfter)
        addOrdersCollectionNavigationProperty(productEntity, "OrdersAsBefore", productDBO.getOrdersAsBeforeProduct(), expandDepth);
        addOrdersCollectionNavigationProperty(productEntity, "OrdersAsAfter", productDBO.getOrdersAsAfterProduct(), expandDepth);

        return productEntity;
    }

    @Override
    public EntityCollection mapEntityCollection(Iterable<ProductDBO> productDBOs, int expandDepth) {
        EntityCollection productsCollection = new EntityCollection();

        for (ProductDBO productDBO : productDBOs) {
            productsCollection.getEntities().add(mapEntity(productDBO, expandDepth));
        }

        return productsCollection;
    }

    @Override
    public URI createEntityId(Entity entity, String idPropertyName, String navigationName) {
        try {
            StringBuilder sb = new StringBuilder(ES_PRODUCTS_NAME).append("(");

            final Property property = entity.getProperty(idPropertyName);
            sb.append(property.asPrimitive()).append(")");
            if(navigationName != null) {
                sb.append("/").append(navigationName);

                // If the navigation property has an ID, append it as well
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


    private void addOrdersCollectionNavigationProperty(Entity productEntity, String navigationName, List<OrderDBO> orderDBOS, int expandDepth) {
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
                    relatedEntity.setId(createEntityId(relatedEntity, "Id", navigationName));
                    entityCollection.getEntities().add(relatedEntity);
                }
            }
        }

        // Set inline entity set for navigation link
        navLink.setInlineEntitySet(entityCollection);

        // Add navigation link to the product entity
        productEntity.getNavigationLinks().add(navLink);
    }


}

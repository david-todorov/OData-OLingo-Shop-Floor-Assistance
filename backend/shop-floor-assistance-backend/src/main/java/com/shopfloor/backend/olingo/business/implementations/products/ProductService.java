package com.shopfloor.backend.olingo.business.implementations.products;

import com.shopfloor.backend.database.objects.OrderDBO;
import com.shopfloor.backend.database.objects.ProductDBO;
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
 * Service for handling ProductDBO entities.
 * Implements the ODataService interface to provide specific functionality for ProductDBO.
 * This class provides concrete implementations for the abstract methods of the ODataService.
 * For handling ProductDBO entities, the ProductService is used.
 *
 * @author David Todorov (https://github.com/david-todorov)
 * */
@Service
public class ProductService implements ODataService<ProductDBO> {

    private String ES_PRODUCTS_NAME = EdmProvider.ES_PRODUCTS_NAME;

    /**
     * Creates an OData Entity from the given ProductDBO.
     *
     * @param dbo the ProductDBO to be converted into an OData Entity
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData Entity
     */
    @Override
    public Entity createEntityFrom(ProductDBO dbo, int expandDepth) {
        Entity productEntity = new Entity();


        // Add primitive properties
        productEntity.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, dbo.getId()));
        productEntity.addProperty(new Property(null, "ProductNumber", ValueType.PRIMITIVE, dbo.getProductNumber()));
        productEntity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, dbo.getName()));
        productEntity.addProperty(new Property(null, "Type", ValueType.PRIMITIVE, dbo.getType()));
        productEntity.addProperty(new Property(null, "Country", ValueType.PRIMITIVE, dbo.getCountry()));
        productEntity.addProperty(new Property(null, "PackageSize", ValueType.PRIMITIVE, dbo.getPackageSize()));
        productEntity.addProperty(new Property(null, "PackageType", ValueType.PRIMITIVE, dbo.getPackageType()));
        productEntity.addProperty(new Property(null, "Language", ValueType.PRIMITIVE, dbo.getLanguage()));
        productEntity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, dbo.getDescription()));
        productEntity.addProperty(new Property(null, "CreatedBy", ValueType.PRIMITIVE, dbo.getCreatedBy()));
        productEntity.addProperty(new Property(null, "UpdatedBy", ValueType.PRIMITIVE, dbo.getUpdatedBy()));
        productEntity.addProperty(new Property(null, "CreatedAt", ValueType.PRIMITIVE, dbo.getCreatedAt()));
        productEntity.addProperty(new Property(null, "UpdatedAt", ValueType.PRIMITIVE, dbo.getUpdatedAt()));

        productEntity.setId(createEntityId(productEntity, "Id", null));

        // Add navigation properties (e.g., OrdersAsBefore, OrdersAsAfter)
        addOrdersCollectionNavigationProperty(productEntity, "OrdersAsBefore", dbo.getOrdersAsBeforeProduct(), expandDepth);
        addOrdersCollectionNavigationProperty(productEntity, "OrdersAsAfter", dbo.getOrdersAsAfterProduct(), expandDepth);

        return productEntity;
    }

    /**
     * Creates an OData EntityCollection from the given iterable of ProductDBO.
     *
     * @param dbos the iterable of ProductDBO to be converted into an OData EntityCollection
     * @param expandDepth the depth to which related entities should be expanded
     * @return the created OData EntityCollection
     */
    @Override
    public EntityCollection createEntityCollectionFrom(Iterable<ProductDBO> dbos, int expandDepth) {
        EntityCollection productsCollection = new EntityCollection();

        for (ProductDBO productDBO : dbos) {
            productsCollection.getEntities().add(this.createEntityFrom(productDBO, expandDepth));
        }

        return productsCollection;
    }

    /**
     * Creates a ProductDBO from the given OData Entity.
     *
     * @param entity the OData Entity to be converted into a ProductDBO
     * @param creatorId the ID of the user who created the entity
     * @return the created ProductDBO
     */
    @Override
    public ProductDBO createDBOFrom(Entity entity, long creatorId) {
       ProductDBO productDBO = new ProductDBO();
       productDBO.setProductNumber(this.getPropertyAsString(entity, "ProductNumber"));
       productDBO.setName(this.getPropertyAsString(entity, "Name"));
       productDBO.setType(this.getPropertyAsString(entity, "Type"));
       productDBO.setCountry(this.getPropertyAsString(entity,  "Country"));
       productDBO.setPackageSize(this.getPropertyAsString(entity, "PackageSize"));
       productDBO.setPackageType(this.getPropertyAsString(entity, "PackageType"));
       productDBO.setLanguage(this.getPropertyAsString(entity,  "Language"));
       productDBO.setDescription(this.getPropertyAsString(entity,  "Description"));

       productDBO.setCreatedBy(creatorId);
       productDBO.setCreatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));


       return productDBO;
    }

    /**
     * Updates the given ProductDBO with the data from the given OData Entity.
     *
     * @param targetDBO the ProductDBO to be updated
     * @param sourceEntity the OData Entity containing the new data
     * @param updaterId the ID of the user who updated the entity
     * @return the updated ProductDBO
     */
    @Override
    public ProductDBO updateDBOFrom(ProductDBO targetDBO, Entity sourceEntity, long updaterId) {

        targetDBO.setProductNumber(this.getPropertyAsString(sourceEntity, "ProductNumber"));
        targetDBO.setName(this.getPropertyAsString(sourceEntity, "Name"));
        targetDBO.setType(this.getPropertyAsString(sourceEntity, "Type"));
        targetDBO.setCountry(this.getPropertyAsString(sourceEntity,  "Country"));
        targetDBO.setPackageSize(this.getPropertyAsString(sourceEntity, "PackageSize"));
        targetDBO.setPackageType(this.getPropertyAsString(sourceEntity, "PackageType"));
        targetDBO.setLanguage(this.getPropertyAsString(sourceEntity,  "Language"));
        targetDBO.setDescription(this.getPropertyAsString(sourceEntity,  "Description"));

        targetDBO.setUpdatedBy(updaterId);
        targetDBO.setUpdatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        return targetDBO;
    }

    /**
     * Clears the references of the given ProductDBO.
     *
     * @param dbo the ProductDBO whose references should be cleared
     */
    @Override
    public void clearReferences(ProductDBO dbo) {

        dbo.clearOrderReferences();

    }

    /**
     * Adds a navigation property for orders to the given product entity.
     *
     * @param productEntity the product entity to which the navigation property is to be added
     * @param navigationName the name of the navigation property
     * @param orderDBOS the list of OrderDBO to be added as navigation property
     * @param expandDepth the depth to which related entities should be expanded
     */
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
                    Entity relatedEntity = new OrderService().createEntityFrom(orderDBO, expandDepth - 1);
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
            StringBuilder sb = new StringBuilder(ES_PRODUCTS_NAME).append("(");

            final Property property = entity.getProperty(idPropertyName);
            sb.append(property.asPrimitive()).append(")");
            if (navigationName != null) {
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

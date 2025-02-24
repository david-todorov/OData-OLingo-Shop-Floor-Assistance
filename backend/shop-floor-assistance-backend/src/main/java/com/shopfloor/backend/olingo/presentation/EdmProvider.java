package com.shopfloor.backend.olingo.presentation;

import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class EdmProvider extends CsdlAbstractEdmProvider {

    // Service Namespace
    public static final String NAMESPACE = "ShopFloor";

    // EDM Container
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types Names
    public static final String ET_PRODUCT_NAME = "Product";
    public static final FullQualifiedName ET_PRODUCT_FQN = new FullQualifiedName(NAMESPACE, ET_PRODUCT_NAME);

    public static final String ET_EQUIPMENT_NAME = "Equipment";
    public static final FullQualifiedName ET_EQUIPMENT_FQN = new FullQualifiedName(NAMESPACE, ET_EQUIPMENT_NAME);

    public static final String ET_ORDER_NAME = "Order";
    public static final FullQualifiedName ET_ORDER_FQN = new FullQualifiedName(NAMESPACE, ET_ORDER_NAME);

    // Entity Set Names
    public static final String ES_PRODUCTS_NAME = "Products";
    public static final String ES_EQUIPMENTS_NAME = "Equipments";
    public static final String ES_ORDERS_NAME = "Orders";

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        CsdlEntityType entityType = null;

        if(entityTypeName.equals(ET_ORDER_FQN)){

            // Basic properties of an Order
            CsdlProperty id = new CsdlProperty().setName("Id")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty orderNumber =  new CsdlProperty().setName("OrderNumber")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty description = new CsdlProperty().setName("Description")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty createdBy = new CsdlProperty().setName("CreatedBy")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty updatedBy = new CsdlProperty().setName("UpdatedBy")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty createdAt = new CsdlProperty().setName("CreatedAt")
                    .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());
            CsdlProperty updatedAt = new CsdlProperty().setName("UpdatedAt")
                    .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());
            CsdlProperty totalRequiredTime = new CsdlProperty().setName("TotalTimeRequired")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());


            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("Id");

            //TODO Navigation properties

            //TODO: "ProductBefore" as Product, One to One
            List<CsdlNavigationProperty> navigationPropertyList = new ArrayList<CsdlNavigationProperty>();
            CsdlNavigationProperty productBefore = new CsdlNavigationProperty()
                    .setName("ProductBefore")
                    .setType(ET_PRODUCT_FQN)
                    .setCollection(false)
                    .setPartner("OrdersAsBefore");
            navigationPropertyList.add(productBefore);

            //TODO: "ProductAfter" as Product, One to One
            CsdlNavigationProperty productAfter = new CsdlNavigationProperty()
                    .setName("ProductAfter")
                    .setType(ET_PRODUCT_FQN)
                    .setCollection(false)
                    .setPartner("OrdersAsAfter");
            navigationPropertyList.add(productAfter);

            //TODO: "Equipments" as Equipment, Many to Many
            CsdlNavigationProperty equipmentsNavProp = new CsdlNavigationProperty()
                    .setName("Equipments")          // Navigation property in Order
                    .setType(ET_EQUIPMENT_FQN)      // Points to Equipment entity
                    .setCollection(true)            // Many-to-many: collection
                    .setPartner("Orders");
            navigationPropertyList.add(equipmentsNavProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_ORDER_NAME);
            entityType.setProperties(
                    Arrays.asList(
                            id,
                            orderNumber,
                            name,
                            description,
                            createdBy,
                            updatedBy,
                            createdAt,
                            updatedAt,
                            totalRequiredTime)
            );
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navigationPropertyList);


        }
        else if(entityTypeName.equals(ET_EQUIPMENT_FQN)){

            //TODO: Basic properties of an Equipment
            CsdlProperty id = new CsdlProperty().setName("Id")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty equipmentNumber = new CsdlProperty().setName("EquipmentNumber")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty type = new CsdlProperty().setName("Type")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty description = new CsdlProperty().setName("Description")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty createdBy = new CsdlProperty().setName("CreatedBy")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty updatedBy = new CsdlProperty().setName("UpdatedBy")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty createdAt = new CsdlProperty().setName("CreatedAt")
                    .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());
            CsdlProperty updatedAt = new CsdlProperty().setName("UpdatedAt")
                    .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("Id");

            List<CsdlNavigationProperty> navigationPropertyList = new ArrayList<CsdlNavigationProperty>();

            CsdlNavigationProperty ordersNavProp = new CsdlNavigationProperty()
                    .setName("Orders")
                    .setType(ET_ORDER_FQN)
                    .setCollection(true)
                    .setPartner("Equipments");
            navigationPropertyList.add(ordersNavProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_EQUIPMENT_NAME);
            entityType.setProperties(
                    Arrays.asList(
                            id,
                            equipmentNumber,
                            name,
                            type,
                            description,
                            createdBy,
                            updatedBy,
                            createdAt,
                            updatedAt
                    )
            );
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navigationPropertyList);

        }
        else if(entityTypeName.equals(ET_PRODUCT_FQN)){
            //TODO: Basic properties of a Product
            CsdlProperty id = new CsdlProperty().setName("Id")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty productNumber = new CsdlProperty().setName("ProductNumber")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty type = new CsdlProperty().setName("Type")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty country = new CsdlProperty().setName("Country")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty packageSize = new CsdlProperty().setName("PackageSize")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty packageType = new CsdlProperty().setName("PackageType")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty language = new CsdlProperty().setName("Language")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty description = new CsdlProperty().setName("Description")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty createdBy = new CsdlProperty().setName("CreatedBy")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty updatedBy = new CsdlProperty().setName("UpdatedBy")
                    .setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
            CsdlProperty createdAt = new CsdlProperty().setName("CreatedAt")
                    .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());
            CsdlProperty updatedAt = new CsdlProperty().setName("UpdatedAt")
                    .setType(EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("Id");

            CsdlPropertyRef propertyRefProductNumber = new CsdlPropertyRef();
            propertyRefProductNumber.setName("ProductNumber");




            //TODO Navigation properties
            List<CsdlNavigationProperty> navigationPropertyList = new ArrayList<CsdlNavigationProperty>();

            CsdlNavigationProperty ordersAsBefore = new CsdlNavigationProperty()
                    .setName("OrdersAsBefore")
                    .setType(ET_ORDER_FQN)
                    .setCollection(true)
                    .setPartner("ProductBefore");
            navigationPropertyList.add(ordersAsBefore);

            CsdlNavigationProperty ordersAsAfter = new CsdlNavigationProperty()
                    .setName("OrdersAsAfter")
                    .setType(ET_ORDER_FQN)
                    .setCollection(true)
                    .setPartner("ProductAfter");
            navigationPropertyList.add(ordersAsAfter);




            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_PRODUCT_NAME);
            entityType.setProperties(
                    Arrays.asList(
                            id,
                            productNumber,
                            name,
                            type,
                            country,
                            packageSize,
                            packageType,
                            language,
                            description,
                            createdBy,
                            updatedBy,
                            createdAt,
                            updatedAt
                    )
            );
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navigationPropertyList);

        }

        return entityType;
    }


    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        CsdlEntitySet entitySet = null;

        if (entitySetName.equals(ES_ORDERS_NAME)) {
            // Define navigation bindings for Orders entity set
            List<CsdlNavigationPropertyBinding> navigationBindings = new ArrayList<>();
            navigationBindings.add(new CsdlNavigationPropertyBinding().setPath("ProductBefore").setTarget(ES_PRODUCTS_NAME));
            navigationBindings.add(new CsdlNavigationPropertyBinding().setPath("ProductAfter").setTarget(ES_PRODUCTS_NAME));
            navigationBindings.add(new CsdlNavigationPropertyBinding().setPath("Equipments").setTarget(ES_EQUIPMENTS_NAME));

            entitySet = new CsdlEntitySet()
                    .setName(ES_ORDERS_NAME)
                    .setType(ET_ORDER_FQN)
                    .setNavigationPropertyBindings(navigationBindings);
        }
        else if (entitySetName.equals(ES_EQUIPMENTS_NAME)) {
            // Define navigation bindings for Equipments entity set
            List<CsdlNavigationPropertyBinding> navigationBindings = new ArrayList<>();
            navigationBindings.add(new CsdlNavigationPropertyBinding().setPath("Orders").setTarget(ES_ORDERS_NAME));

            entitySet = new CsdlEntitySet()
                    .setName(ES_EQUIPMENTS_NAME)
                    .setType(ET_EQUIPMENT_FQN)
                    .setNavigationPropertyBindings(navigationBindings);
        }
        else if (entitySetName.equals(ES_PRODUCTS_NAME)) {
            // Define navigation bindings for Products entity set
            List<CsdlNavigationPropertyBinding> navigationBindings = new ArrayList<>();
            navigationBindings.add(new CsdlNavigationPropertyBinding().setPath("OrdersAsBefore").setTarget(ES_ORDERS_NAME));
            navigationBindings.add(new CsdlNavigationPropertyBinding().setPath("OrdersAsAfter").setTarget(ES_ORDERS_NAME));

            entitySet = new CsdlEntitySet()
                    .setName(ES_PRODUCTS_NAME)
                    .setType(ET_PRODUCT_FQN)
                    .setNavigationPropertyBindings(navigationBindings);
        }

        return entitySet;
    }



    @Override
    public CsdlEntityContainer getEntityContainer() {
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.add(getEntitySet(CONTAINER, ES_PRODUCTS_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_EQUIPMENTS_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_ORDERS_NAME));

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }


    @Override
    public List<CsdlSchema> getSchemas() {
        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        entityTypes.add(getEntityType(ET_PRODUCT_FQN));
        entityTypes.add(getEntityType(ET_EQUIPMENT_FQN));
        entityTypes.add(getEntityType(ET_ORDER_FQN));
        schema.setEntityTypes(entityTypes);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        schemas.add(schema);

        return schemas;
    }


    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

        // This method is invoked when displaying the Service Document at e.g.
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }

        return null;
    }

}

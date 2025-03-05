# OData API Description

## Metadata

The metadata document describes the structure of the API. It is available at the following URL:
`/odata/$metadata`
---
## Entities Sets
- `Products` - The collection of products.
- `Orders` - The collection of orders.
- `Equipments` - The collection of equipment.
---
## Product
#### Basic Properties:
- `Id` : `Long` - The unique identifier of the product.
- `ProductNumber` : `String` - The unique product identifier of the product.
- `Name` : `String` - The name of the product.
- `Type` : `String` - The type of the product.
- `Country` : `String` - The country of the product.
- `PackageSize` : `String` - The package size of the product.
- `PackageType` : `String` - The package type of the product.
- `Language` : `String` - The language of the product.
- `Description` : `String` - The description of the product.
- `CreatedBy` : `Long` - The unique identifier of the user who created the product.
- `UpdatedBy` : `Long` - The unique identifier of the user who updated the product.
- `CreatedAt` : `DateTime` - The date and time when the product was created.
- `UpdatedAt` : `DateTime` - The date and time when the product was updated.
#### Referential Property/s
- `Id` : `Long` - The unique identifier of the product.
#### Navigation Property/s
- `OrdersAsBefore` : `List<Order>` - The collection of orders that consider the product as before product.
- `OrdersAsAfter` : `List<Order>` - The collection of orders that consider the product as after product.
---
## Equipment
#### Basic Properties:
- `Id` : `Long` - The unique identifier of the equipment.
- `EquipmentNumber` : `String` - The unique equipment identifier of the equipment.
- `Name` : `String` - The name of the equipment.
- `Type` : `String` - The type of the equipment.
- `Description` : `String` - The description of the equipment.
- `CreatedBy` : `Long` - The unique identifier of the user who created the equipment.
- `UpdatedBy` : `Long` - The unique identifier of the user who updated the equipment.
- `CreatedAt` : `DateTime` - The date and time when the equipment was created.
- `UpdatedAt` : `DateTime` - The date and time when the equipment was updated.
#### Referential Property/s
- `Id` : `Long` - The unique identifier of the equipment.
#### Navigation Property/s
- `Orders` : `List<Order>` - The collection of orders that consider the equipment.
---
## Order
#### Basic Properties:
- `Id` : `Long` - The unique identifier of the order.
- `OrderNumber` : `String` - The unique order identifier of the order.
- `Name` : `String` - The name of the order.
- `Description` : `String` - The description of the order.
- `CreatedBy` : `Long` - The unique identifier of the user who created the order.
- `UpdatedBy` : `Long` - The unique identifier of the user who updated the order.
- `CreatedAt` : `DateTime` - The date and time when the order was created.
- `UpdatedAt` : `DateTime` - The date and time when the order was updated.
- `TotalTimeRequired` : `Integer` - The total time required for the order.
#### Referential Property/s
- `Id` : `Long` - The unique identifier of the order.
#### Navigation Property/s
- `ProductAsBefore` : `Product` - The product that is considered as before product in the order.
- `ProductAsAfter` : `Product` - The product that is considered as after product in the order.
- `Equipments` : `List<Equipment>` - The equipments that are considered in the order.
---
## Entity Collection requests
The following collection requests are available:
- `GET /odata/Products` - Retrieves the collection of products.
- `GET /odata/Orders` - Retrieves the collection of orders.
- `GET /odata/Equipments` - Retrieves the collection of equipments.

## Query Options
The following query options are supported for collection requests:
- `$filter` - Filters the collection based on the specified criteria.
- `$orderby` - Orders the collection based on the specified criteria.
- `$top` - Retrieves the specified number of items from the collection.
- `$skip` - Skips the specified number of items from the collection.
- `$count` - Retrieves the total count of items in the collection.
- `$select` - Retrieves the specified properties of the items in the collection.
- `$expand` - Expands the specified navigation properties of the items in the collection.
- `$format` - Retrieves the collection in the specified format (JSON or XML).
- `$search` - Not supported.
---
## Single Entity requests
### Products
- `GET /odata/Products(Id)` - Retrieves the product with the specified identifier.
- `POST /odata/Products` - Creates a new product, which is contained in the request body.
- `PUT /odata/Products(Id)` - Updates the product with the specified identifier, which is contained in the request body.
- `DELETE /odata/Products(Id)` - Deletes the product with the specified identifier.
### Orders
- `GET /odata/Orders(Id)` - Retrieves the order with the specified identifier.
- `POST /odata/Orders` - Creates a new order, which is contained in the request body.
- `PUT /odata/Orders(Id)` - Updates the order with the specified identifier, which is contained in the request body.
- `DELETE /odata/Orders(Id)` - Deletes the order with the specified identifier.
### Equipments
- `GET /odata/Equipments(Id)` - Retrieves the equipment with the specified identifier.
- `POST /odata/Equipments` - Creates a new equipment, which is contained in the request body.
- `PUT /odata/Equipments(Id)` - Updates the equipment with the specified identifier, which is contained in the request body.
- `DELETE /odata/Equipments(Id)` - Deletes the equipment with the specified identifier.

## Query Options
The following query options are supported for single entity requests:
- `$select` - Retrieves the specified properties of the entity.
- `$expand` - Expands the specified navigation properties of the entity.
- `$format` - Retrieves the entity in the specified format (JSON or XML).

# DISCLAIMER:
### The current implementation of the API supports only 'first level' querying the navigation properties are accessible only through $expand query option.

# Supported:
- `GET /odata/Products(Id)?$expand=OrdersAsBefore,OrdersAsAfter` - Retrieves the product with the specified identifier and expands the OrdersAsBefore and OrdersAsAfter navigation properties.
- `GET /odata/Orders?$expand=ProductAsBefore,ProductAsAfter,Equipments` - Retrieves all orders and expands the ProductAsBefore, ProductAsAfter, and Equipments navigation properties.

# Not Supported:
- `GET /odata/Products(id)/OrdersAsBefore(id)` - Retrieves the order with the specified identifier that is associated with the product.
- `GET /odata/Orders(id)/Equipments(id)` - Retrieves the equipment with the specified identifier that is associated with the order.
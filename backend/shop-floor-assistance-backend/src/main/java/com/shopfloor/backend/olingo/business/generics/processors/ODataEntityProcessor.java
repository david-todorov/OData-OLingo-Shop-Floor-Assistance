package com.shopfloor.backend.olingo.business.generics.processors;

import com.shopfloor.backend.olingo.business.generics.ODataService;
import com.shopfloor.backend.olingo.business.generics.specifications.ODataSpecificationBuilder;
import com.shopfloor.backend.olingo.database.ODataRepository;
import jakarta.transaction.Transactional;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * ODataEntityProcessor is a generic class that implements the EntityProcessor interface.
 * It provides methods to handle CRUD operations for OData entities.
 *
 * @param <T> the type of the entity
 * @author David Todorov (https://github.com/david-todorov)
 */
public class ODataEntityProcessor<T> implements EntityProcessor {

    /**
     * The maximum depth for entity expansion.
     */
    private static final int MAX_DEPTH = 2;

    /**
     * Repository for accessing and managing entities of type T.
     * The repository is used to retrieve entities from the database. Using JpaRepository
     * as the repository interface allows for easy CRUD operations.
     * Additionally, it inherits the JpaSpecificationExecutor interface which provides
     * methods for querying entities using Specifications.
     */
    private final ODataRepository<T> repository;

    /**
     * Service for handling OData operations for entities of type T.
     * This service is used to perform various operations such as creating, reading, updating, and deleting entities.
     */
    private final ODataService<T> service;

    /**
     * OData instance used for creating serializers and building context URLs.
     */
    private OData odata;

    /**
     * Service metadata used for accessing the EDM (Entity Data Model) and other service-related metadata.
     */
    private ServiceMetadata serviceMetadata;

    /**
     * Constructs an ODataEntityProcessor with the specified repository and service.
     *
     * @param repository the repository for accessing and managing entities of type T
     * @param service the service for handling OData operations for entities of type T
     */
    public ODataEntityProcessor(ODataRepository<T> repository, ODataService<T> service) {
        this.repository = repository;
        this.service = service;
    }

    /**
     * Reads an OData entity based on the provided OData request and query options.
     * This method handles various OData query options such as $select and $expand.
     * It retrieves the entity from the repository, converts it to an OData Entity,
     * and serializes the response.
     *  @Transactional annotation is used to ensure that the operation is executed within a JPA transaction and the object/s are not detached
     *
     * @param oDataRequest the OData request
     * @param oDataResponse the OData response
     * @param uriInfo the URI information containing the OData query options
     * @param contentType the content type of the response
     * @throws ODataApplicationException if an OData application error occurs
     * @throws ODataLibraryException if an OData library error occurs
     */
    @Override
    @Transactional
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
            throws ODataApplicationException, ODataLibraryException {

        Entity toReturnEntity = null;

        // Extract EntitySet and EntityType from URI
        EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // Extract $select and $expand options from URI
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();

        // Build the specification to query the entity
        Specification<T> specification = buildSpecification(uriInfo);

        // Retrieve entity from repository
        T dbEntity = getEntityFromRepository(specification);

        // Map the database entity to OData entity
        toReturnEntity = service.createEntityFrom(dbEntity, MAX_DEPTH);

        // Build Context URL with $select and $expand options
        ContextURL contextUrl = buildContextUrl(edmEntitySet, edmEntityType, selectOption, expandOption);

        // Serialize the entity to OData format
        SerializerResult serializerResult = serializeEntity(edmEntityType, toReturnEntity, selectOption, expandOption, contextUrl, contentType);

        // Set the response
        setResponse(oDataResponse, serializerResult, contentType);
    }

    /**
     * Creates an OData entity based on the provided OData request and query options.
     * This method handles the deserialization of the OData entity from the request body,
     * converts it to a database entity, saves it, and then serializes the response.
     *  @Transactional annotation is used to ensure that the operation is executed within a JPA transaction and the object/s are not detached
     *
     * @param oDataRequest the OData request
     * @param oDataResponse the OData response
     * @param uriInfo the URI information containing the OData query options
     * @param contentType the content type of the request
     * @param contentType1 the content type of the response
     * @throws ODataApplicationException if an OData application error occurs
     * @throws ODataLibraryException if an OData library error occurs
     */
    @Override
    @Transactional
    public void createEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1)
            throws ODataApplicationException, ODataLibraryException {

        // Extract EntitySet and EntityType from URI
        EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // Deserialize the OData entity from the request body
        Entity requestEntity = deserializeEntity(oDataRequest, contentType, edmEntityType);

        // Convert OData entity to DB entity and save
        T dbo = service.createDBOFrom(requestEntity, 0L);
        dbo = repository.save(dbo);

        // Map the saved database entity back to OData entity
        Entity createdEntity = service.createEntityFrom(dbo, MAX_DEPTH);

        // Build Context URL with $select and $expand options
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();
        ContextURL contextUrl = buildContextUrl(edmEntitySet, edmEntityType, selectOption, expandOption);

        // Serialize the created entity to OData format
        SerializerResult serializerResult = serializeEntity(edmEntityType, createdEntity, selectOption, expandOption, contextUrl, contentType);

        // Set the response
        setResponse(oDataResponse, serializerResult, contentType);
    }

    /**
     * Updates an OData entity based on the provided OData request and query options.
     * This method handles the deserialization of the OData entity from the request body,
     * updates the corresponding database entity, and then serializes the response.
     * @Transactional annotation is used to ensure that the operation is executed within a JPA transaction and the object/s are not detached
     *
     * @param oDataRequest the OData request
     * @param oDataResponse the OData response
     * @param uriInfo the URI information containing the OData query options
     * @param contentType the content type of the request
     * @param contentType1 the content type of the response
     * @throws ODataApplicationException if an OData application error occurs
     * @throws ODataLibraryException if an OData library error occurs
     */
    @Override
    @Transactional
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1)
            throws ODataApplicationException, ODataLibraryException {

        // Extract EntitySet and EntityType from URI
        EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // Build the specification to query the entity
        Specification<T> specification = buildSpecification(uriInfo);

        // Retrieve the database entity from the repository
        T dbEntity = getEntityFromRepository(specification);

        // Deserialize the OData entity from the request body
        Entity requestEntity = deserializeEntity(oDataRequest, contentType, edmEntityType);

        // Update the database entity
        dbEntity = service.updateDBOFrom(dbEntity, requestEntity, 999);

        // Map the updated database entity back to OData entity
        Entity updatedEntity = service.createEntityFrom(dbEntity, MAX_DEPTH);

        // Build Context URL with $select and $expand options
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();
        ContextURL contextUrl = buildContextUrl(edmEntitySet, edmEntityType, selectOption, expandOption);

        // Serialize the updated entity to OData format
        SerializerResult serializerResult = serializeEntity(edmEntityType, updatedEntity, selectOption, expandOption, contextUrl, contentType);

        // Set the response
        setResponse(oDataResponse, serializerResult, contentType);
    }

    /**
     * Deletes an OData entity based on the provided OData request and query options.
     * This method builds a specification to identify the entity to delete, retrieves the entity from the repository,
     * clears any references, deletes the entity from the database, and sets the response status to NO_CONTENT.
     * @Transactional annotation is used to ensure that the operation is executed within a JPA transaction and the object/s are not detached
     *
     * @param oDataRequest the OData request
     * @param oDataResponse the OData response
     * @param uriInfo the URI information containing the OData query options
     * @throws ODataApplicationException if an OData application error occurs
     * @throws ODataLibraryException if an OData library error occurs
     */
    @Override
    @Transactional
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {

        // Build the specification to identify the entity to delete
        Specification<T> specification = buildSpecification(uriInfo);

        // Retrieve the database entity to delete
        T dboToDelete = getEntityFromRepository(specification);

        // Clear references before deleting the entity
        service.clearReferences(dboToDelete);

        // Delete the entity from the database
        repository.delete(dboToDelete);

        // Set the response to NO_CONTENT
        oDataResponse.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }

    /**
     * Initializes the ODataEntityProcessor with the provided OData and ServiceMetadata instances.
     * This method is called by the OData library to set up the processor with the necessary OData context and metadata.
     *
     * @param oData the OData instance used for creating serializers and building context URLs
     * @param serviceMetadata the service metadata used for accessing the EDM (Entity Data Model) and other service-related metadata
     */
    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.odata = oData;
        this.serviceMetadata = serviceMetadata;
    }

    /**
     * Retrieves the EdmEntitySet from the provided UriInfo.
     * Currently only the first UriResource is considered, which should be the EntitySet.
     *
     * @param uriInfo the URI information containing the OData query options
     * @return the EdmEntitySet extracted from the URI
     */
    private EdmEntitySet getEdmEntitySet(UriInfo uriInfo) {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        return uriResourceEntitySet.getEntitySet();
    }

    /**
     * Builds a Specification for querying the entity based on the provided UriInfo.
     * The Specification is the primary key or composite key of the entity, both work
     * Contained in the UriInfo is the key predicates that are used to build the Specification.
     *
     * @param uriInfo the URI information containing the OData query options
     * @return the Specification for querying the entity
     * @throws ODataApplicationException if an error occurs while building the specification
     */
    private Specification<T> buildSpecification(UriInfo uriInfo) throws ODataApplicationException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        ODataSpecificationBuilder<T> specificationBuilder = new ODataSpecificationBuilder<>();

        return specificationBuilder.addComposeKey(uriResourceEntitySet).build();
    }

    /**
     * Retrieves an entity from the repository based on the provided Specification.
     * If the entity is not found, an ODataApplicationException is thrown.
     *
     * @param specification the Specification for querying the entity
     * @return the entity retrieved from the repository
     * @throws ODataApplicationException if the entity is not found
     */
    private T getEntityFromRepository(Specification<T> specification) throws ODataApplicationException {
        return repository.findOne(specification)
                .orElseThrow(() -> new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null));
    }

    /**
     * Deserializes an OData entity from the request body.
     * The deserialization is based on the provided content type and EDM entity type.
     *
     * @param oDataRequest the OData request
     * @param contentType the content type of the request
     * @param edmEntityType the EDM entity type
     * @return the deserialized OData entity
     * @throws DeserializerException if an error occurs during deserialization
     */
    private Entity deserializeEntity(ODataRequest oDataRequest, ContentType contentType, EdmEntityType edmEntityType) throws DeserializerException {
        ODataDeserializer deserializer = odata.createDeserializer(contentType);
        return deserializer.entity(oDataRequest.getBody(), edmEntityType).getEntity();
    }

    /**
     * Builds a ContextURL for the OData entity based on the provided parameters.
     * The ContextURL includes the entity set, select list, and entity suffix.
     *
     * @param edmEntitySet the EDM entity set
     * @param edmEntityType the EDM entity type
     * @param selectOption the $select query option
     * @param expandOption the $expand query option
     * @return the built ContextURL
     * @throws SerializerException if an error occurs during URL building
     */
    private ContextURL buildContextUrl(EdmEntitySet edmEntitySet,
                                       EdmEntityType edmEntityType,
                                       SelectOption selectOption,
                                       ExpandOption expandOption) throws SerializerException {

        String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);

        return ContextURL.with().entitySet(edmEntitySet)
                .selectList(selectList)
                .suffix(ContextURL.Suffix.ENTITY).build();
    }

    /**
     * Serializes an OData entity to the specified content type.
     * The serialization is based on the provided EDM entity type, OData entity, select option, expand option, context URL, and content type.
     *
     * @param edmEntityType the EDM entity type
     * @param entity the OData entity to serialize
     * @param selectOption the $select query option
     * @param expandOption the $expand query option
     * @param contextUrl the context URL
     * @param contentType the content type of the response
     * @return the serialized result
     * @throws SerializerException if an error occurs during serialization
     */
    private SerializerResult serializeEntity(EdmEntityType edmEntityType,
                                             Entity entity,
                                             SelectOption selectOption,
                                             ExpandOption expandOption,
                                             ContextURL contextUrl,
                                             ContentType contentType) throws SerializerException {

        EntitySerializerOptions opts = EntitySerializerOptions.with()
                .contextURL(contextUrl)
                .select(selectOption)
                .expand(expandOption)
                .build();

        ODataSerializer serializer = odata.createSerializer(contentType);

        return serializer.entity(serviceMetadata, edmEntityType, entity, opts);
    }

    /**
     * Sets the OData response with the serialized result and content type.
     *
     * @param oDataResponse the OData response
     * @param serializerResult the serialized result
     * @param contentType the content type of the response
     */
    private void setResponse(ODataResponse oDataResponse, SerializerResult serializerResult, ContentType contentType) {
        oDataResponse.setContent(serializerResult.getContent());
        oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
        oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
    }

}

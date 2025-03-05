package com.shopfloor.backend.olingo.business.generics.processors;

import com.shopfloor.backend.olingo.business.generics.ODataService;
import com.shopfloor.backend.olingo.business.generics.paginations.ODataPaginationBuilder;
import com.shopfloor.backend.olingo.business.generics.specifications.ODataSpecificationBuilder;
import com.shopfloor.backend.olingo.database.ODataRepository;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ODataCollectionProcessor is a generic class that implements the EntityCollectionProcessor interface.
 * It is responsible for processing OData entity collection requests.
 *
 * @param <T> the type of the entity
 * @author David Todorov (https://github.com/david-todorov)
 */
public class ODataCollectionProcessor<T> implements EntityCollectionProcessor {


    /**
     * The maximum depth for expanding entities in the OData response.
     */
    private final int MAX_DEPTH = 2;

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
     * Constructs an ODataCollectionProcessor with the specified repository and service.
     *
     * @param repository the repository for accessing and managing entities of type T
     * @param service the service for handling OData operations for entities of type T
     */
    public ODataCollectionProcessor(ODataRepository<T> repository, ODataService<T> service) {
        this.repository = repository;
        this.service = service;
    }

    /**
     * Reads an OData entity collection based on the provided OData request and query options.
     * This method handles various OData query options such as $filter, $orderby, $search, $top, $skip, $select, $expand, and $count.
     * It retrieves the filtered and paginated results from the repository, converts them to an OData EntityCollection,
     * and serializes the response.
     *
     * @param request the OData request
     * @param response the OData response
     * @param uriInfo the URI information containing the OData query options
     * @param contentType the content type of the response
     * @throws ODataApplicationException if an OData application error occurs
     * @throws ODataLibraryException if an OData library error occurs
     */
    @Override
    @Transactional
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType contentType)
            throws ODataApplicationException, ODataLibraryException {

        // 1. Extract OData query options
        FilterOption filterOption = uriInfo.getFilterOption();
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        SearchOption searchOption = uriInfo.getSearchOption();
        TopOption topOption = uriInfo.getTopOption();
        SkipOption skipOption = uriInfo.getSkipOption();
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();
        CountOption countOption = uriInfo.getCountOption();

        // 2. Build filter specifications, reflects $filter and $orderby
        Specification<T> specification = new ODataSpecificationBuilder<T>()
                .addFilter(filterOption)
                .addOrderBy(orderByOption)
                .addSearchOption(searchOption)
                .build();

        // 3. Get entity count, reflects $count
        long count = (countOption != null && countOption.getValue()) ? repository.count(specification) : -1;

        // 4. Build pagination, reflects  $top and $skip
        Pageable paging = new ODataPaginationBuilder()
                .addTopOption(topOption)
                .addSkipOption(skipOption)
                .build();

        // 5. Retrieve paginated and filtered results from the repository
        List<T> dbEntities = repository.findAll(specification, paging).getContent();

        // 6. Convert results to OData EntityCollection
        EntityCollection entityCollection = service.createEntityCollectionFrom(dbEntities, MAX_DEPTH);
        entityCollection.setCount((int) count);

        // 7. Retrieve entity set metadata
        String entitySetName = uriInfo.getUriResourceParts().get(0).getSegmentValue();
        EdmEntitySet edmEntitySet = serviceMetadata.getEdm().getEntityContainer().getEntitySet(entitySetName);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // 8. Build Context URL
        String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).selectList(selectList).build();

        // 9. Configure serialization options, reflects $select and $expand
        String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
                .contextURL(contextUrl)
                .select(selectOption)
                .expand(expandOption)
                .count(countOption)
                .id(id)
                .build();

        // 10. Serialize response
        ODataSerializer serializer = odata.createSerializer(contentType);
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, opts);

        // 11. Set response content and headers
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
    }

    /**
     * Initializes the ODataCollectionProcessor with the provided OData and ServiceMetadata instances.
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
}

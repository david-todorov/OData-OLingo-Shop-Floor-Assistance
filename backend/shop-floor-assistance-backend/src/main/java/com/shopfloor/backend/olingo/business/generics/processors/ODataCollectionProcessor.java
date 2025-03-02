package com.shopfloor.backend.olingo.business.generics.processors;

import com.shopfloor.backend.olingo.business.generics.paginations.ODataPaginationBuilder;
import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import com.shopfloor.backend.olingo.business.generics.specifications.ODataSpecificationBuilder;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ODataCollectionProcessor<T> implements EntityCollectionProcessor {

    private final JpaSpecificationExecutor<T> repository;

    private final ODataProjectionBuilder<T> projectionBuilder;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    public ODataCollectionProcessor(JpaSpecificationExecutor<T> repository, ODataProjectionBuilder<T> projectionBuilder) {
        this.repository = repository;
        this.projectionBuilder = projectionBuilder;
    }

    @Override
    @Transactional
    public void readEntityCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
            throws
            ODataApplicationException,
            ODataLibraryException {

        // The collection to be returned
        EntityCollection toReturnCollection = new EntityCollection();

        // Extract OData query options
        FilterOption filterOption = uriInfo.getFilterOption();
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        TopOption topOption = uriInfo.getTopOption();
        SkipOption skipOption = uriInfo.getSkipOption();
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();
        CountOption countOption = uriInfo.getCountOption();
        FormatOption formatOption = uriInfo.getFormatOption();

        // Build specifications and pagination
        ODataSpecificationBuilder<T> queryBuilder = new ODataSpecificationBuilder<>();
        ODataPaginationBuilder paginationBuilder = new ODataPaginationBuilder();

        // This reflects the $filter, $orderby
        Specification<T> specification = queryBuilder
                .addFilter(filterOption)
                .addOrderBy(orderByOption)
                .build();


        // This reflects the $top, $skip
        Pageable paging = paginationBuilder
                .addTopOption(topOption)
                .addSkipOption(skipOption)
                .build();

        // Retrieve paginated and filtered results from the repository
        List<T> dbEntities = repository.findAll(specification, paging).getContent();

        // Build the entity collection from the projection builder
        // Internally, this will call the ODataEntityMapper, which is specific to the entity set
        toReturnCollection = this.projectionBuilder.buildEntityCollectionFrom(dbEntities);


        String entitySetName = uriInfo.getUriResourceParts().get(0).getSegmentValue();
        EdmEntitySet edmEntitySet = serviceMetadata.getEdm().getEntityContainer()
                .getEntitySet(entitySetName);

        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // Build Context URL with $select and $expand
        String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
        ContextURL contextUrl = ContextURL.with()
                .entitySet(edmEntitySet)
                .selectList(selectList)
                .build();

        // Configure serialization options
        final String id = oDataRequest.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
                .contextURL(contextUrl)
                .select(selectOption)
                .expand(expandOption)
                .id(id)
                .build();

        // Serialize the entity collection
        ODataSerializer serializer = odata.createSerializer(contentType);
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, toReturnCollection, opts);

        // Configure the response object
        oDataResponse.setContent(serializerResult.getContent());
        oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
        oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.odata = oData;
        this.serviceMetadata = serviceMetadata;
    }
}

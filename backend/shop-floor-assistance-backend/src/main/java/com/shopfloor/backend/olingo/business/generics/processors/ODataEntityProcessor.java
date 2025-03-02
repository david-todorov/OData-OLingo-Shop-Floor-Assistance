package com.shopfloor.backend.olingo.business.generics.processors;

import com.shopfloor.backend.olingo.business.generics.projections.ODataProjectionBuilder;
import com.shopfloor.backend.olingo.business.generics.specifications.ODataSpecificationBuilder;
import jakarta.transaction.Transactional;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public class ODataEntityProcessor<T> implements EntityProcessor {

    private final JpaSpecificationExecutor<T> repository;

    private final ODataProjectionBuilder<T> projectionBuilder;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    public ODataEntityProcessor(JpaSpecificationExecutor<T> repository, ODataProjectionBuilder<T> projectionBuilder) {
        this.repository = repository;
        this.projectionBuilder = projectionBuilder;
    }

    @Override
    @Transactional
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
            throws ODataApplicationException,
            ODataLibraryException {

        //The OData entity which will be returned
        Entity toReturnEntity = null;

        //Extracting the root EntitySet
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        //Extracting the $select and $expand options
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();

        //SpecificationBuilder helps to dynamically construct a query, using specifications
        //Building the specifications according to the uriResourceEntitySet
        ODataSpecificationBuilder<T> specificationBuilder = new ODataSpecificationBuilder<>();
        Specification<T> specification = specificationBuilder
                .addComposeKey(uriResourceEntitySet)
                .build();

        //Retrieving the database entity according to the specifications
        //Otherwise throwing an exception
        T dbEntity = this.repository.findOne(specification)
                .orElseThrow(() -> new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null));

        //Mapping the Database Entity to OData Entity
        toReturnEntity = this.projectionBuilder.buildEntityFrom(dbEntity);



        // 4. serialize

        // we need the property names of the $select, in order to build the context URL
        String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet)
                .selectList(selectList)
                .suffix(ContextURL.Suffix.ENTITY).build();

        // make sure that $expand and $select are considered by the serializer
        // adding the selectOption to the serializerOpts will actually tell the lib to do the job
        EntitySerializerOptions opts = EntitySerializerOptions.with()
                .contextURL(contextUrl)
                .select(selectOption)
                .expand(expandOption)
                .build();

        ODataSerializer serializer = this.odata.createSerializer(contentType);
        SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, toReturnEntity, opts);

        // 5. configure the response object
        oDataResponse.setContent(serializerResult.getContent());
        oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
        oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());

    }

    @Override
    public void createEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1)
            throws
            ODataApplicationException,
            ODataLibraryException {

    }

    @Override
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1)
            throws
            ODataApplicationException,
            ODataLibraryException {

    }

    @Override
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

        //Extracting the root EntitySet
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);

        ODataSpecificationBuilder<T> specificationBuilder = new ODataSpecificationBuilder<>();

        Specification<T> specification = specificationBuilder.addComposeKey(uriResourceEntitySet).build();


    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.odata = oData;
        this.serviceMetadata = serviceMetadata;
    }

}

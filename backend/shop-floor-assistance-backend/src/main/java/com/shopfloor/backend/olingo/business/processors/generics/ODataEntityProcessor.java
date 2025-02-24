package com.shopfloor.backend.olingo.business.processors.generics;

import com.shopfloor.backend.olingo.business.processors.generics.utils.projections.ODataProjectionBuilder;
import com.shopfloor.backend.olingo.business.processors.generics.utils.specifications.ODataSpecificationBuilder;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
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
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType) throws ODataApplicationException, ODataLibraryException {

        // 1. retrieve the Entity Type
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2. retrieve the data from backend
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

        SelectOption selectOption = uriInfo.getSelectOption();
        ODataSpecificationBuilder<T> specificationBuilder = new ODataSpecificationBuilder<>();
        specificationBuilder.addComposeKey(keyPredicates, uriInfo);

    }

    @Override
    public void createEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.odata = oData;
        this.serviceMetadata = serviceMetadata;
    }
}

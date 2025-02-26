package com.shopfloor.backend.olingo.business.processors.generics;

import com.shopfloor.backend.olingo.business.processors.generics.utils.projections.ODataProjectionBuilder;
import com.shopfloor.backend.olingo.business.processors.generics.utils.specifications.ODataSpecificationBuilder;
import jakarta.transaction.Transactional;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.InputStream;
import java.util.List;

public class ODataPrimitiveProcessor<T> implements PrimitiveProcessor {

    private final JpaSpecificationExecutor<T> repository;

    private final ODataProjectionBuilder<T> projectionBuilder;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    public ODataPrimitiveProcessor(JpaSpecificationExecutor<T> repository, ODataProjectionBuilder<T> projectionBuilder) {
        this.repository = repository;
        this.projectionBuilder = projectionBuilder;
    }

    @Override
    @Transactional
    public void readPrimitive(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
            throws ODataApplicationException, ODataLibraryException {

        Property propertyToReturn = null;

        //Extracting the root EntitySet
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

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
        //It will contain the requested property
        Entity entity = this.projectionBuilder.buildEntityFrom(dbEntity);

        // Retrieve the requested (Edm) property the last segment is the Property
        // in our example, we know we have only primitive types in our model
        UriResourceProperty uriProperty = (UriResourceProperty) resourcePaths.get(resourcePaths.size() - 1);
        EdmProperty edmProperty = uriProperty.getProperty();
        String edmPropertyName = edmProperty.getName();
        EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) edmProperty.getType();

        // Retrieve the property data from the entity
        // Or throw an exception
        propertyToReturn = entity.getProperty(edmPropertyName);
        if (propertyToReturn == null) {
            throw new ODataApplicationException("Property not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null);
        }

        // Serialize
        Object value = propertyToReturn.getValue();
        if (value != null) {
            // Configure the serializer
            ODataSerializer serializer = odata.createSerializer(contentType);

            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).navOrPropertyPath(edmPropertyName).build();
            PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
            // Serialize
            SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, propertyToReturn, options);
            InputStream propertyStream = serializerResult.getContent();

            // Configure the response object
            oDataResponse.setContent(propertyStream);
            oDataResponse.setStatusCode(HttpStatusCode.OK.getStatusCode());
            oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
        } else {
            // in case there's no value for the property, we can skip the serialization
            oDataResponse.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        }
    }

    @Override
    public void updatePrimitive(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1)
            throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deletePrimitive(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.odata = oData;
        this.serviceMetadata = serviceMetadata;
    }
}

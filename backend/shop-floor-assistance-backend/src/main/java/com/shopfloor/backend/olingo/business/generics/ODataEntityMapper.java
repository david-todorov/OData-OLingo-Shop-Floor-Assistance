package com.shopfloor.backend.olingo.business.generics;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;

import java.net.URI;

public interface ODataEntityMapper<T> {

    Entity mapEntity(T entity, int expandDepth);

    EntityCollection mapEntityCollection(Iterable<T> entities, int expandDepth);

    URI createEntityId(Entity entity, String idPropertyName, String navigationName);

}

package com.shopfloor.backend.olingo.business.generics.projections;

import com.shopfloor.backend.olingo.business.generics.ODataEntityMapper;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import java.util.List;

public class ODataProjectionBuilder<T> {

    private ODataEntityMapper<T> entityMapper;

    private SelectOption selectOption;

    private ExpandOption expandOption;

    private final int MAX_DEPTH = 2;

    public ODataProjectionBuilder(ODataEntityMapper<T> entityMapper) {
        this.entityMapper = entityMapper;
    }

    public EntityCollection buildEntityCollectionFrom(List<T> DBOs) {
        return this.entityMapper.mapEntityCollection(DBOs, MAX_DEPTH);
    }

    public Entity buildEntityFrom(T DBO) {
        return this.entityMapper.mapEntity(DBO, MAX_DEPTH);
    }

}

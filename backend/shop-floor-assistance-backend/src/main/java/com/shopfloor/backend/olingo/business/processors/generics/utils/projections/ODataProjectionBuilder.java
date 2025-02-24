package com.shopfloor.backend.olingo.business.processors.generics.utils.projections;

import com.shopfloor.backend.olingo.business.processors.generics.ODataEntityMapper;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import java.util.List;

public class ODataProjectionBuilder<T> {

    private ExpandOption  expandOption;

    private SelectOption selectOption;

    private ODataEntityMapper<T> entityMapper;

    private final int MAX_DEPTH = 1;

    public ODataProjectionBuilder(ODataEntityMapper<T> entityMapper) {
        this.entityMapper = entityMapper;
    }

    public ODataProjectionBuilder<T> addExpand(ExpandOption expandOption) {
        this.expandOption = expandOption;
        return this;
    }

    public ODataProjectionBuilder<T> addSelect(SelectOption selectOption) {

        this.selectOption = selectOption;
        return this;
    }

    public EntityCollection buildEntityCollectionFrom(List<T> DBOs) {
        //TODO: Implement according to the $select and $expand options
        return this.entityMapper.mapEntityCollection(DBOs, MAX_DEPTH);
    }

    public Entity buildEntityFrom(T DBO) {
        //TODO: Implement according to the $select and $expand options
        return this.entityMapper.mapEntity(DBO, MAX_DEPTH);
    }

}

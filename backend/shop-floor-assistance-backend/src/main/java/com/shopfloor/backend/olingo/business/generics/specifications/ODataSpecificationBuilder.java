package com.shopfloor.backend.olingo.business.generics.specifications;


import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.springframework.data.jpa.domain.Specification;

public class ODataSpecificationBuilder<T> {

    private Specification<T> specification;
    private ExpressionTranslator expressionTranslator;
    public ODataSpecificationBuilder() {
        this.specification = Specification.where(null); // No filters applied initially
        this.expressionTranslator = new ExpressionTranslator();
    }

    public ODataSpecificationBuilder<T> addFilter(FilterOption filterOption) {
        if (filterOption != null) {
            Specification<T> filterSpecification = new FilterSpecification<T>().build(filterOption);
            specification = specification.and(filterSpecification);
        }
        return this;
    }

    public ODataSpecificationBuilder<T> addOrderBy(OrderByOption orderByOption) {
        if (orderByOption != null) {
            Specification<T> orderBySpecification = new OrderBySpecification<T>().build(orderByOption);
            specification = specification.and(orderBySpecification);
        }
        return this;
    }

    public ODataSpecificationBuilder<T> addComposeKey(UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {
        Expression expression = this.expressionTranslator.translateExpressionFromEntitySet(uriResourceEntitySet);
        if (expression != null) {
            Specification<T> expressionSpecification = new FilterSpecification<T>().build(expression);
            specification = specification.and(expressionSpecification);
        }
        return this;
    }

    public Specification<T> build() {
        return specification;
    }
}


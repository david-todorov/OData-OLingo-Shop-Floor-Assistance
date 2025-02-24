package com.shopfloor.backend.olingo.business.processors.generics.utils.specifications;



import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ODataSpecificationBuilder<T> {

    private Specification<T> specification;
    public ODataSpecificationBuilder() {
        this.specification = Specification.where(null); // No filters applied initially
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

    public ODataSpecificationBuilder<T> addComposeKey(List<UriParameter> keyParams, UriInfo uriInfo) {
        Expression expression = translateExpressionFromKeys(keyParams, uriInfo);
        if (expression != null) {
            Specification<T> expressionSpecification = new FilterSpecification<T>().build(expression);
            specification = specification.and(expressionSpecification);
        }
        return this;
    }

    public Expression translateExpressionFromKeys(List<UriParameter> keyParams, UriInfo uriInfo) {


        if (keyParams.isEmpty()) {
            throw new IllegalArgumentException("Key parameters must not be empty.");
        }

        Expression finalExpression = null;


        for (UriParameter param : keyParams) {
            String propertyName = param.getName();
            String propertyValue = param.getText();


        }

        return finalExpression;
    }

    public Specification<T> build() {
        return specification;
    }
}


package com.shopfloor.backend.olingo.business.generics.specifications;


import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.springframework.data.jpa.domain.Specification;

/**
 * A builder class for creating JPA Specifications based on OData query options.
 * This class supports adding filters, order by options, search options, and composite keys.
 * Supported Options:
 * - $filter (FilterOption)
 * - $orderby (OrderByOption)
 * - $search (SearchOption)
 * - Composite keys (UriResourceEntitySet)
 * It uses builder pattern to chain multiple options together.
 *
 * @param <T> the type of the entity for which the specification is being built
 * @author David Todorov (https://github.com/david-todorov)
 */
public class ODataSpecificationBuilder<T> {

    /**
     * The JPA Specification used to build the query based on OData options.
     */
    private Specification<T> specification;

    /**
     * The translator used to convert ResourceSet predicates to Expression
     * Which can be used as FilterOption
     */
    private ExpressionTranslator expressionTranslator;

    /**
     * Constructs an ODataSpecificationBuilder instance.
     * Initializes the specification with no filters applied initially and sets up the expression translator.
     */
    public ODataSpecificationBuilder() {
        this.specification = Specification.where(null); // No filters applied initially
        this.expressionTranslator = new ExpressionTranslator();
    }

    /**
     * Adds a filter option to the current specification.
     *
     * @param filterOption the filter option to be added
     * @return the updated ODataSpecificationBuilder instance
     */
    public ODataSpecificationBuilder<T> addFilter(FilterOption filterOption) {
        if (filterOption != null) {
            Specification<T> filterSpecification = new FilterSpecification<T>().build(filterOption);
            specification = specification.and(filterSpecification);
        }
        return this;
    }

    /**
     * Adds an order by option to the current specification.
     *
     * @param orderByOption the order by option to be added
     * @return the updated ODataSpecificationBuilder instance
     */
    public ODataSpecificationBuilder<T> addOrderBy(OrderByOption orderByOption) {
        if (orderByOption != null) {
            Specification<T> orderBySpecification = new OrderBySpecification<T>().build(orderByOption);
            specification = specification.and(orderBySpecification);
        }
        return this;
    }

    /**
     * Adds a search option to the current specification.
     *
     * @param searchOption the search option to be added
     * @return the updated ODataSpecificationBuilder instance
     */
    public ODataSpecificationBuilder<T> addSearchOption(SearchOption searchOption) {
        if (searchOption != null) {
            Specification<T> searchSpecification = new SearchSpecification<T>().build(searchOption);
            searchSpecification = specification.and(searchSpecification);
        }
        return this;
    }

    /**
     * Adds a composite key to the current specification.
     * It translates the composite key into an expression and builds a specification based on it.
     * Can be used for filtering entities based on composite keys or single keys.
     *
     * @param uriResourceEntitySet the URI resource entity set containing the composite key
     * @return the updated ODataSpecificationBuilder instance
     * @throws ODataApplicationException if an error occurs while translating the expression
     */
    public ODataSpecificationBuilder<T> addComposeKey(UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {
        Expression expression = this.expressionTranslator.translateExpressionFromEntitySet(uriResourceEntitySet);
        if (expression != null) {
            Specification<T> expressionSpecification = new FilterSpecification<T>().build(expression);
            specification = specification.and(expressionSpecification);
        }
        return this;
    }

    /**
     * Builds and returns the current JPA Specification.
     *
     * @return the current JPA Specification
     */
    public Specification<T> build() {
        return specification;
    }
}


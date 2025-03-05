package com.shopfloor.backend.olingo.business.generics.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * A specification for adding order by clauses to a JPA query.
 * Represents the $orderby option in OData.
 *
 * @param <T> the type of the entity to be queried
 * @author David Todorov (https://github.com/david-todorov)
 */
public class OrderBySpecification<T> {

    /**
     * Builds a JPA Specification for ordering results based on the provided OrderByOption.
     *
     * @param orderByOption the option containing the order by items
     * @return a JPA Specification with the order by clauses applied, or a no-op Specification if no orders are provided
     */
    public Specification<T> build(OrderByOption orderByOption) {
        if (orderByOption == null || orderByOption.getOrders().isEmpty()) {
            return Specification.where(null); // No sorting needed
        }

        return (root, query, criteriaBuilder) -> {
            // Create a list of orders for the query
            query.orderBy(createOrders(orderByOption, root, criteriaBuilder));
            return null; // No predicate, just sorting
        };
    }

    /**
     * Creates a list of JPA Order objects based on the provided OrderByOption.
     *
     * @param orderByOption the option containing the order by items
     * @param root the root type in the from clause
     * @param criteriaBuilder the criteria builder used to construct criteria queries
     * @return a list of JPA Order objects
     */
    private List<Order> createOrders(OrderByOption orderByOption, Root<?> root, CriteriaBuilder criteriaBuilder) {
        List<Order> orders = new ArrayList<>();
        for (OrderByItem orderByItem : orderByOption.getOrders()) {
            String fieldName = this.camelCaseFieldName(orderByItem);
            boolean ascending = !orderByItem.isDescending();
            Order order = ascending
                    ? criteriaBuilder.asc(root.get(fieldName))
                    : criteriaBuilder.desc(root.get(fieldName));
            orders.add(order);
        }
        return orders;
    }

    /**
     * Converts the field name from the OrderByItem to camel case format.
     *
     * @param orderByItem the order by item containing the field name
     * @return the field name in camel case format
     */
    private String camelCaseFieldName(OrderByItem orderByItem) {
        // Extract the expression as a string (e.g., "[Name]")
        String fieldName = orderByItem.getExpression().toString();

        // Remove the square brackets
        if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
            fieldName = fieldName.substring(1, fieldName.length() - 1); // Remove [ and ]
        }

        // Convert the first letter to lowercase
        if (!fieldName.isEmpty()) {
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        }

        return fieldName;
    }

}

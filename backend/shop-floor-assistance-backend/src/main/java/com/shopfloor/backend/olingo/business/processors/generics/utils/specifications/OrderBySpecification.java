package com.shopfloor.backend.olingo.business.processors.generics.utils.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderBySpecification<T> {
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

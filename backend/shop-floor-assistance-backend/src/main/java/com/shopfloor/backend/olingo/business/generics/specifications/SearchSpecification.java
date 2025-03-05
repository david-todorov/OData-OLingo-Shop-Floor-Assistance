package com.shopfloor.backend.olingo.business.generics.specifications;

import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.search.SearchBinary;
import org.apache.olingo.server.api.uri.queryoption.search.SearchExpression;
import org.apache.olingo.server.api.uri.queryoption.search.SearchTerm;
import org.apache.olingo.server.api.uri.queryoption.search.SearchUnary;
import org.springframework.data.jpa.domain.Specification;


/**
 * A generic class for building JPA Specifications based on OData search options.
 * This class supports adding search terms, unary expressions, and binary expressions.
 * It uses the Specification interface to build the query based on the search options.
 * IMPORTANT: Currently, not supported yet.
 *
 * @param <T> the type of the entity for which the specification is being built
 * @author David Todorov (
 */
public class SearchSpecification<T> {

    public Specification<T> build(SearchOption searchOption){
        SearchExpression searchExpression = searchOption.getSearchExpression();

        return processExpression(searchExpression);
    }

    private Specification<T> processExpression(SearchExpression searchExpression){
        if (searchExpression.isSearchBinary()){
            return handleBinarySearchExpression((SearchBinary) searchExpression);
        }
        else if(searchExpression.isSearchUnary()){
            return handleUnaryExpression((SearchUnary) searchExpression);
        }
        else if(searchExpression.isSearchTerm()){
            return handleTermExpression((SearchTerm) searchExpression);
        }
        throw new IllegalArgumentException("Unsupported search expression type: " + searchExpression.getClass());
    }

    private Specification<T> handleTermExpression(SearchTerm searchTerm) {
        String term = searchTerm.getSearchTerm();
        // Iterate all over the string
        // Make specification for each field

        throw new UnsupportedOperationException("Not @search option implemented yet");
    }



    private Specification<T> handleUnaryExpression(SearchUnary searchExpression) {
        return null;
    }

    private Specification<T> handleBinarySearchExpression(SearchBinary searchBinary) {
        // Implement the handling of SearchBinary expression
        // Example: Handle AND/OR operations or comparisons (e.g., term1 AND term2)
        SearchExpression leftOperand = searchBinary.getLeftOperand();
        SearchExpression rightOperand = searchBinary.getRightOperand();

        Specification<T> leftSpec = processExpression(leftOperand);
        Specification<T> rightSpec = processExpression(rightOperand);

        switch (searchBinary.getOperator()) {
            case AND:
                return Specification.where(leftSpec).and(rightSpec);
            case OR:
                return Specification.where(leftSpec).or(rightSpec);
            default:
                throw new UnsupportedOperationException("Unsupported binary operator: " + searchBinary.getOperator());
        }
    }

}

package com.shopfloor.backend.olingo.business.processors.generics.utils.specifications;

import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class FilterSpecification<T> {

    public Specification<T> build(FilterOption filterOption) {
        Expression expression = filterOption.getExpression();
        return processExpression(expression);
    }

    public Specification<T> build(Expression expression) {
        return processExpression(expression);
    }

    private Specification<T> processExpression(Expression expression) {
        if (expression instanceof Binary) {
            return handleBinaryExpression((Binary) expression);
        } else if (expression instanceof Member) {
            return handleMemberExpression((Member) expression);
        } else if (expression instanceof Literal) {
            return handleLiteralExpression((Literal) expression);
        } else if (expression instanceof Method) {
            return handleMethodExpression((Method) expression);
        }
        throw new IllegalArgumentException("Unsupported expression type: " + expression.getClass());
    }

    private Specification<T> handleBinaryExpression(Binary binary) {
        // Process left and right expressions first (postorder traversal)
        Specification<T> leftSpec = processExpression(binary.getLeftOperand());
        Specification<T> rightSpec = processExpression(binary.getRightOperand());
        BinaryOperatorKind operator = binary.getOperator();

        // Combine left and right into a single predicate based on the operator
        switch (operator) {
            case AND:
                return Specification.where(leftSpec).and(rightSpec);
            case OR:
                return Specification.where(leftSpec).or(rightSpec);
            case EQ:
            case NE:
            case GT:
            case LT:
            case GE:
            case LE:
                return buildSimpleComparison(binary, operator);
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    private Specification<T> buildSimpleComparison(Binary binary, BinaryOperatorKind operator) {
        Member member = (Member) binary.getLeftOperand();
        Literal literal = (Literal) binary.getRightOperand();

        // Extract field name and value
        String fieldName = this.camelCaseFieldName(member);
        Object value = parseLiteralValue(literal);

        // Return a Specification that builds the comparison
        return (root, query, criteriaBuilder) -> {
            switch (operator) {
                case EQ:
                    return criteriaBuilder.equal(root.get(fieldName), value);
                case NE:
                    return criteriaBuilder.notEqual(root.get(fieldName), value);
                case GT:
                    return criteriaBuilder.greaterThan(root.get(fieldName), (Comparable) value);
                case LT:
                    return criteriaBuilder.lessThan(root.get(fieldName), (Comparable) value);
                case GE:
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), (Comparable) value);
                case LE:
                    return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), (Comparable) value);
                default:
                    throw new UnsupportedOperationException("Unsupported comparison operator: " + operator);
            }
        };
    }

    private Specification<T> handleMemberExpression(Member member) {
        // A Member represents a field in the entity
        String fieldName = this.camelCaseFieldName(member);


        // Return a Specification that provides the Path for the field
        return (root, query, criteriaBuilder) -> root.get(fieldName).as(Object.class).isNotNull(); // Dummy predicate for now
    }

    private Specification<T> handleLiteralExpression(Literal literal) {
        // A Literal represents a constant value in the expression
        Object value = parseLiteralValue(literal);

        // Return a dummy Specification (actual usage happens in Binary expressions)
        return (root, query, criteriaBuilder) -> criteriaBuilder.literal(value).isNotNull(); // Dummy predicate for now
    }

    private Specification<T> handleMethodExpression(Method method) {
        String methodName = method.getMethod().name().toLowerCase();

        if (methodName.equals("contains")) {
            return handleContains(method);
        } else if (methodName.equals("startswith")) {
            return handleStartsWith(method);
        } else if (methodName.equals("endswith")) {
            return handleEndsWith(method);
        }

        throw new UnsupportedOperationException("Method expressions like " + methodName + " are not supported yet");
    }

    private Specification<T> handleContains(Method method) {
        Member member = (Member) method.getParameters().get(0);  // Get the field (left operand)
        Literal literal = (Literal) method.getParameters().get(1);  // Get the value (right operand)

        String fieldName = camelCaseFieldName(member);
        String value = (String) parseLiteralValue(literal);

        // Use LIKE in the query for "contains"
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.toLowerCase() + "%"
        );
    }

    private Specification<T> handleStartsWith(Method method) {
        Member member = (Member) method.getParameters().get(0);  // Get the field (left operand)
        Literal literal = (Literal) method.getParameters().get(1);  // Get the value (right operand)

        String fieldName = camelCaseFieldName(member);
        String value = (String) parseLiteralValue(literal);

        // Use LIKE for "startswith"
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                value.toLowerCase() + "%"
        );
    }

    private Specification<T> handleEndsWith(Method method) {
        Member member = (Member) method.getParameters().get(0);  // Get the field (left operand)
        Literal literal = (Literal) method.getParameters().get(1);  // Get the value (right operand)

        String fieldName = camelCaseFieldName(member);
        String value = (String) parseLiteralValue(literal);

        // Use LIKE for "endswith"
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.toLowerCase()
        );
    }

    private Object parseLiteralValue(Literal literal) {
        String literalValue = literal.getText();

        // Remove quotes if it's a string
        if (literalValue.startsWith("'") && literalValue.endsWith("'")) {
            return literalValue.substring(1, literalValue.length() - 1);
        }

        // Try to parse as other types
        try {
            if (literalValue.matches("-?\\d+")) {
                return Integer.parseInt(literalValue);
            } else if (literalValue.matches("-?\\d+\\.\\d+")) {
                return Double.parseDouble(literalValue);
            } else if (literalValue.equalsIgnoreCase("true") || literalValue.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(literalValue);
            } else if (literalValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(literalValue);
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid literal value: " + literalValue, e);
        }

        throw new IllegalArgumentException("Unsupported literal type: " + literalValue);
    }

    private String camelCaseFieldName(Member member) {
        return Character.toLowerCase(member.getResourcePath().getUriResourceParts().get(0).getSegmentValue().charAt(0))
                + member.getResourcePath().getUriResourceParts().get(0).getSegmentValue().substring(1);
    }
}

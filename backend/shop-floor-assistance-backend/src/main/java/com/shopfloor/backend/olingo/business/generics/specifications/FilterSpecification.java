package com.shopfloor.backend.olingo.business.generics.specifications;

import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.expression.*;
import org.apache.olingo.server.core.uri.queryoption.expression.UnaryImpl;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

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
        } else if (expression instanceof Unary) {
            return handleUnaryExpression((Unary) expression);
        }
        throw new IllegalArgumentException("Unsupported expression type: " + expression.getClass());
    }

    private Specification<T> handleUnaryExpression(Unary unary) {
        UnaryOperatorKind operator = unary.getOperator();

        if (operator == UnaryOperatorKind.NOT) {
            Expression expression = unary.getOperand(); // The expression inside the unary operator
            Specification<T> operandSpec = processExpression(expression);
            // Process the operand expression
            return (root, query, criteriaBuilder) -> criteriaBuilder.not(operandSpec.toPredicate(root, query, criteriaBuilder));
        } else if (operator == UnaryOperatorKind.MINUS) {
            // MINUS is handled inside buildSimpleComparison(), so no processing needed here.
            return processExpression(unary.getOperand());
        }

        throw new UnsupportedOperationException("Unsupported unary operator: " + operator);
    }

    private Specification<T> handleMemberExpression(Member member) {
        // A Member represents a field in the entity
        String fieldName = this.camelCaseFieldName(member);


        // Return a Specification that provides the Path for the field
        return (root, query, criteriaBuilder) -> root.get(fieldName).as(Object.class).isNotNull(); // Dummy predicate for now
    }

    private Specification<T> handleLiteralExpression(Literal literal) {
        // A Literal represents a constant value in the expression
        Object value = parseLiteralValue(literal, false);

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
        String value = (String) parseLiteralValue(literal, false);

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
        String value = (String) parseLiteralValue(literal, false);

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
        String value = (String) parseLiteralValue(literal, false);

        // Use LIKE for "endswith"
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + value.toLowerCase()
        );
    }

    private Specification<T> handleBinaryExpression(Binary binary) {
        BinaryOperatorKind operator = binary.getOperator();

        if (operator == BinaryOperatorKind.IN) {
            return handleInOperator(binary);
        }

        Specification<T> leftSpec = processExpression(binary.getLeftOperand());
        Specification<T> rightSpec = processExpression(binary.getRightOperand());

        switch (operator) {
            case AND:
                return Specification.where(leftSpec).and(rightSpec);
            case OR:
                return Specification.where(leftSpec).or(rightSpec);
            default:
                return buildSimpleComparison(binary, operator);
        }
    }

    private Specification<T> handleInOperator(Binary binary) {
        Member member = (Member) binary.getLeftOperand();
        String fieldName = camelCaseFieldName(member);

        List<Object> values = binary.getExpressions().stream()
                .map(expression -> parseLiteralValue((Literal) expression, false))
                .collect(Collectors.toList());

        return (root, query, criteriaBuilder) -> root.get(fieldName).in(values);
    }

    private Specification<T> buildSimpleComparison(Binary binary, BinaryOperatorKind operator) {

        Member member = null;
        Literal literal = null;
        Object literalValue = null;

        // Special case when the binary operation
        // is negated with UnaryOperatorKind.MINUS
        // do not confuse it with not() operator
        // it serves different purpose
        if (isNegatedBinaryOperationMINUS(binary)) {
            member = (Member) ((UnaryImpl) binary.getLeftOperand()).getOperand();
            literal = (Literal) binary.getRightOperand();
            literalValue = parseLiteralValue(literal, true);
        } else {
            // Standard binary operation
            member = (Member) binary.getLeftOperand();
            literal = (Literal) binary.getRightOperand();
            literalValue = parseLiteralValue(literal, false);
        }

        String fieldName = camelCaseFieldName(member);


        Object finalLiteralValue = literalValue;
        return (root, query, criteriaBuilder) -> {
            switch (operator) {
                case EQ:
                    return criteriaBuilder.equal(root.get(fieldName), finalLiteralValue);
                case NE:
                    return criteriaBuilder.notEqual(root.get(fieldName), finalLiteralValue);
                case GT:
                    return criteriaBuilder.greaterThan(root.get(fieldName), (Comparable) finalLiteralValue);
                case LT:
                    return criteriaBuilder.lessThan(root.get(fieldName), (Comparable) finalLiteralValue);
                case GE:
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), (Comparable) finalLiteralValue);
                case LE:
                    return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), (Comparable) finalLiteralValue);
                default:
                    throw new UnsupportedOperationException("Unsupported comparison operator: " + operator);
            }
        };
    }

    private Object parseLiteralValue(Literal literal, boolean negate) {
        String literalStringValue = literal.getText();

        if (literalStringValue.startsWith("'") && literalStringValue.endsWith("'")) {
            return literalStringValue.substring(1, literalStringValue.length() - 1);
        }

        try {
            if (literalStringValue.matches("-?\\d+")) {
                Integer value = Integer.parseInt(literalStringValue);
                return negate ? -value : value;
            } else if (literalStringValue.matches("-?\\d+\\.\\d+")) {
                Double value = Double.parseDouble(literalStringValue);
                return negate ? -value : value;
            } else if (literalStringValue.equalsIgnoreCase("true") || literalStringValue.equalsIgnoreCase("false")) {
                Boolean value = Boolean.parseBoolean(literalStringValue);
                return negate ? !value : value;
            } else if (literalStringValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(literalStringValue);
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid literal value: " + literalStringValue, e);
        }

        throw new IllegalArgumentException("Unsupported literal type: " + literalStringValue);
    }

    private String camelCaseFieldName(Member member) {
        return Character.toLowerCase(member.getResourcePath().getUriResourceParts().get(0).getSegmentValue().charAt(0))
                + member.getResourcePath().getUriResourceParts().get(0).getSegmentValue().substring(1);
    }

    private boolean isNegatedBinaryOperationMINUS(Binary binary) {
        return binary.getLeftOperand() instanceof UnaryImpl;
    }

}

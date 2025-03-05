package com.shopfloor.backend.olingo.business.generics.specifications;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBinary;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.core.uri.UriInfoImpl;
import org.apache.olingo.server.core.uri.UriResourcePrimitivePropertyImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.BinaryImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;

import java.util.List;

/**
 * A class responsible for translating OData expressions from an entity set.
 * @author David Todorov (https://github.com/david-todorov)
 */
public class ExpressionTranslator {

    /**
     * Translates OData expressions from an entity set into a combined expression.
     * Represents the primary key of the entity set as a combined expression.
     * Or composite key if the entity set has multiple key properties.
     *
     * @param uriResourceEntitySet the entity set containing the OData expressions
     * @return the combined OData expression
     * @throws ODataApplicationException if the key parameters are empty or null
     */
    public Expression translateExpressionFromEntitySet(UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException  {

        List<UriParameter> keyParams = uriResourceEntitySet.getKeyPredicates();

        if (keyParams == null || keyParams.isEmpty()) {
            throw new ODataApplicationException("Key parameters must not be empty or null", HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
        }

        Expression finalExpression = null;

        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        for (UriParameter param : keyParams) {
            String propertyName = param.getName();
            String propertyValue = param.getText();

            EdmProperty edmProperty = (EdmProperty) edmEntityType.getProperty(propertyName);
            EdmType edmPropertyType = edmProperty.getType();

            UriInfoImpl uriInfoResource = new UriInfoImpl();
            UriResourcePrimitivePropertyImpl uriResourcePrimitiveProperty = new UriResourcePrimitivePropertyImpl(edmProperty);
            uriInfoResource.addResourcePart(uriResourcePrimitiveProperty);

            MemberImpl left = new MemberImpl(uriInfoResource, null);
            LiteralImpl right =  new LiteralImpl(propertyValue, edmPropertyType);

            Expression currentExpression = new BinaryImpl(left, BinaryOperatorKind.EQ, right, EdmBinary.getInstance());

            if (finalExpression == null) {
                finalExpression = currentExpression;
            } else {
                finalExpression = new BinaryImpl(finalExpression, BinaryOperatorKind.AND, currentExpression, EdmBinary.getInstance());
            }

        }

        return finalExpression;
    }

}

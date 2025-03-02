package com.shopfloor.backend.olingo.presentation;


import com.shopfloor.backend.olingo.business.implementations.equipments.EquipmentCollectionProcessor;
import com.shopfloor.backend.olingo.business.implementations.equipments.EquipmentEntityProcessor;
import com.shopfloor.backend.olingo.business.implementations.equipments.EquipmentPrimitiveProcessor;
import com.shopfloor.backend.olingo.business.implementations.orders.OrderCollectionProcessor;
import com.shopfloor.backend.olingo.business.implementations.orders.OrderEntityProcessor;
import com.shopfloor.backend.olingo.business.implementations.orders.OrderPrimitiveProcessor;
import com.shopfloor.backend.olingo.business.implementations.products.ProductCollectionProcessor;
import com.shopfloor.backend.olingo.business.implementations.products.ProductEntityProcessor;
import com.shopfloor.backend.olingo.business.implementations.products.ProductPrimitiveProcessor;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;

@Configuration
public class ODataServletConfiguration {
    private final ApplicationContext applicationContext;

    private final String BASE_URL = "/odata/*";

    public ODataServletConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> odataServlet() {
        return new ServletRegistrationBean<>(new ODataServlet(applicationContext), BASE_URL);
    }

    static class ODataServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        private final ApplicationContext applicationContext;
        ODataServlet(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try {
                OData odata = OData.newInstance();
                ServiceMetadata edm = odata.createServiceMetadata(
                        applicationContext.getBean(EdmProvider.class),
                        new ArrayList<>()
                );
                ODataHttpHandler handler = odata.createHandler(edm);

                // Extract entity type from request URL
                String requestUri = req.getRequestURI(); // Example: /odata/Products
                String entityName = extractEntityName(requestUri);

                // Dynamically register processors based on entity name
                switch (entityName) {
                    case "Products":
                        handler.register(applicationContext.getBean(ProductEntityProcessor.class));
                        handler.register(applicationContext.getBean(ProductCollectionProcessor.class));
                        handler.register(applicationContext.getBean(ProductPrimitiveProcessor.class));
                        break;
                    case "Orders":
                        handler.register(applicationContext.getBean(OrderEntityProcessor.class));
                        handler.register(applicationContext.getBean(OrderCollectionProcessor.class));
                        handler.register(applicationContext.getBean(OrderPrimitiveProcessor.class));
                        break;
                    case "Equipments":
                        handler.register(applicationContext.getBean(EquipmentEntityProcessor.class));
                        handler.register(applicationContext.getBean(EquipmentCollectionProcessor.class));
                        handler.register(applicationContext.getBean(EquipmentPrimitiveProcessor.class));
                        break;
                    default:
                        throw new RuntimeException("Unknown entity: " + entityName);
                }

                handler.process(req, resp);
            } catch (RuntimeException e) {
                throw new IOException(e);
            }
        }

        private String extractEntityName(String requestUri) {
            // Example requestUri: "/odata/Products" → Extract "Products"
            String[] segments = requestUri.split("/");
            if (segments.length > 2) {
                // The entity name is typically at index 2 after "/odata"
                String entityName = segments[2];
                // Handle the case of entity (1), (2), etc.
                if (entityName.contains("(")) {
                    entityName = entityName.split("\\(")[0];
                }
                return entityName;
            }
            return null;
        }
    }
}

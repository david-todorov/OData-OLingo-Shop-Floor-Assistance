package com.shopfloor.backend.olingo.presentation;

import com.shopfloor.backend.olingo.business.implementations.equipments.EquipmentCollectionProcessor;
import com.shopfloor.backend.olingo.business.implementations.equipments.EquipmentEntityProcessor;
import com.shopfloor.backend.olingo.business.implementations.orders.OrderCollectionProcessor;
import com.shopfloor.backend.olingo.business.implementations.orders.OrderEntityProcessor;
import com.shopfloor.backend.olingo.business.implementations.products.ProductCollectionProcessor;
import com.shopfloor.backend.olingo.business.implementations.products.ProductEntityProcessor;
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

/**
 * Configuration class for the OData servlet.
 * Registers the OData servlet with the Spring application context.
 * The servlet is registered at the base URL "/odata/*".
 * The servlet dynamically registers entity processors based on the requested entity type.
 * In general, every entity type has two processors - one for handling single entities and one for handling collections.
 * The entity processors are registered based on the requested entity type.
 *
 *
 * @Author David Todorov (https://github.com/david-todorov)
 */
@Configuration
public class ODataServletConfiguration {
    private final ApplicationContext applicationContext;
    private static final String BASE_URL = "/odata";

    public ODataServletConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> odataServlet() {
        return new ServletRegistrationBean<>(new ODataServlet(applicationContext), BASE_URL + "/*");
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

                // Allow metadata and service root requests
                String requestUri = req.getRequestURI();
                if (requestUri.equals(BASE_URL + "/$metadata") || requestUri.equals("/odata/")) {
                    handler.process(req, resp);
                    return;
                }

                // Extract entity type from request URL
                String entityName = extractEntityName(requestUri);
                if (entityName == null) {
                    handler.process(req, resp);
                    return;
                }

                // Register processors dynamically
                registerProcessors(handler, entityName);
                handler.process(req, resp);
            } catch (RuntimeException e) {
                throw new IOException(e);
            }
        }

        /**
         * Registers entity and collection processors based on the requested entity type.
         *
         * @param handler the ODataHttpHandler to register the processors with
         * @param entityName the name of the entity type for which processors are to be registered
         * @throws RuntimeException if the entity type is unknown
         */
        private void registerProcessors(ODataHttpHandler handler, String entityName) {
            // Register entity and collection processors based on the requested entity type
            switch (entityName) {
                case "Products":
                    handler.register(applicationContext.getBean(ProductEntityProcessor.class));
                    handler.register(applicationContext.getBean(ProductCollectionProcessor.class));
                    break;
                case "Orders":
                    handler.register(applicationContext.getBean(OrderEntityProcessor.class));
                    handler.register(applicationContext.getBean(OrderCollectionProcessor.class));
                    break;
                case "Equipments":
                    handler.register(applicationContext.getBean(EquipmentEntityProcessor.class));
                    handler.register(applicationContext.getBean(EquipmentCollectionProcessor.class));
                    break;
                default:
                    throw new RuntimeException("Unknown entity: " + entityName);
            }
        }

        private String extractEntityName(String requestUri) {
            String[] segments = requestUri.split("/");
            if (segments.length > 2) {
                String entityName = segments[2];
                if (entityName.contains("(")) {
                    entityName = entityName.split("\\(")[0];
                }
                return entityName;
            }
            return null;
        }
    }
}

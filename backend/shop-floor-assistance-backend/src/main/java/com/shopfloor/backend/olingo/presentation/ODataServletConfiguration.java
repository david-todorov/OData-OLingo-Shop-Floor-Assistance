package com.shopfloor.backend.olingo.presentation;


import com.shopfloor.backend.olingo.business.processors.products.ProductCollectionProcessor;
import com.shopfloor.backend.olingo.business.processors.products.ProductEntityProcessor;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.olingo.commons.api.edmx.EdmxReference;
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

    public ODataServletConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> odataServlet() {
        return new ServletRegistrationBean<>(new ODataServlet(applicationContext), "/odata/*");
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
                ServiceMetadata edm = odata.createServiceMetadata(applicationContext.getBean(EdmProvider.class), new ArrayList<EdmxReference>());
                ODataHttpHandler handler = odata.createHandler(edm);

                // Retrieve Spring-managed beans instead of manual instantiation
                handler.register(applicationContext.getBean(ProductEntityProcessor.class));
                handler.register(applicationContext.getBean(ProductCollectionProcessor.class));

                //TODO: Register OrderCollectionProcessor if we can not make one general
             //  handler.register(applicationContext.getBean(OrderCollectionProcessor.class));
             //  handler.register(applicationContext.getBean(OrderEntityProcessor.class));

                handler.process(req, resp);
            } catch (RuntimeException e) {
                throw new IOException(e);
            }
        }
    }
}

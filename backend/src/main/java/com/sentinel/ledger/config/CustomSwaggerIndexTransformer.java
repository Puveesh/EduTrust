package com.sentinel.ledger.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CustomSwaggerIndexTransformer extends SwaggerIndexPageTransformer {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public CustomSwaggerIndexTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerUiConfigParameters swaggerUiConfigParameters,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerUiConfigParameters, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformer) throws IOException {
        Resource transformedResource = super.transform(request, resource, transformer);
        if (antPathMatcher.match("**/swagger-ui/**/swagger-initializer.js", resource.getURL().toString())) {
            String originalJs = new String(transformedResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String customJs = loadCustomJs();
            String modifiedJs = originalJs + "\n\n" + customJs;
            return new TransformedResource(resource, modifiedJs.getBytes(StandardCharsets.UTF_8));
        }
        return transformedResource;
    }

    private String loadCustomJs() {
        try (InputStream is = getClass().getResourceAsStream("/static/swagger-ui/swagger-login.js")) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}

package com.mock.service;

import com.mock.dto.SwaggerEndpoint;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SwaggerParser {

    /**
     * Parse Swagger/OpenAPI document from URL
     */
    public List<SwaggerEndpoint> parseSwaggerUrl(String swaggerUrl) {
        try {
            log.info("Parsing Swagger from URL: {}", swaggerUrl);

            // Parse using OpenAPI parser (supports both Swagger 2.0 and OpenAPI 3.0)
            OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerUrl);

            if (openAPI == null) {
                log.error("Failed to parse Swagger document from: {}", swaggerUrl);
                throw new RuntimeException("Failed to parse Swagger document");
            }

            return extractEndpoints(openAPI);
        } catch (Exception e) {
            log.error("Error parsing Swagger document", e);
            throw new RuntimeException("Error parsing Swagger document: " + e.getMessage(), e);
        }
    }

    /**
     * Extract endpoints from OpenAPI object
     */
    private List<SwaggerEndpoint> extractEndpoints(OpenAPI openAPI) {
        List<SwaggerEndpoint> endpoints = new ArrayList<>();

        if (openAPI.getPaths() == null) {
            return endpoints;
        }

        openAPI.getPaths().forEach((path, pathItem) -> {
            endpoints.addAll(extractOperations(path, pathItem));
        });

        log.info("Extracted {} endpoints from Swagger document", endpoints.size());
        return endpoints;
    }

    /**
     * Extract operations from a path item
     */
    private List<SwaggerEndpoint> extractOperations(String path, PathItem pathItem) {
        List<SwaggerEndpoint> endpoints = new ArrayList<>();

        // Process each HTTP method
        processOperation(path, "GET", pathItem.getGet(), endpoints);
        processOperation(path, "POST", pathItem.getPost(), endpoints);
        processOperation(path, "PUT", pathItem.getPut(), endpoints);
        processOperation(path, "DELETE", pathItem.getDelete(), endpoints);
        processOperation(path, "PATCH", pathItem.getPatch(), endpoints);
        processOperation(path, "HEAD", pathItem.getHead(), endpoints);
        processOperation(path, "OPTIONS", pathItem.getOptions(), endpoints);

        return endpoints;
    }

    /**
     * Process a single operation
     */
    private void processOperation(String path, String method, Operation operation, List<SwaggerEndpoint> endpoints) {
        if (operation == null) {
            return;
        }

        SwaggerEndpoint endpoint = new SwaggerEndpoint();
        endpoint.setPath(path);
        endpoint.setMethod(method);
        endpoint.setSummary(operation.getSummary());
        endpoint.setDescription(operation.getDescription());
        endpoint.setOperationId(operation.getOperationId());

        // Extract tag
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            endpoint.setTag(operation.getTags().get(0));
        }

        // Extract request body schema
        endpoint.setRequestBodySchema(extractRequestBodySchema(operation.getRequestBody()));

        // Extract response schema (use 200 response)
        endpoint.setResponseSchema(extractResponseSchema(operation.getResponses()));

        endpoints.add(endpoint);
    }

    /**
     * Extract request body schema
     */
    private Map<String, Object> extractRequestBodySchema(RequestBody requestBody) {
        if (requestBody == null || requestBody.getContent() == null) {
            return null;
        }

        Content content = requestBody.getContent();

        // Try to get JSON content
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = content.get("*/*");
        }
        if (mediaType == null && !content.isEmpty()) {
            mediaType = content.values().iterator().next();
        }

        if (mediaType != null && mediaType.getSchema() != null) {
            return schemaToMap(mediaType.getSchema());
        }

        return null;
    }

    /**
     * Extract response schema
     */
    private Map<String, Object> extractResponseSchema(io.swagger.v3.oas.models.responses.ApiResponses responses) {
        if (responses == null) {
            return null;
        }

        // Try to get 200 response
        ApiResponse response = responses.get("200");
        if (response == null) {
            response = responses.get("201");
        }
        if (response == null && !responses.isEmpty()) {
            response = responses.values().iterator().next();
        }

        if (response != null && response.getContent() != null) {
            Content content = response.getContent();

            MediaType mediaType = content.get("application/json");
            if (mediaType == null) {
                mediaType = content.get("*/*");
            }
            if (mediaType == null && !content.isEmpty()) {
                mediaType = content.values().iterator().next();
            }

            if (mediaType != null && mediaType.getSchema() != null) {
                return schemaToMap(mediaType.getSchema());
            }
        }

        return null;
    }

    /**
     * Convert Schema to Map
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, Object> schemaToMap(Schema schema) {
        if (schema == null) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // Type
        if (schema.getType() != null) {
            result.put("type", schema.getType());
        }

        // Format
        if (schema.getFormat() != null) {
            result.put("format", schema.getFormat());
        }

        // Enum
        if (schema.getEnum() != null) {
            result.put("enum", schema.getEnum());
        }

        // Min/Max
        if (schema.getMinimum() != null) {
            result.put("minimum", schema.getMinimum());
        }
        if (schema.getMaximum() != null) {
            result.put("maximum", schema.getMaximum());
        }

        // Properties (for object type)
        if (schema.getProperties() != null) {
            Map<String, Object> properties = new LinkedHashMap<>();
            schema.getProperties().forEach((name, propSchema) -> {
                properties.put((String) name, schemaToMap((Schema) propSchema));
            });
            result.put("properties", properties);
        }

        // Items (for array type)
        if (schema.getItems() != null) {
            result.put("items", schemaToMap(schema.getItems()));
        }

        // Example
        if (schema.getExample() != null) {
            result.put("example", schema.getExample());
        }

        return result;
    }
}

package com.mock.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SwaggerEndpoint {
    private String path;
    private String method;
    private String summary;
    private String description;
    private String operationId;
    private Map<String, Object> requestBodySchema;
    private Map<String, Object> responseSchema;
    private String tag;
}

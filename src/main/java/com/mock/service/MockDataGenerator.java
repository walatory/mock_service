package com.mock.service;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class MockDataGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    /**
     * Generate mock data based on JSON schema
     */
    public Object generateFromSchema(Map<String, Object> schema) {
        if (schema == null || schema.isEmpty()) {
            return Map.of("message", "success", "data", null);
        }

        String type = (String) schema.get("type");
        if (type == null) {
            type = "object"; // Default to object
        }

        return generateByType(type, schema, null);
    }

    /**
     * Generate data by type
     */
    private Object generateByType(String type, Map<String, Object> schema, String fieldName) {
        switch (type.toLowerCase()) {
            case "object":
                return generateObject(schema, fieldName);
            case "array":
                return generateArray(schema, fieldName);
            case "string":
                return generateString(schema, fieldName);
            case "integer":
            case "number":
                return generateNumber(schema, fieldName);
            case "boolean":
                return random.nextBoolean();
            default:
                return null;
        }
    }

    /**
     * Generate object with properties
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> generateObject(Map<String, Object> schema, String parentName) {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        if (properties == null || properties.isEmpty()) {
            // No properties defined, return a simple object
            result.put("id", UUID.randomUUID().toString());
            result.put("message", "success");
            return result;
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String propName = entry.getKey();
            Map<String, Object> propSchema = (Map<String, Object>) entry.getValue();
            String propType = (String) propSchema.getOrDefault("type", "string");

            result.put(propName, generateByType(propType, propSchema, propName));
        }

        return result;
    }

    /**
     * Generate array
     */
    @SuppressWarnings("unchecked")
    private List<Object> generateArray(Map<String, Object> schema, String fieldName) {
        List<Object> result = new ArrayList<>();

        Map<String, Object> items = (Map<String, Object>) schema.get("items");
        if (items == null) {
            return result;
        }

        String itemType = (String) items.getOrDefault("type", "object");
        int count = random.nextInt(3) + 2; // Generate 2-4 items

        for (int i = 0; i < count; i++) {
            result.add(generateByType(itemType, items, fieldName));
        }

        return result;
    }

    /**
     * Generate string based on field name patterns
     */
    @SuppressWarnings("unchecked")
    private String generateString(Map<String, Object> schema, String fieldName) {
        // Check for enum values
        List<String> enumValues = (List<String>) schema.get("enum");
        if (enumValues != null && !enumValues.isEmpty()) {
            return enumValues.get(random.nextInt(enumValues.size()));
        }

        // Check for pattern or format
        String format = (String) schema.get("format");
        if (format != null) {
            switch (format.toLowerCase()) {
                case "date":
                case "date-time":
                    return Instant.now().toString();
                case "email":
                    return faker.internet().emailAddress();
                case "uri":
                case "url":
                    return faker.internet().url();
                case "uuid":
                    return UUID.randomUUID().toString();
                default:
                    break;
            }
        }

        // Generate based on field name
        if (fieldName != null) {
            String lowerName = fieldName.toLowerCase();

            // ID fields
            if (lowerName.equals("id") || lowerName.endsWith("id")) {
                return UUID.randomUUID().toString();
            }

            // Name fields
            if (lowerName.contains("name")) {
                if (lowerName.contains("first") || lowerName.equals("firstname")) {
                    return faker.name().firstName();
                } else if (lowerName.contains("last") || lowerName.equals("lastname")) {
                    return faker.name().lastName();
                } else if (lowerName.contains("user") || lowerName.contains("account")) {
                    return faker.name().username();
                } else if (lowerName.contains("company")) {
                    return faker.company().name();
                } else {
                    return faker.name().fullName();
                }
            }

            // Email
            if (lowerName.contains("email") || lowerName.contains("mail")) {
                return faker.internet().emailAddress();
            }

            // Phone
            if (lowerName.contains("phone") || lowerName.contains("mobile") || lowerName.contains("tel")) {
                return faker.phoneNumber().phoneNumber();
            }

            // Address
            if (lowerName.contains("address")) {
                return faker.address().fullAddress();
            }
            if (lowerName.contains("city")) {
                return faker.address().city();
            }
            if (lowerName.contains("country")) {
                return faker.address().country();
            }
            if (lowerName.contains("street")) {
                return faker.address().streetAddress();
            }
            if (lowerName.contains("zipcode") || lowerName.contains("zip") || lowerName.contains("postal")) {
                return faker.address().zipCode();
            }

            // URL
            if (lowerName.contains("url") || lowerName.contains("link") || lowerName.contains("website")) {
                return faker.internet().url();
            }

            // Description/Content
            if (lowerName.contains("desc") || lowerName.contains("content") || lowerName.contains("comment")) {
                return faker.lorem().sentence();
            }

            // Title
            if (lowerName.contains("title")) {
                return faker.book().title();
            }

            // Status
            if (lowerName.contains("status")) {
                return random.nextBoolean() ? "active" : "inactive";
            }

            // Code
            if (lowerName.contains("code")) {
                return String.format("%06d", random.nextInt(1000000));
            }

            // Date/Time
            if (lowerName.contains("date") || lowerName.contains("time") || lowerName.contains("at")) {
                return Instant.now().toString();
            }
        }

        // Default: random sentence
        return faker.lorem().sentence(5);
    }

    /**
     * Generate number
     */
    private Number generateNumber(Map<String, Object> schema, String fieldName) {
        Object minimum = schema.get("minimum");
        Object maximum = schema.get("maximum");

        int min = minimum != null ? ((Number) minimum).intValue() : 0;
        int max = maximum != null ? ((Number) maximum).intValue() : 1000;

        // Generate based on field name
        if (fieldName != null) {
            String lowerName = fieldName.toLowerCase();

            if (lowerName.contains("age")) {
                return random.nextInt(60) + 18; // 18-77
            }
            if (lowerName.contains("price") || lowerName.contains("amount") || lowerName.contains("cost")) {
                return Math.round(random.nextDouble() * 10000 * 100.0) / 100.0; // 0-10000 with 2 decimals
            }
            if (lowerName.contains("count") || lowerName.contains("quantity") || lowerName.contains("total")) {
                return random.nextInt(100) + 1;
            }
            if (lowerName.contains("percent") || lowerName.contains("rate")) {
                return random.nextInt(100);
            }
        }

        // Default range
        if (max > min) {
            return random.nextInt(max - min) + min;
        }

        return random.nextInt(1000);
    }
}

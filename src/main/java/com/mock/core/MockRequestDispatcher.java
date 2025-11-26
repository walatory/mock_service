package com.mock.core;

import com.mock.model.MockRule;
import com.mock.model.MockServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Enumeration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@Slf4j
@Component
public class MockRequestDispatcher {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ExpressionParser parser = new SpelExpressionParser();
    private final RestTemplate restTemplate = new RestTemplate();

    public void dispatch(HttpServletRequest req, HttpServletResponse resp, MockServiceConfig config)
            throws IOException {
        String uri = req.getRequestURI();
        String method = req.getMethod();
        String body = StreamUtils.copyToString(req.getInputStream(), StandardCharsets.UTF_8);

        log.info("Received request: {} {} on service {}", method, uri, config.getServiceName());

        MockRule matchedRule = findMatchingRule(req, body, config);

        if (matchedRule != null) {
            handleResponse(resp, matchedRule, req, body);
        } else {
            resp.setStatus(404);
            resp.getWriter().write("No matching mock rule found for " + method + " " + uri);
        }
    }

    private MockRule findMatchingRule(HttpServletRequest req, String body, MockServiceConfig config) {
        for (MockRule rule : config.getRules()) {
            // Match Method
            if (StringUtils.hasText(rule.getMethod()) && !rule.getMethod().equalsIgnoreCase(req.getMethod())) {
                continue;
            }

            // Match URL
            if (StringUtils.hasText(rule.getUrlPattern())
                    && !pathMatcher.match(rule.getUrlPattern(), req.getRequestURI())) {
                continue;
            }

            // Match Body
            if (StringUtils.hasText(rule.getBodyMatch())) {
                if (body == null || !Pattern.compile(rule.getBodyMatch()).matcher(body).find()) {
                    continue;
                }
            }

            return rule;
        }
        return null;
    }

    private void handleResponse(HttpServletResponse resp, MockRule rule, HttpServletRequest req, String requestBody)
            throws IOException {
        if ("forward".equalsIgnoreCase(rule.getMode()) && StringUtils.hasText(rule.getForwardUrl())) {
            handleForwarding(resp, rule, req, requestBody);
            return;
        }

        if (rule.getDelayMs() > 0) {
            try {
                Thread.sleep(rule.getDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        resp.setStatus(rule.getResponseStatus());

        if (rule.getResponseHeaders() != null) {
            for (Map.Entry<String, String> entry : rule.getResponseHeaders().entrySet()) {
                resp.addHeader(entry.getKey(), entry.getValue());
            }
        }

        if (rule.getResponseBody() != null) {
            String responseBody = rule.getResponseBody();
            try {
                // Simple SpEL evaluation context
                EvaluationContext context = new StandardEvaluationContext();
                context.setVariable("req", req);
                context.setVariable("body", requestBody);
                // TODO: Add more useful variables like path variables, query params

                // If the response body contains SpEL expressions like #{...}
                if (responseBody.contains("#{")) {
                    // This is a very naive template replacement.
                    // For a real template engine, we might want something more robust.
                    // But for "expressions", we can just evaluate the whole string if it's an
                    // expression,
                    // or replace parts. Let's assume the user puts the whole body as a template or
                    // specific parts.
                    // For now, let's just support direct SpEL if the string starts with #{ and ends
                    // with }
                    // Or better, use a TemplateParserContext if we want mixed content.

                    // Let's try to parse it as a template string if it has #{
                    org.springframework.expression.common.TemplateParserContext templateContext = new org.springframework.expression.common.TemplateParserContext();
                    responseBody = parser.parseExpression(responseBody, templateContext).getValue(context,
                            String.class);
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate response template", e);
                // Fallback to raw body
            }
            resp.getWriter().write(responseBody);
        }
    }

    private void handleForwarding(HttpServletResponse resp, MockRule rule, HttpServletRequest req, String body) throws IOException {
        try {
            String targetUrl = rule.getForwardUrl();
            // Basic support for appending path info if needed could go here, 
            // but for now we trust the rule's forwardUrl.

            HttpMethod method = HttpMethod.valueOf(req.getMethod());
            
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Skip headers that are hop-by-hop or might cause issues
                if (!headerName.equalsIgnoreCase("host") 
                        && !headerName.equalsIgnoreCase("content-length")
                        && !headerName.equalsIgnoreCase("transfer-encoding")) {
                    headers.add(headerName, req.getHeader(headerName));
                }
            }
            
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(targetUrl, method, entity, byte[].class);
            
            resp.setStatus(response.getStatusCode().value());
            
            response.getHeaders().forEach((k, v) -> {
                // Skip some response headers if needed
                if (!k.equalsIgnoreCase("transfer-encoding")) {
                    v.forEach(val -> resp.addHeader(k, val));
                }
            });
            
            if (response.getBody() != null) {
                resp.getOutputStream().write(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Forwarding failed", e);
            resp.setStatus(500);
            resp.getWriter().write("Forwarding failed: " + e.getMessage());
        }
    }
}

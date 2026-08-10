package gasi.one.core.api.resource.hook;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Request metadata available to resource hooks.
 *
 * <p>
 * This keeps URL-scoped data, such as nested resource path variables, outside
 * request DTOs while still making it available to generated and custom hooks.
 * </p>
 *
 * @since 1.0.0
 */
public final class ResourceRequestContext {

    private static final ResourceRequestContext EMPTY = builder().build();

    private final Map<String, String> pathVariables;
    private final String method;
    private final String requestUri;
    private final String requestUrl;
    private final String queryString;
    private final String contextPath;
    private final String servletPath;

    private ResourceRequestContext(Builder builder) {
        this.pathVariables = Collections.unmodifiableMap(new LinkedHashMap<>(builder.pathVariables));
        this.method = builder.method;
        this.requestUri = builder.requestUri;
        this.requestUrl = builder.requestUrl;
        this.queryString = builder.queryString;
        this.contextPath = builder.contextPath;
        this.servletPath = builder.servletPath;
    }

    public static ResourceRequestContext empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public String pathVariable(String name) {
        return pathVariables.get(name);
    }

    public boolean hasPathVariable(String name) {
        return pathVariables.containsKey(name);
    }

    public String getMethod() {
        return method;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    public static final class Builder {

        private final Map<String, String> pathVariables = new LinkedHashMap<>();
        private String method;
        private String requestUri;
        private String requestUrl;
        private String queryString;
        private String contextPath;
        private String servletPath;

        private Builder() {
        }

        public Builder pathVariables(Map<?, ?> pathVariables) {
            if (pathVariables == null || pathVariables.isEmpty()) {
                return this;
            }
            pathVariables.forEach((key, value) -> {
                if (key != null && value != null) {
                    this.pathVariables.put(String.valueOf(key), String.valueOf(value));
                }
            });
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder requestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }

        public Builder requestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Builder servletPath(String servletPath) {
            this.servletPath = servletPath;
            return this;
        }

        public ResourceRequestContext build() {
            return new ResourceRequestContext(this);
        }
    }
}

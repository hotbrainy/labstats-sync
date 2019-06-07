package edu.sydneyuni.myuni.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class ApiGatewayProxyResponse {
    private int statusCode;
    private Map<String, String> headers;
    private String body;

    public ApiGatewayProxyResponse() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    private ApiGatewayProxyResponse(int statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public ApiGatewayProxyResponseBuilder builder() {
        return new ApiGatewayProxyResponseBuilder()
                .withStatusCode(this.getStatusCode())
                .withHeaders(this.getHeaders())
                .withBody(this.getBody());
    }

    public static class ApiGatewayProxyResponseBuilder {
        private int statusCode = 0;
        private Map<String, String> headers = new HashMap<>();
        private String body = "";

        public ApiGatewayProxyResponseBuilder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public ApiGatewayProxyResponseBuilder withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public ApiGatewayProxyResponseBuilder withBody(String body) {
            this.body = body;
            return this;
        }

        public ApiGatewayProxyResponse build() {
            return new ApiGatewayProxyResponse(statusCode, headers, body);
        }
    }

    @Override
    public String toString() {
        return "ApiGatewayProxyResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", body='" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiGatewayProxyResponse)) return false;

        ApiGatewayProxyResponse response = (ApiGatewayProxyResponse) o;

        if (statusCode != response.statusCode) return false;
        if (!Objects.equals(headers, response.headers)) return false;
        return Objects.equals(body, response.body);

    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }
}

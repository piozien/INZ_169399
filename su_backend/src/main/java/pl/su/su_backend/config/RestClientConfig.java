//https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html 24.11.2025 9:20
package pl.su.su_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("graphRestClient")
    public RestClient graphRestClient(@Value("${MS_GRAPH_BASE_URL_01}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("restClient")
    public RestClient restClient() {
        return RestClient.builder()
                .build();
    }
}
package com.portfolio.recruitment.currencyaccount.config;

import com.portfolio.recruitment.currencyaccount.app.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigTest {

    @Test
    void testWebClientBuilderBean() {
        WebClientConfig config = new WebClientConfig();
        WebClient.Builder builder = config.webClientBuilder();

        assertThat(builder).isNotNull();
    }
}
package com.celi.apia.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

@RestController
public class ControllerA {

    @Value("${app.token-endpoint}")
    private String tokenEndpoint;

    @Value("${app.client-id}")
    private String clientId;

    @Value("${app.client-secret}")
    private String clientSecret;

    @Value("${app.token-exchange-client-id}")
    private String tokenExchangeClientId;

    @Bean
    public WebClient rest() {
        return WebClient.builder()
                .filter(new TokenExchangeFilterFunction(
                        tokenEndpoint, clientId, clientSecret, tokenExchangeClientId))
                .build();
    }

    @GetMapping("/api-a/hello")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> hello() {

        // just returns current token from context
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(Jwt::getTokenValue)
                .map(t -> "hello from API-A, my token: " + t);
    }

    @GetMapping("/api-a/hello-all")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> helloWebClient() throws InterruptedException, ExecutionException {

        return this.rest().get()
                .uri("http://localhost:8084/api-b/hello")
                .retrieve()
                .bodyToMono(String.class)
                .map(b -> "hello from API-A - calling my budy >>>> " + b + " <<<<");
    }
}

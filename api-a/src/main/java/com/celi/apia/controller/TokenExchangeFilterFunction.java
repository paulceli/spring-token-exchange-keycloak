package com.celi.apia.controller;

import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * inspired by
 * https://github.com/spring-projects/spring-security/blob/5.7.1/oauth2/oauth2-resource-server/src/main/java/org/springframework/security/oauth2/server/resource/web/reactive/function/client/ServerBearerExchangeFilterFunction.java
 */

public final class TokenExchangeFilterFunction implements ExchangeFilterFunction {

    private String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private String tokenExchangeClientId;

    public TokenExchangeFilterFunction(String tokenEndpoint, String clientId, String clientSecret,
            String tokenExchangeClientId) {
        super();
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenExchangeClientId = tokenExchangeClientId;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // @formatter:off
		return oauth2Token()
                .flatMap(t -> tokenExchange(t))
                .map((token) -> bearer(request, token.getAccess_token()))
				.defaultIfEmpty(request)
				.flatMap(next::exchange);
		// @formatter:on
    }

    private Mono<MyToken> tokenExchange(AbstractOAuth2Token token) {
        System.out.println("we in exchange token, with current token : " + token.getTokenValue());

        WebClient webClient = WebClient.create(tokenEndpoint);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        formData.add("subject_token", token.getTokenValue());
        formData.add("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
        formData.add("audience", tokenExchangeClientId);

        return webClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(
                        formData))
                .retrieve()
                .bodyToMono(MyToken.class);
    }

    private Mono<AbstractOAuth2Token> oauth2Token() {
        // @formatter:off
        return currentAuthentication()
				.filter((authentication) -> authentication.getCredentials() instanceof AbstractOAuth2Token)
				.map(Authentication::getCredentials)
				.cast(AbstractOAuth2Token.class);
		// @formatter:on
    }

    private Mono<Authentication> currentAuthentication() {
        // @formatter:off
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication);
		// @formatter:on
    }

    private ClientRequest bearer(ClientRequest request, String token) {
        System.out.println("we in bearer, with new token : " + token);
        // @formatter:off
		return ClientRequest.from(request)
				.headers((headers) -> headers.setBearerAuth(token))
				.build();
		// @formatter:on
    }

}
package com.celi.apia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2ResourceServerSpec;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {

        @Value("${app.client-id}")
        private String audience;

        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private String issuerUri;

        @Bean
        public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
                http
                                .authorizeExchange(exchanges -> exchanges
                                                // .pathMatchers("/api-a/hello/**").hasAuthority("SCOPE_something")
                                                .anyExchange().authenticated())
                                .oauth2ResourceServer(OAuth2ResourceServerSpec::jwt);
                return http.build();
        }

        @Bean
        ReactiveJwtDecoder jwtDecoder() {
                /*
                 * https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-
                 * server/jwt.html#oauth2resourceserver-jwt-validation-custom
                 * https://github.com/auth0-samples/auth0-spring-security5-api-sample/tree/
                 * master/01-Authorization-MVC/src/main/java/com/auth0/example/security
                 * 
                 * By default, Spring Security does not validate the "aud" (nor "azp") claim of
                 * the token,
                 * to ensure that this token is
                 * indeed intended for our app. Adding our own validator is easy to do:
                 */

                NimbusReactiveJwtDecoder jwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders
                                .fromIssuerLocation(issuerUri);

                OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
                OAuth2TokenValidator<Jwt> azpValidator = new AzpValidator(audience);
                OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
                OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer,
                                audienceValidator,
                                azpValidator);

                jwtDecoder.setJwtValidator(withAudience);

                return jwtDecoder;
        }
}
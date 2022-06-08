package com.celi.apib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.client-id}")
    private String audience;

    @Value("${app.azp}")
    private String azp;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() // disbale session
                                                                                                  // cookies
                .authorizeHttpRequests(authorize -> authorize
                        // .mvcMatchers("/api-b/**").hasAuthority("SCOPE_something")
                        .anyRequest().authenticated())
                .oauth2ResourceServer().jwt();
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
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

        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> azpValidator = new AzpValidator(azp);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAzp = new DelegatingOAuth2TokenValidator<>(withIssuer,
                audienceValidator, azpValidator);

        jwtDecoder.setJwtValidator(withAzp);

        return jwtDecoder;
    }
}
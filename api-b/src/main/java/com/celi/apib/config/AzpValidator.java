package com.celi.apib.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Validates that the JWT token contains the intended AZP in its claims.
 */
class AzpValidator implements OAuth2TokenValidator<Jwt> {
    private final String azp;

    AzpValidator(String azp) {
        this.azp = azp;
    }

    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        OAuth2Error error = new OAuth2Error("invalid_token", "The required azp is missing", null);

        if (jwt.getClaimAsStringList("azp").contains(azp)) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(error);
    }
}
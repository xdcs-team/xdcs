package pl.edu.agh.xdcs.security.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Kamil Jarosz
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class TokenGrant {
    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("token_type")
    private final String tokenType;

    @JsonProperty("expires_in")
    private final int expiresIn;

    @JsonProperty("refresh_token")
    private final String refreshToken;

    @JsonProperty("scope")
    private final String scope;
}

package pl.edu.agh.xdcs.security.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Kamil Jarosz
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CodeGrant {
    private final String code;
}

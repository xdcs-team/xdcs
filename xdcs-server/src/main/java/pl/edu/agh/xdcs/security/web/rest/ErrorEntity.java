package pl.edu.agh.xdcs.security.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Kamil Jarosz
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ErrorEntity {
    private final String error;
}

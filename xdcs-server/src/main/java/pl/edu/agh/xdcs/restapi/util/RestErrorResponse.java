package pl.edu.agh.xdcs.restapi.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Kamil Jarosz
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestErrorResponse {
    private String error;
}

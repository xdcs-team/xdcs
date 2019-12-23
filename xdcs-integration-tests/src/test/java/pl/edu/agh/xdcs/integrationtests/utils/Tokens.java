package pl.edu.agh.xdcs.integrationtests.utils;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class Tokens {
    private String access;
    private String refresh;
}

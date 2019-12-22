package pl.edu.agh.xdcs.integrationtests.utils;

import io.restassured.filter.Filter;

/**
 * @author Kamil Jarosz
 */
public class AuthHelper {
    public static Filter FILTER = (req, resp, filterContext) ->
            filterContext.next(req, resp);
}

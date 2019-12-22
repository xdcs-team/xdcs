package pl.edu.agh.xdcs.integrationtests.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.Assertions;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import pl.edu.agh.xdcs.integrationtests.utils.AuthHelper;
import pl.edu.agh.xdcs.integrationtests.utils.Tokens;

/**
 * @author Kamil Jarosz
 */
@FullOGNL
@RunWith(ConcordionRunner.class)
public class AuthFixture {
    public void unauthorizedWithoutToken() {
        RestAssured.given()
                .when()
                .get("rest/healthcheck")
                .then()
                .statusCode(401);
    }

    public void authNoParameters() {
        RestAssured.when()
                .get("auth/auth")
                .then()
                .statusCode(400);
    }

    private RequestSpecification givenAuthParams(String redirectUri) {
        RequestSpecification spec = RestAssured.given()
                .queryParam("response_type", "code")
                .queryParam("client_id", "web");
        return redirectUri == null ? spec :
                spec.queryParam("redirect_uri", redirectUri);
    }

    public void authPage() {
        givenAuthParams("/")
                .when()
                .get("auth/auth")
                .then()
                .statusCode(200)
                .contentType(ContentType.HTML);
    }

    private RequestSpecification givenAuthenticationData(String username, String password, String redirectUri) {
        return givenAuthParams(redirectUri)
                .formParam("username", username)
                .formParam("password", password);
    }

    public void wrongCredentials() {
        givenAuthenticationData("someNonExistentUser", "password", "/")
                .when()
                .post("auth/auth")
                .then()
                .statusCode(400);
    }

    public void adminCredentialsRedirect() {
        String location = givenAuthenticationData("admin", "admin", "/")
                .when()
                .post("auth/auth")
                .then()
                .statusCode(302)
                .extract()
                .header("Location");
        Assertions.assertThat(location)
                .matches("/\\?code=(.*)");
    }

    public String getAuthCode(String username, String password) {
        return givenAuthenticationData(username, password, null)
                .when()
                .post("auth/auth")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .get("code");
    }

    public Tokens requestTokensFromGrant(String authCode) {
        JsonPath jsonPath = RestAssured.given()
                .formParam("code", authCode)
                .formParam("grant_type", "authorization_code")
                .when()
                .post("auth/token")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath();
        return Tokens.builder()
                .access(jsonPath.get("access_token"))
                .refresh(jsonPath.get("refresh_token"))
                .build();
    }

    public Tokens refreshAccessToken(Tokens tokens) {
        JsonPath jsonPath = RestAssured.given()
                .formParam("refresh_token", tokens.getRefresh())
                .formParam("grant_type", "refresh_token")
                .when()
                .post("auth/token")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath();
        return Tokens.builder()
                .access(jsonPath.get("access_token"))
                .refresh(jsonPath.get("refresh_token"))
                .build();
    }

    public void validateTokens(Tokens tokens) {
        RestAssured.given()
                .header("Authorization", "Bearer " + tokens.getAccess())
                .when()
                .get("rest/healthcheck")
                .then()
                .statusCode(204);
    }

    public Tokens fullAuth(String username, String password) {
        String authCode = getAuthCode(username, password);
        return requestTokensFromGrant(authCode);
    }

    public void useTokens(Tokens tokens) {
        AuthHelper.FILTER = (req, resp, filterContext) -> {
            req = req.replaceHeader("Authorization", "Bearer " + tokens.getAccess());
            return filterContext.next(req, resp);
        };
    }
}

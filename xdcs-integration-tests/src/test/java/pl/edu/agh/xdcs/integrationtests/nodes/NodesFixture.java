package pl.edu.agh.xdcs.integrationtests.nodes;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import pl.edu.agh.xdcs.integrationtests.utils.AuthHelper;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * @author Kamil Jarosz
 */
@FullOGNL
@RunWith(ConcordionRunner.class)
public class NodesFixture {
    private ValidatableResponse forNodesList() {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get("rest/nodes")
                .then();
    }

    private ValidatableResponse forNode(String nodeId) {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get("rest/nodes/{nodeId}", nodeId)
                .then();
    }

    private ValidatableResponse forNodeDetails(String nodeId) {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get("rest/nodes/{nodeId}/details", nodeId)
                .then();
    }

    public void nodeListEmpty() {
        forNodesList()
                .statusCode(200)
                .body("items", is(empty()));
    }

    public void notFoundNode() {
        forNode("nonExistentNode")
                .statusCode(404);
        forNodeDetails("nonExistentNode")
                .statusCode(404);
    }
}

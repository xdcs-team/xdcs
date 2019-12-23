package pl.edu.agh.xdcs.integrationtests.taskdefinitions;

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
public class TaskDefinitionsFixture {
    private ValidatableResponse forDefinitionList() {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get("rest/task-definitions")
                .then();
    }

    private ValidatableResponse forDefinition(String taskDefinitionId) {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get("rest/task-definitions/{taskDefinitionId}", taskDefinitionId)
                .then();
    }

    private ValidatableResponse forDefinitionWorkspace(String taskDefinitionId) {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get("rest/task-definitions/{taskDefinitionId}/workspace", taskDefinitionId)
                .then();
    }

    public void definitionsEmpty() {
        forDefinitionList()
                .statusCode(200)
                .body("items", is(empty()));
    }

    public void notFoundDefinition() {
        forDefinition("nonExistentNode")
                .statusCode(404);
        forDefinitionWorkspace("nonExistentNode")
                .statusCode(404);
    }
}

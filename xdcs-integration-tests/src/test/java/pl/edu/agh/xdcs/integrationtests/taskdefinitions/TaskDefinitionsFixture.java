package pl.edu.agh.xdcs.integrationtests.taskdefinitions;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.concordion.api.FullOGNL;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import pl.edu.agh.xdcs.integrationtests.utils.AuthHelper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * @author Kamil Jarosz
 */
@FullOGNL
@RunWith(ConcordionRunner.class)
public class TaskDefinitionsFixture {
    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    private static final String PATH_DEFINITIONS = "rest/task-definitions";
    private static final String PATH_DEFINITION = "rest/task-definitions/{id}";
    private static final String PATH_DEPLOYMENTS = "rest/deployments";
    private static final String PATH_WORKSPACE_FILE = "rest/task-definitions/{id}/workspace";
    private static final String PATH_WORKSPACE_FILE_CONTENT = "rest/task-definitions/{id}/workspace/content";
    private static final String PATH_WORKSPACE_FILE_MOVE = "rest/task-definitions/{id}/workspace/actions/move";

    private static final String FILE_CONTENT_DOCKERFILE = "FROM pyton:3.7\nCMD python test_python.py\n";
    private static final String FILE_CONTENT_PYTHON = "print(\"Hello World\")\n";

    private ValidatableResponse forDefinitionList() {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get(PATH_DEFINITIONS)
                .then();
    }

    private ValidatableResponse forDefinition(String taskDefinitionId) {
        return RestAssured.given()
                .filter(AuthHelper.FILTER)
                .when()
                .get(PATH_DEFINITION, taskDefinitionId)
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

        RestAssured.given()
                .filter(AuthHelper.FILTER).when()
                .get(PATH_WORKSPACE_FILE, "nonExistentNode")
                .then()
                .statusCode(404);
    }

    public String createTaskDefinition(String name) {
        String locationPattern = "(.*)/rest/task-definitions/(?<id>.+)";
        String location = RestAssured.given()
                .filter(AuthHelper.FILTER)
                .contentType(ContentType.JSON)
                .body(nodeFactory.objectNode()
                        .put("name", name))
                .when()
                .post("rest/task-definitions")
                .then()
                .statusCode(201)
                .extract()
                .header("Location");

        assertThat(location)
                .isNotNull()
                .matches(locationPattern);

        return location.replaceAll(locationPattern, "${id}");
    }

    public ObjectNode getDefinition(String defId) {
        return forDefinition(defId)
                .statusCode(200)
                .extract()
                .body()
                .as(ObjectNode.class);
    }

    public void assertEmptyWorkspace(String defId) {
        RestAssured.given()
                .filter(AuthHelper.FILTER).when()
                .get(PATH_WORKSPACE_FILE, defId)
                .then()
                .statusCode(200)
                .body("type", is("directory"))
                .body("permissions", is("rwxr-xr-x"))
                .body("children", is(empty()));

        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("path", "/")
                .when()
                .get(PATH_WORKSPACE_FILE, defId)
                .then()
                .statusCode(200)
                .body("type", is("directory"))
                .body("permissions", is("rwxr-xr-x"))
                .body("children", is(empty()));
    }

    public void createDirectory(String defId, String path) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("path", path)
                .contentType(ContentType.JSON)
                .body(nodeFactory.objectNode()
                        .put("type", "directory"))
                .when()
                .put(PATH_WORKSPACE_FILE, defId)
                .then()
                .statusCode(200)
                .body("type", is("directory"))
                .body("permissions", is("rwxr-xr-x"))
                .body("children", is(empty()));
    }

    public void createFile(String defId, String path) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("path", path)
                .contentType(ContentType.JSON)
                .body(nodeFactory.objectNode()
                        .put("type", "regular"))
                .when()
                .put(PATH_WORKSPACE_FILE, defId)
                .then()
                .statusCode(200)
                .body("type", is("regular"))
                .body("permissions", is("rw-r--r--"));
    }

    public void cannotMoveFile(String defId, String from, String to, int expectedStatus) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("from", from)
                .queryParam("to", to)
                .when()
                .post(PATH_WORKSPACE_FILE_MOVE, defId)
                .then()
                .statusCode(expectedStatus);
    }

    public void moveFile(String defId, String from, String to) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("from", from)
                .queryParam("to", to)
                .when()
                .post(PATH_WORKSPACE_FILE_MOVE, defId)
                .then()
                .statusCode(204);
    }

    public void cannotDeleteFile(String defId, String path, int expectedStatus) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("path", path)
                .when()
                .delete(PATH_WORKSPACE_FILE, defId)
                .then()
                .statusCode(expectedStatus);
    }

    public void deleteFile(String defId, String path) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("path", path)
                .when()
                .delete(PATH_WORKSPACE_FILE, defId)
                .then()
                .statusCode(204);
    }

    public String getFileContent(String defId, String path) {
        return new String(RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("path", path)
                .when()
                .get(PATH_WORKSPACE_FILE_CONTENT, defId)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asByteArray(), StandardCharsets.UTF_8);
    }

    public void setFileContent(String defId, String path, String content) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .queryParam("path", path)
                .contentType(ContentType.fromContentType("application/octet-stream"))
                .body(content.getBytes(StandardCharsets.UTF_8))
                .when()
                .put(PATH_WORKSPACE_FILE_CONTENT, defId)
                .then()
                .statusCode(204);
    }

    public void setDockerFileContent(String defId, String path) {
        setFileContent(defId, path, FILE_CONTENT_DOCKERFILE);
    }

    public void setPythonFileContent(String defId, String path) {
        setFileContent(defId, path, FILE_CONTENT_PYTHON);
    }

    public void checkDockerFileContent(String defId, String path) {
        assertThat(getFileContent(defId, path))
                .isEqualTo(FILE_CONTENT_DOCKERFILE);
    }

    public void assertDeploymentFailed(String defId) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .contentType(ContentType.JSON)
                .body(nodeFactory.objectNode()
                        .put("from", defId)
                        .put("description", "Test deployment"))
                .when()
                .post(PATH_DEPLOYMENTS)
                .then()
                .statusCode(400);
    }

    public String deploy(String defId) {
        String locationPattern = "(.*)/rest/deployments/(?<id>.+)";
        String location = RestAssured.given()
                .filter(AuthHelper.FILTER)
                .contentType(ContentType.JSON)
                .body(nodeFactory.objectNode()
                        .put("from", defId)
                        .put("description", "Test deployment"))
                .when()
                .post(PATH_DEPLOYMENTS)
                .then()
                .statusCode(201)
                .extract()
                .header("Location");

        assertThat(location)
                .isNotNull()
                .matches(locationPattern);

        return location.replaceAll(locationPattern, "${id}");
    }

    public void setDockerConfiguration(String taskDefinitionId) {
        RestAssured.given()
                .filter(AuthHelper.FILTER)
                .contentType(ContentType.JSON)
                .body(nodeFactory.objectNode()
                        .set("config", nodeFactory.objectNode()
                                .put("type", "docker")))
                .when()
                .put(PATH_DEFINITION, taskDefinitionId)
                .then()
                .statusCode(200);
    }
}

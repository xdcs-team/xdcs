package pl.edu.agh.xdcs.integrationtests;

import io.restassured.RestAssured;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

/**
 * @author Kamil Jarosz
 */
@RunWith(ConcordionRunner.class)
public class IndexFixture {
    private static final String SERVICE_SERVER = "xdcs-server";
    private static final Logger SERVER_LOGGER = LoggerFactory.getLogger(SERVICE_SERVER);

    private static DockerComposeContainer environment =
            new DockerComposeContainer(new File("../docker-compose.debug.yml"))
                    .withBuild(true)
                    .withServices(SERVICE_SERVER)
                    .withLogConsumer(SERVICE_SERVER, new Slf4jLogConsumer(SERVER_LOGGER))
                    .withExposedService(SERVICE_SERVER, 8080, Wait.forHttp("/xdcs/rest/healthcheck")
                            .forStatusCode(200)
                            .forStatusCode(204)
                            .forStatusCode(401));

    @Before
    public void setUp() {
        environment.start();
        RestAssured.port = environment.getServicePort(SERVICE_SERVER, 8080);
        RestAssured.basePath = "/xdcs";
        RestAssured.baseURI = "http://" + environment.getServiceHost(SERVICE_SERVER, 8080);
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @After
    public void tearDown() {
        environment.stop();
    }
}

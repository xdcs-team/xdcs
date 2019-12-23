package pl.edu.agh.xdcs.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.xdcs.RestApplication;
import pl.edu.agh.xdcs.restapi.TaskDefinitionsApi;
import pl.edu.agh.xdcs.security.web.AuthApplication;

import javax.enterprise.inject.Instance;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Kamil Jarosz
 */
@ExtendWith(MockitoExtension.class)
class UriResolverTest {
    @Mock
    private Instance applications;

    private UriResolver resolver;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        resolver = new UriResolver();
        resolver.applications = applications;

        when(applications.iterator())
                .thenReturn(Arrays.asList(
                        new RestApplication(),
                        new AuthApplication()).iterator());
    }

    @Test
    void resolve() {
        assertThat(resolver.of(TaskDefinitionsApi::getTaskDefinition, "x"))
                .isEqualTo("/xdcs/rest/task-definitions/x");
    }
}

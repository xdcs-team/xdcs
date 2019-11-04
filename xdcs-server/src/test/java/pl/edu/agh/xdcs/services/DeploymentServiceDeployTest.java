package pl.edu.agh.xdcs.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.xdcs.db.dao.DeploymentDescriptorDao;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.ObjectRepositoryMock;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.or.types.Tree;
import pl.edu.agh.xdcs.test.utils.FileSetup;
import pl.edu.agh.xdcs.workspace.Workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Kamil Jarosz
 */
@ExtendWith(MockitoExtension.class)
class DeploymentServiceDeployTest {
    @Spy
    private ObjectRepository objectRepository = new ObjectRepositoryMock();

    @Mock
    private TaskDefinitionService definitionService;

    @Mock
    private DeploymentDescriptorDao deploymentDescriptorDao;

    @InjectMocks
    private DeploymentService deploymentService = new DeploymentService();

    private Path workspacePath;

    @BeforeEach
    void setUp() throws IOException {
        workspacePath = FileSetup.setUpDirectory(this);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSetup.tearDownDirectory(workspacePath);
    }

    @Test
    void testDeploy() throws IOException {
        // set up workspace

        Path file1 = workspacePath.resolve("file1");
        Path file2 = workspacePath.resolve("dir").resolve("file2");

        Files.createDirectories(file1.getParent());
        Files.write(file1, "test1".getBytes());
        Files.createDirectories(file2.getParent());
        Files.write(file2, "test2".getBytes());

        // set up definition

        TaskDefinitionEntity taskDefinition = TaskDefinitionEntity.builder()
                .name("test definition")
                .build();

        when(definitionService.getWorkspace(taskDefinition))
                .thenReturn(Workspace.forPath(workspacePath));

        String id = deploymentService.deploy(taskDefinition);

        Deployment deployment = objectRepository.cat(id, Deployment.class);

        assertThat(deployment.getDefinitionId())
                .isEqualTo(taskDefinition.getId());

        Tree root = objectRepository.cat(deployment.getRoot(), Tree.class);

        assertThat(root.getEntries())
                .hasSize(2)
                .anyMatch(entry -> entry.getName().equals("file1"))
                .anyMatch(entry -> entry.getName().equals("dir"));

        String dirId = root.getEntries()
                .stream()
                .filter(e -> e.getName().equals("dir"))
                .findAny()
                .get()
                .getObjectId();
        Tree dir = objectRepository.cat(dirId, Tree.class);
        assertThat(dir.getEntries())
                .hasSize(1)
                .allMatch(entry -> entry.getName().equals("file2"));
    }
}

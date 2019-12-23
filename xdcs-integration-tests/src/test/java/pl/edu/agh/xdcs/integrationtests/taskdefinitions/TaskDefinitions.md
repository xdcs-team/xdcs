# Task definitions tests

The task definition list should be [empty](- "definitionsEmpty()").
The server [returns 404](- "notFoundDefinition()")
if asked for an unknown task definition.

A task definition [is added](- "#defId=createTaskDefinition('Test')").
Its ID is [ ](- "c:echo=#defId").

The task definition
[should be available](- "#definition=getDefinition(#defId)")
when asked for.

Its name should be [Test](- "c:assert-equals=#definition.get('name').textValue()").
Its ID should be
[equal to the received ID](- "c:assert-true=#defId.equals(#definition.get('id').textValue())").

## Workspace tests

The workspace should be
[empty](- "assertEmptyWorkspace(#defId)").

A directory named `dir` is
[created](- "createDirectory(#defId, '/dir')").

A file inside the directory named `file1` is
[created](- "createFile(#defId, '/dir/file1')").

Another directory named `dir2` is
[created](- "createDirectory(#defId, '/dir2')").

The file `/dir/file1`
[cannot be moved](- "cannotMoveFile(#defId, '/dir/file1', '/dir2', 400)")
to `/dir2`, as it exists.

The file `/dir/file1` is then
[moved](- "moveFile(#defId, '/dir/file1', '/dir2/file2')")
to `/dir2/file2`.

The file `/dir2/file2`
[cannot be moved](- "cannotMoveFile(#defId, '/dir2/file2', '/dir3/file3', 404)")
to `/dir3/file3`, as `/dir3` does not exist.

The root directory
[cannot be deleted](- "cannotDeleteFile(#defId, '/', 400)").

A non existent file
[cannot be deleted](- "cannotDeleteFile(#defId, 'nonExistentFile', 404)").

The file `/dir2/file2`
[is deleted](- "deleteFile(#defId, '/dir2/file2')").

Create the following files for deployment:
[`/Dockerfile`](- "createFile(#defId, '/Dockerfile')"),
[`/python_test.py`](- "createFile(#defId, '/python_test.py')").

[Set content of `Dockerfile`.](- "setDockerFileContent(#defId, 'Dockerfile')")
Its content should be [the same as set before](- "checkDockerFileContent(#defId, 'Dockerfile')").

[Set content of the Python script.](- "setPythonFileContent(#defId, 'python_test.py')").

## Deployment test

The deployment should [fail](- "assertDeploymentFailed(#defId)"),
because its config is not set.

[Set](- "setDockerConfiguration(#defId)")
the task definition configuration.

The deployment should [succeed](- "#deploymentId=deploy(#defId)").

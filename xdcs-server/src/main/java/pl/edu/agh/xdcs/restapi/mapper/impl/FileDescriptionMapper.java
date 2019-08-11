package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.restapi.mapper.SimpleMapper;
import pl.edu.agh.xdcs.restapi.model.FileDto;
import pl.edu.agh.xdcs.util.FsUtils;
import pl.edu.agh.xdcs.workspace.FileDescription;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class FileDescriptionMapper implements SimpleMapper<FileDescription, FileDto> {
    @Inject
    private FileTypeMapper typeMapper;

    @Override
    public FileDescription toModelEntity(FileDto rest) {
        return FileDescription.builder()
                .type(typeMapper.toModelEntity(rest.getType()))
                .children(rest.getChildren())
                .build();
    }

    @Override
    public FileDto toRestEntity(FileDescription model) {
        FileDto dto = new FileDto();
        dto.setType(typeMapper.toRestEntity(model.getType()));
        dto.setPermissions(FsUtils.permissionsToString(model.getPermissions()));
        dto.setChildren(model.getChildren());
        return dto;
    }
}

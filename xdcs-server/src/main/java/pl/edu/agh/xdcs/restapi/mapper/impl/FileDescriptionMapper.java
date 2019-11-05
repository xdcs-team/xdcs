package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.restapi.mapper.SimpleMapper;
import pl.edu.agh.xdcs.restapi.model.FileDto;
import pl.edu.agh.xdcs.util.FsUtils;
import pl.edu.agh.xdcs.workspace.FileDescription;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class FileDescriptionMapper implements SimpleMapper<FileDescription, FileDto> {
    @Inject
    private FileTypeMapper typeMapper;

    @Inject
    private FileEntryMapper entryMapper;

    @Override
    public FileDescription toModelEntity(FileDto rest) {
        return FileDescription.builder()
                .type(typeMapper.toModelEntity(rest.getType()))
                .children(rest.getChildren().stream()
                        .map(entryMapper::toModelEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public FileDto toRestEntity(FileDescription model) {
        FileDto dto = new FileDto();
        dto.setType(typeMapper.toRestEntity(model.getType()));
        dto.setPermissions(FsUtils.permissionsToString(model.getPermissions()));
        List<FileDescription.Entry> children = model.getChildren();
        if (children != null) {
            dto.setChildren(children.stream()
                    .sorted(FileDescription.Entry.DIRECTORIES_FIRST
                            .thenComparing(FileDescription.Entry::getName))
                    .map(entryMapper::toRestEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}

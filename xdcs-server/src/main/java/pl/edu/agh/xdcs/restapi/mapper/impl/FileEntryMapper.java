package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.restapi.mapper.SimpleMapper;
import pl.edu.agh.xdcs.restapi.model.FileEntryDto;
import pl.edu.agh.xdcs.util.FsUtils;
import pl.edu.agh.xdcs.workspace.FileDescription;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class FileEntryMapper implements SimpleMapper<FileDescription.Entry, FileEntryDto> {
    @Inject
    private FileTypeMapper typeMapper;

    @Override
    public FileDescription.Entry toModelEntity(FileEntryDto rest) {
        return FileDescription.Entry.builder()
                .type(typeMapper.toModelEntity(rest.getType()))
                .name(rest.getName())
                .build();
    }

    @Override
    public FileEntryDto toRestEntity(FileDescription.Entry model) {
        FileEntryDto dto = new FileEntryDto();
        dto.setType(typeMapper.toRestEntity(model.getType()));
        dto.setPermissions(FsUtils.permissionsToString(model.getPermissions()));
        dto.setName(model.getName());
        return dto;
    }
}

package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.mapper.SimpleMapper;
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
    public FileEntryDto toApiEntity(FileDescription.Entry model) {
        FileEntryDto dto = new FileEntryDto();
        dto.setType(typeMapper.toApiEntity(model.getType()));
        dto.setPermissions(FsUtils.permissionsToString(model.getPermissions()));
        dto.setName(model.getName());
        return dto;
    }
}

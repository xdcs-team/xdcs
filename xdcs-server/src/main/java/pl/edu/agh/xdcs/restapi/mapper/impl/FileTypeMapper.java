package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.restapi.mapper.SimpleMapper;
import pl.edu.agh.xdcs.restapi.mapper.UnsatisfiedMappingException;
import pl.edu.agh.xdcs.restapi.model.FileDto;
import pl.edu.agh.xdcs.workspace.FileDescription;

/**
 * @author Kamil Jarosz
 */
public class FileTypeMapper implements SimpleMapper<FileDescription.FileType, FileDto.TypeEnum> {
    @Override
    public FileDescription.FileType toModelEntity(FileDto.TypeEnum rest) {
        switch (rest) {
            case REGULAR:
                return FileDescription.FileType.REGULAR;
            case DIRECTORY:
                return FileDescription.FileType.DIRECTORY;
            case LINK:
                return FileDescription.FileType.SYMLINK;
            default:
                throw new UnsatisfiedMappingException();
        }
    }

    @Override
    public FileDto.TypeEnum toRestEntity(FileDescription.FileType model) {
        switch (model) {
            case REGULAR:
                return FileDto.TypeEnum.REGULAR;
            case DIRECTORY:
                return FileDto.TypeEnum.DIRECTORY;
            case SYMLINK:
                return FileDto.TypeEnum.LINK;
            default:
                throw new UnsatisfiedMappingException();
        }
    }
}

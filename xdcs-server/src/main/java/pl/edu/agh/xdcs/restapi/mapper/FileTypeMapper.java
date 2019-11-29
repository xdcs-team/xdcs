package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.mapper.SimpleMapper;
import pl.edu.agh.xdcs.mapper.UnsatisfiedMappingException;
import pl.edu.agh.xdcs.restapi.model.FileType;
import pl.edu.agh.xdcs.workspace.FileDescription;

/**
 * @author Kamil Jarosz
 */
public class FileTypeMapper implements SimpleMapper<FileDescription.FileType, FileType> {
    @Override
    public FileDescription.FileType toModelEntity(FileType rest) {
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
    public FileType toApiEntity(FileDescription.FileType model) {
        switch (model) {
            case REGULAR:
                return FileType.REGULAR;
            case DIRECTORY:
                return FileType.DIRECTORY;
            case SYMLINK:
                return FileType.LINK;
            default:
                throw new UnsatisfiedMappingException();
        }
    }
}

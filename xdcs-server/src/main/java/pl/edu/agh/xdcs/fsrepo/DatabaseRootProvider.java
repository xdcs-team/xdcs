package pl.edu.agh.xdcs.fsrepo;

import pl.edu.agh.xdcs.db.dao.ObjectRefDao;
import pl.edu.agh.xdcs.or.RootProvider;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class DatabaseRootProvider implements RootProvider {
    @Inject
    private ObjectRefDao objectRefDao;

    @Override
    public void provideRoots(RootVisitor visitor) {
        objectRefDao.selectAll()
                .forEach(objectRef -> visitor.visit(
                        objectRef.getReferencedObjectId(),
                        objectRef.getReferencedObjectType()));
    }
}

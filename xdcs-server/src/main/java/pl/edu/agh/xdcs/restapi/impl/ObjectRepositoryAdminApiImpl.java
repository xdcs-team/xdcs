package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.fsrepo.DatabaseRootProvider;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.restapi.ObjectRepositoryAdminApi;
import pl.edu.agh.xdcs.restapi.util.RestUtils;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryAdminApiImpl implements ObjectRepositoryAdminApi {
    @Inject
    private ObjectRepository or;

    @Inject
    private DatabaseRootProvider databaseRootProvider;

    @Override
    public Set<String> getOrphans() {
        try {
            return or.runHousekeeping(databaseRootProvider).get();
        } catch (InterruptedException | ExecutionException e) {
            throw RestUtils.throwServerError(e);
        }
    }

    @Override
    public void runHousekeeping() {
        try {
            Set<String> toRemove = or.runHousekeeping(databaseRootProvider).get();
            toRemove.forEach(id -> or.delete(id));
        } catch (InterruptedException | ExecutionException e) {
            throw RestUtils.throwServerError(e);
        }
    }
}

package pl.edu.agh.xdcs.db.dao;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.ResourceEntity;
import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.db.entity.RuntimeTaskEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class ResourceDao extends EntityDaoBase<ResourceEntity> {
    @Override
    protected Class<ResourceEntity> getEntityClass() {
        return ResourceEntity.class;
    }

    @SuppressWarnings("unchecked")
    public List<ResourceEntity> getByAgent(AgentEntity owner) {
        return entityManager.createQuery("select r from Resource r " +
                "where r.owner = :owner")
                .setParameter("owner", owner)
                .getResultList();
    }

    public List<BoundResource> lockResources(QueuedTaskEntity queuedTask, RuntimeTaskEntity runtimeTask)
            throws ResourceLockFailedException {
        List<BoundResource> resources = ((List<?>) entityManager.createQuery(
                "select r, rp from Resource r " +
                        "    right join ResourcePattern rp " +
                        "        on r.owner.name like rp.agentNameLike " +
                        "        and r.resourceKey like rp.resourceKeyLike " +
                        "        and r.lockedBy is null " +
                        "where rp.requester = :requester")
                .setParameter("requester", queuedTask)
                .getResultList())
                .stream()
                .map(Object[].class::cast)
                .map(this::mapResult)
                .collect(Collectors.toList());

        if (resources.stream()
                .map(BoundResource::getResource)
                .anyMatch(Objects::isNull)) {
            // some resources are not available
            throw new ResourceLockFailedException(resources.stream()
                    .filter(bound -> bound.getResource() == null)
                    .collect(Collectors.toList()));
        }

        resources.forEach(r -> r.getResource().setLockedBy(runtimeTask));
        return resources;
    }

    public void unlockResources(String taskId, AgentEntity owner) {
        entityManager.createQuery("update Resource r " +
                "set r.lockedBy = null " +
                "where r.lockedBy.id = :id " +
                "and r.owner = :owner")
                .setParameter("id", taskId)
                .setParameter("owner", owner)
                .executeUpdate();
    }

    public void removeResources(AgentEntity agentEntity) {
        entityManager.createQuery("delete Resource r " +
                "where r.owner.id = :id")
                .setParameter("id", agentEntity.getId())
                .executeUpdate();
    }

    public boolean hasAnyLocks(String taskId) {
        return (long) entityManager.createQuery("select count(*) " +
                "from Resource r " +
                "where r.lockedBy.id = :id")
                .setParameter("id", taskId)
                .getSingleResult() != 0L;
    }

    private BoundResource mapResult(Object[] o) {
        return BoundResource.builder()
                .resource((ResourceEntity) o[0])
                .resourcePattern((ResourcePatternEntity) o[1])
                .build();
    }

    @Getter
    @Builder
    public static class BoundResource {
        private ResourceEntity resource;
        private ResourcePatternEntity resourcePattern;

        @Override
        public String toString() {
            if (resource == null) {
                return resourcePattern.toString();
            } else {
                return resource.toString();
            }
        }
    }

    public static class ResourceLockFailedException extends Exception {
        private List<BoundResource> failedResources;

        public ResourceLockFailedException(List<BoundResource> failedResources) {
            super("Could not lock resources: " + toString(failedResources));
            this.failedResources = failedResources;
        }

        private static String toString(List<BoundResource> failedResources) {
            return failedResources.stream()
                    .map(BoundResource::toString)
                    .collect(Collectors.joining("; "));
        }

        public List<BoundResource> getFailedResources() {
            return failedResources;
        }
    }
}

package io.fourcast.gae.model.transformer;

import com.google.api.server.spi.config.Transformer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import io.fourcast.gae.model.project.Project;
import io.fourcast.gae.model.user.User;
import io.fourcast.gae.service.dto.ProjectDTO;

/**
 * Created by nielsbuekers on 13/08/15.
 */
public class ProjectTransformer extends GenericTransformer implements Transformer<Project, ProjectDTO> {

    @Override
    public ProjectDTO transformTo(Project e) {
        ProjectDTO dto = new ProjectDTO();

        dto.setId(e.getId());
        dto.setParentId(e.getParentId());
        dto.setCreationDate(e.getCreationDate());
        dto.setLastModified(e.getLastModified());
        dto.setActive(e.getActive());
        dto.setSubProjectIds(e.getSubProjectIds());

        return dto;
    }

    @Override
    public Project transformFrom(ProjectDTO dto) {
        Project project = new Project();

        if (dto.getId() != null) {
            project.setId(dto.getId());
        }

        if (dto.getOwner() != null && dto.getOwner().getId() != null && dto.getOwner().getId().length() > 0) {
            Key<User> ownerKey = userKey(dto.getOwner().getId());
            project.setOwner(Ref.create(ownerKey));
        }

        project.setLastModified(dto.getLastModified());
        project.setActive(dto.getActive());
        project.setSubProjectIds(dto.getSubProjectIds());
        project.setParentId(dto.getParentId() == null? 0L:dto.getParentId());


        return project;
    }
}

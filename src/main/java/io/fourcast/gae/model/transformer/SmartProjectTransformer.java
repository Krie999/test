package io.fourcast.gae.model.transformer;

import com.google.api.server.spi.config.Transformer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import io.fourcast.gae.model.smart.SmartProject;
import io.fourcast.gae.model.user.DSUser;
import io.fourcast.gae.service.dto.smart.SmartProjectDTO;

/**
 * Created by nielsbuekers on 13/08/15.
 */
public class SmartProjectTransformer extends GenericTransformer implements Transformer<SmartProject, SmartProjectDTO> {

    @Override
    public SmartProjectDTO transformTo(SmartProject e) {
        SmartProjectDTO dto = new SmartProjectDTO();

        dto.setId(e.getId());
        dto.setParentId(e.getParentId());
        dto.setCreationDate(e.getCreationDate());
        dto.setLastModified(e.getLastModified());

        dto.setSubProjectsIds(e.getSubProjectIds());

        return dto;
    }

    @Override
    public SmartProject transformFrom(SmartProjectDTO dto) {
        SmartProject project = new SmartProject();

        if (dto.getId() != null) {
            project.setId(dto.getId());
        }

        if (dto.getOwner() != null && dto.getOwner().getId() != null && dto.getOwner().getId().length() > 0) {
            Key<DSUser> ownerKey = userKey(dto.getOwner().getId());
            project.setOwner(Ref.create(ownerKey));
        }

        project.setLastModified(dto.getLastModified());
        project.setSubProjectIds(dto.getSubProjectsIds());
        project.setParentId(dto.getParentId() == null? 0L:dto.getParentId());


        return project;
    }
}

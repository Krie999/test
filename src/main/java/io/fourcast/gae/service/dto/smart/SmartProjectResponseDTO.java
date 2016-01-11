package io.fourcast.gae.service.dto.smart;

import io.fourcast.gae.model.smart.SmartProject;
import io.fourcast.gae.service.dto.ProjectResponseDTO;

import java.util.List;

public class SmartProjectResponseDTO extends ProjectResponseDTO<SmartProject> {

    private List<SmartProject> projects;

    public List<SmartProject> getProjects() {
        return projects;
    }

    public void setProjects(List<SmartProject> projects) {
        this.projects = projects;
    }

}

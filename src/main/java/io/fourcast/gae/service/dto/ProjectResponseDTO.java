package io.fourcast.gae.service.dto;

import java.util.List;

/**
 * Created by nielsbuekers on 06/08/15.
 */
public class ProjectResponseDTO<T>{

    private String cursor;

    private List<T> projects;

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public List<T> getProjects() {
        return projects;
    }

    public void setProjects(List<T> projects) {
        this.projects = projects;
    }
}

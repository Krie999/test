package io.fourcast.gae.service.dto.lists;

import io.fourcast.gae.model.lmd.LmdProject;

import java.util.List;

/**
 * Created by nielsbuekers on 02/11/15.
 */
public class LmdProjectListDTO {
    private List<LmdProject> items;

    public LmdProjectListDTO(List<LmdProject> projects) {
        this.items = projects;
    }

    public LmdProjectListDTO() {

    }

    public List<LmdProject> getItems() {
        return items;
    }

    public void setItems(List<LmdProject> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LmdProjectListDTO)) return false;

        LmdProjectListDTO that = (LmdProjectListDTO) o;

        return !(items != null ? !items.equals(that.items) : that.items != null);

    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }
}

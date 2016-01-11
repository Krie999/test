package io.fourcast.gae.service.dto.lists;

import java.util.List;

/**
 * Created by nielsbuekers on 02/11/15.
 */
public class ProjectTreeListDTO {
    private List<ProjectTreeEntry> items;

    public ProjectTreeListDTO(List<ProjectTreeEntry> entries) {
        this.items = entries;
    }

    public List<ProjectTreeEntry> getItems() {
        return items;
    }

    public void setItems(List<ProjectTreeEntry> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectTreeListDTO)) return false;

        ProjectTreeListDTO that = (ProjectTreeListDTO) o;

        return !(items != null ? !items.equals(that.items) : that.items != null);

    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }
}

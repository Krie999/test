package io.fourcast.gae.service.dto.lists;

import io.fourcast.gae.model.lmd.LmdTask;

import java.util.List;

/**
 * Created by nielsbuekers on 02/11/15.
 */
public class LmdTaskListDTO {

    private List<LmdTask> items;

    public LmdTaskListDTO(List<LmdTask> tasks) {
        this.items = tasks;
    }

    public List<LmdTask> getItems() {
        return items;
    }

    public void setItems(List<LmdTask> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LmdTaskListDTO)) return false;

        LmdTaskListDTO that = (LmdTaskListDTO) o;

        return !(items != null ? !items.equals(that.items) : that.items != null);

    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }
}

package io.fourcast.gae.service.dto;

import io.fourcast.gae.service.dto.notification.NotificationDTO;

import java.util.List;

/**
 * Created by kevinnorek on 4/11/15.
 */
public class updateNotificationsDTO {

    private int number;
    private List<NotificationDTO> items ;


    public updateNotificationsDTO() {
        this(0);
    }

    public updateNotificationsDTO(int number) {
        this.number = number;
    }

    public List<NotificationDTO> getItems() {
        return items;
    }

    public void setItems(List<NotificationDTO> items) {
        this.items = items;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        updateNotificationsDTO that = (updateNotificationsDTO) o;

        if (number != that.number) return false;
        return !(items != null ? !items.equals(that.items) : that.items != null);

    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }
}

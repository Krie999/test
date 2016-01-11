package io.fourcast.gae.service.dto.smart;

import java.util.List;

import io.fourcast.gae.model.smart.SmartProject;


public class SmartProjectListDTO {
    List<SmartProject> list;
    public SmartProjectListDTO() {
    }

    public SmartProjectListDTO(List<SmartProject> list) {
        this.list = list;
    }

    public List<SmartProject> getList() {
        return list;
    }

    public void setList(List<SmartProject> list) {
        this.list = list;
    }

}
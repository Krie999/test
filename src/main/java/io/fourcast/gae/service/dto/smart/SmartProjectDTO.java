package io.fourcast.gae.service.dto.smart;

import io.fourcast.gae.model.smart.SmartProject;
import io.fourcast.gae.model.user.DSUser;
import io.fourcast.gae.service.dto.ProjectDTO;

import java.util.Date;
import java.util.List;

/**
 * Created by nielsbuekers on 13/08/15.
 */
public class SmartProjectDTO extends ProjectDTO {


    private List<Long> subProjectsIds;

    private String ownerDisplayName;

    private DSUser sponsor;

    private SmartProject.PROJECT_STATUS status;

    private Date creationDate = new Date();
    private Date projectStartDate;
    private Date goLiveDate;

    private String projectCode;

    public List<Long> getSubProjectsIds() {
        return subProjectsIds;
    }

    public void setSubProjectsIds(List<Long> subProjectsIds) {
        this.subProjectsIds = subProjectsIds;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }

    public DSUser getSponsor() {
        return sponsor;
    }

    public void setSponsor(DSUser sponsor) {
        this.sponsor = sponsor;
    }

    public SmartProject.PROJECT_STATUS getStatus() {
        return status;
    }

    public void setStatus(SmartProject.PROJECT_STATUS status) {
        this.status = status;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getProjectStartDate() {
        return projectStartDate;
    }

    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    public Date getGoLiveDate() {
        return goLiveDate;
    }

    public void setGoLiveDate(Date goLiveDate) {
        this.goLiveDate = goLiveDate;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SmartProjectDTO that = (SmartProjectDTO) o;

        if (subProjectsIds != null ? !subProjectsIds.equals(that.subProjectsIds) : that.subProjectsIds != null)
            return false;
        if (ownerDisplayName != null ? !ownerDisplayName.equals(that.ownerDisplayName) : that.ownerDisplayName != null)
            return false;
        if (sponsor != null ? !sponsor.equals(that.sponsor) : that.sponsor != null) return false;
        if (status != that.status) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (projectStartDate != null ? !projectStartDate.equals(that.projectStartDate) : that.projectStartDate != null)
            return false;
        if (goLiveDate != null ? !goLiveDate.equals(that.goLiveDate) : that.goLiveDate != null) return false;
        return !(projectCode != null ? !projectCode.equals(that.projectCode) : that.projectCode != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (subProjectsIds != null ? subProjectsIds.hashCode() : 0);
        result = 31 * result + (ownerDisplayName != null ? ownerDisplayName.hashCode() : 0);
        result = 31 * result + (sponsor != null ? sponsor.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (projectStartDate != null ? projectStartDate.hashCode() : 0);
        result = 31 * result + (goLiveDate != null ? goLiveDate.hashCode() : 0);
        result = 31 * result + (projectCode != null ? projectCode.hashCode() : 0);
        return result;
    }
}

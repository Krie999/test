package io.fourcast.gae.service.dto;

import io.fourcast.gae.model.user.User;

import java.util.Date;
import java.util.List;

/**
 * Created by nielsbuekers on 13/08/15.
 */
public class ProjectDTO {

    public Boolean active;
    Long id;
    private Long parentId;
    private String parentProjectCode;
    private Date lastModified;
    private Date creationDate;
    private String projectName;
    private Long departmentId;
    private User owner;
    private List<Long> subProjectIds;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

    public String getParentProjectCode() {
        return parentProjectCode;
    }

    public void setParentProjectCode(String parentProjectCode) {
        this.parentProjectCode = parentProjectCode;
    }

    public List<Long> getSubProjectIds() {
        return subProjectIds;
    }

    public void setSubProjectIds(List<Long> subProjectIds) {
        this.subProjectIds = subProjectIds;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectDTO that = (ProjectDTO) o;

        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        if (parentProjectCode != null ? !parentProjectCode.equals(that.parentProjectCode) : that.parentProjectCode != null)
            return false;
        if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) return false;
        if (departmentId != null ? !departmentId.equals(that.departmentId) : that.departmentId != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        return !(subProjectIds != null ? !subProjectIds.equals(that.subProjectIds) : that.subProjectIds != null);

    }

    @Override
    public int hashCode() {
        int result = active != null ? active.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (parentProjectCode != null ? parentProjectCode.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (departmentId != null ? departmentId.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (subProjectIds != null ? subProjectIds.hashCode() : 0);
        return result;
    }
}

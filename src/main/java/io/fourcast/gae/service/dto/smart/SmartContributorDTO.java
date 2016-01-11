package io.fourcast.gae.service.dto.smart;

import io.fourcast.gae.utils.Globals;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.api.server.spi.config.AnnotationBoolean;


import java.util.Date;

/**
 * Created by nielsbuekers on 14/08/15.
 */

//afblijven -niels
public class SmartContributorDTO {


    public SmartContributorDTO() {
    }

    public enum SMARTCONTRIBUTOR_ROLE {

        OWNER("Owner"),
        CAMPAIGN_MANAGER("Campaign Manager"),
        LMD_ANALYST("LMD Analyst"),
        SPONSOR("Sponsor");

        private String role;
        SMARTCONTRIBUTOR_ROLE(String role) {
            this.role = role;
        }

        @Override
        public String toString() {
            return this.role;
        }
    }

    private Date startDate;
    private Date endDate;
    private String workLoad;
    private String tasks;
    private String Spent;
    private String ETC;
    private String displayName;
    private Long projectId;
    private Globals.APP appType;
    private String projectName;
    private String projectCode;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private SMARTCONTRIBUTOR_ROLE role;




    public String getRoleDescription(){
        return role.toString();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getWorkLoad() {
        return workLoad;
    }

    public void setWorkLoad(String workLoad) {
        this.workLoad = workLoad;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public String getSpent() {
        return Spent;
    }

    public void setSpent(String spent) {
        Spent = spent;
    }

    public String getETC() {
        return ETC;
    }

    public void setETC(String ETC) {
        this.ETC = ETC;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Globals.APP getAppType() {
        return appType;
    }

    public void setAppType(Globals.APP appType) {
        this.appType = appType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public SmartContributorDTO setProjectCode(String projectCode) {
        this.projectCode = projectCode;
        return this;
    }

    public SMARTCONTRIBUTOR_ROLE getRole() {
        return role;
    }

    public void setRole(SMARTCONTRIBUTOR_ROLE role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartContributorDTO that = (SmartContributorDTO) o;

        if (startDate != null ? !startDate.equals(that.startDate) : that.startDate != null) return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (workLoad != null ? !workLoad.equals(that.workLoad) : that.workLoad != null) return false;
        if (tasks != null ? !tasks.equals(that.tasks) : that.tasks != null) return false;
        if (Spent != null ? !Spent.equals(that.Spent) : that.Spent != null) return false;
        if (ETC != null ? !ETC.equals(that.ETC) : that.ETC != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
        if (appType != that.appType) return false;
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) return false;
        if (projectCode != null ? !projectCode.equals(that.projectCode) : that.projectCode != null) return false;
        return role == that.role;

    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (workLoad != null ? workLoad.hashCode() : 0);
        result = 31 * result + (tasks != null ? tasks.hashCode() : 0);
        result = 31 * result + (Spent != null ? Spent.hashCode() : 0);
        result = 31 * result + (ETC != null ? ETC.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (appType != null ? appType.hashCode() : 0);
        result = 31 * result + (projectName != null ? projectName.hashCode() : 0);
        result = 31 * result + (projectCode != null ? projectCode.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
    }
}

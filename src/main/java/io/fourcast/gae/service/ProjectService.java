package io.fourcast.gae.service;

import io.fourcast.gae.model.common.DSEntry;
import io.fourcast.gae.utils.Globals;

import java.util.List;

/**
 * Created by nielsbuekers on 13/10/15.
 */
public abstract class ProjectService extends AbstractService {
    private static ProjectManager projectManager = new ProjectManager();


    protected ProjectTreeEntry findSimpleProjectTree(Globals.APP appType,Long projectId){
        ProjectTreeEntry tree = findProjectTree(appType,projectId);
        simplifyProjectTreeEntry(tree);
        return tree;
    }

    private void simplifyProjectTreeEntry(ProjectTreeEntry expandedProjectTree) {
        expandedProjectTree.setId(expandedProjectTree.getProject().getId());
        expandedProjectTree.setDescription(expandedProjectTree.getProject().getProjectName());
        expandedProjectTree.setProject(null);
        for(ProjectTreeEntry expandedEntry: expandedProjectTree.getProjects()){
            simplifyProjectTreeEntry(expandedEntry);
        }
    }

    //TODO CACHE RESULTS AND UPDATE ON ANY SAVE OF THE TREE? OR LET IT RUN? ANALYSE PERFORMANCE
    protected ProjectTreeEntry findProjectTree(Globals.APP appType, Long projectId) {
        DSEntry currentProject = projectManager.findProjectForTypeAndId(appType, projectId);

        ProjectTreeEntry tree = new ProjectTreeEntry(currentProject);

        //child projects
        List<ProjectTreeEntry> childTrees = projectManager.findChildEntriesForProject(currentProject);
        tree.setProjects(childTrees);

        //parent projects
        tree = projectManager.fillParentEntriesForEntry(tree, appType, currentProject);

        return tree;
    }


    @Override
    protected Globals.USER_ROLE requiredRole() {
        return Globals.USER_ROLE.ROLE_USER;
    }

}

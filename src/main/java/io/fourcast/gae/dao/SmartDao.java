package io.fourcast.gae.dao;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
import io.fourcast.gae.model.roots.SmartRoot;
import io.fourcast.gae.model.smart.SmartProject;
import io.fourcast.gae.model.user.DSUser;
import io.fourcast.gae.utils.Globals;
import io.fourcast.gae.utils.exceptions.ConstraintViolationsException;
import io.fourcast.gae.utils.exceptions.E2EServerException;
import io.fourcast.gae.utils.exceptions.ExceptionWrapper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//smart has no project root
@SuppressWarnings("serial")
public class SmartDao extends AbstractDao<SmartProject> {

    private UserDao userDao = new UserDao();
    private SearchManager searchManager = new SearchManager();

    /**
     * get a smart project by providing the id of the project
     *
     * @param smartProjectId the id of the project to retrieve
     * @return the project with the given id, or null if no project is found
     */
    public SmartProject getSmartProject(Long smartProjectId) {
        Preconditions.checkNotNull(smartProjectId, "ProjectId cannot be NULL");

        Key<SmartProject> projectKey = createKey(smartProjectId);
        return ofy().load().key(projectKey).now();
    }

    /**
     * get a multiple smart projects by providing the cursor and number of items
     * cursor is null the first time and should be added for a second request
     * cursor is returned as a part of the projectsresponsedto
     *
     * @param webSafeCursorString the cursor for where to start in the DB with the query. Will
     *                            be filled with the new cursor value
     * @param numberOfItems       the maximum number of items to be returned in 1 batch. Use
     *                            cursor for consecutive batches
     * @return
     */
    public Map<Globals.PROJECT_MAP, Object> getNumberOfSmartProjects(String webSafeCursorString, int numberOfItems) {
        Query<SmartProject> query = query();

        List<SmartProject> responseList = new ArrayList<SmartProject>();
        int itemCounter = 0;

        if (webSafeCursorString != null)
            query.startAt(Cursor.fromWebSafeString(webSafeCursorString));

        QueryResultIterator<SmartProject> iterator = query.iterator();
        while (iterator.hasNext()) {
            itemCounter++;
            responseList.add(iterator.next());
            if (itemCounter == numberOfItems) {
                break;
            }
        }

        Map<Globals.PROJECT_MAP, Object> response = new HashMap<Globals.PROJECT_MAP, Object>();
        response.put(Globals.PROJECT_MAP.CURSOR, iterator.getCursor().toWebSafeString());
        response.put(Globals.PROJECT_MAP.PROJECTLIST, responseList);

        return response;
    }


    /**
     * save or update a new SMART project
     *
     * @param project
     * @return
     * @throws ConstraintViolationsException
     */
    public SmartProject saveSMARTProject(final SmartProject project) throws E2EServerException, ConstraintViolationsException {
        validate(project);
        SmartProject savedSmartProject = project;
        try {

                    savedSmartProject = ofy().transact(new Work<SmartProject>() {
                @Override
                public SmartProject run() {

                    if (project.getId() != null) {
                        //validate timestamp for existing project
                        try {
                            validateTimestamp(project, getSmartProject(project.getId()));
                        } catch (E2EServerException e) {
                        	throw new ExceptionWrapper(ExceptionWrapper.EXCEPTION_TYPE.E2ESERVEREXCEPTION, e.getMessage());
                        }
                        //FE can't update list of linked subprojects. Always override with old data. Subproject links itself to root, only then this list is updated
                        Key<SmartProject> projectKey = createKey(project.getId());
                        SmartProject storedSmartProject = ofy().load().key(projectKey).now();
                        //migt have been an invalid ID
                        if (storedSmartProject != null) {
                            project.setSubProjectIds(storedSmartProject.getSubProjectIds());
                        }
                    }

                    //store this (potentially new) smart project so we are sure of our project key, since we might need to set it as a sub of the parent

                    //the subproject is a VIRTUAL child of the PARENT, not an ACTUAL DS link --> avoids trouble with recreating ancestor path
                    //when we're on e.g. LMD and need to recreate the complete ancestor path: smart - coca - space. No nested smart parents, only linked
                    project.setRootProject(ancestor());
                    Key<SmartProject> newProjectKey = ofy().save().entity(project).now();

                    //if this smart project is a subproject, add it to the parent's list of children
                    if (project.getParentId() != 0) {
                        SmartProject parentProject = getSmartProject(project.getParentId());
                        parentProject.addSubProjectId(newProjectKey.getId());

                        //no need for a synchronous call
                        ofy().save().entity(parentProject);
                    }


                    return ofy().load().key(newProjectKey).now();
                }
            });

            if(savedSmartProject.equalsIgnoreModifDateAndRootProject(project)){


            }
            return savedSmartProject;
        } catch (ExceptionWrapper e) {
            throw new E2EServerException(e.getErrorMessage());
        }
    }

    /**
     * @param e2eUser
     * @return
     */
    @SuppressWarnings("static-access")
    public List<SmartProject> getSmartProjectsForUser(DSUser e2eUser) {
        Ref<DSUser> userRef = Ref.create(userDao.e2eUserKey(e2eUser.getId()));
        List<SmartProject> smartProjectsOfUser = query().filter("owner", userRef).list();

        return smartProjectsOfUser;
    }


    /**
     * @return
     */
    public List<SmartProject> getAllSmartProjects() {
        List<SmartProject> response = query().list();
        return response;
    }

    public List<SmartProject> getLatestSmartProjects() {
        List<String> allowedSmartProjectStates = new ArrayList<String>();

        allowedSmartProjectStates.add(SmartProject.SMART_PROJECT_STATUS.PRESTUDY.toString());
        allowedSmartProjectStates.add(SmartProject.SMART_PROJECT_STATUS.STUDY.toString());
        allowedSmartProjectStates.add(SmartProject.SMART_PROJECT_STATUS.PLAN.toString());
        allowedSmartProjectStates.add(SmartProject.SMART_PROJECT_STATUS.DO.toString());
        allowedSmartProjectStates.add(SmartProject.SMART_PROJECT_STATUS.CHECK.toString());
        allowedSmartProjectStates.add(SmartProject.SMART_PROJECT_STATUS.ACT.toString());
        allowedSmartProjectStates.add(SmartProject.SMART_PROJECT_STATUS.BAURTB.toString());

        return query().filter("status in ", allowedSmartProjectStates).order("-creationDate").limit(5).list();
    }

    @Override
    public Key<SmartRoot> ancestor() {
        return Key.create(SmartRoot.class, SmartRoot.ID);
    }

    public String generateNewSmartCode() {
        return ofy().transact(new Work<String>() {
            @Override
            public String run() {


                SmartCodeGenerator smartCodeGenerator = getSmartCodeGenerator();
                String newCode = smartCodeGenerator.generateNewCode();
                //save because generate function generates code without save
                ofy().save().entity(smartCodeGenerator).now();

                return newCode;
            }
        });
    }

    public SmartCodeGenerator getSmartCodeGenerator() {
        //create keyGen if not existing
        // TODO check and create smartKeyGenerator at warmup or when creating the dao
        SmartCodeGenerator smartCodeGenerator = ofy().load().key(Key.create(SmartCodeGenerator.class, SmartCodeGenerator.CODE)).now();

        if (smartCodeGenerator == null) {
            smartCodeGenerator = new SmartCodeGenerator();
            smartCodeGenerator.setCounter(1);
            ofy().save().entity(smartCodeGenerator).now();
        }

        return smartCodeGenerator;
    }
    
    public void addProjectToSearchDS(final SmartProject savedProject) throws ParseException {
        ofy().transact(new VoidWork() {
            public void vrun() {
                Document.Builder documentBuilder = indexGenericFields(savedProject);
                
                documentBuilder.setId(Key.create(SmartProject.class, savedProject.getId()).toWebSafeString());

                documentBuilder.addField(Field.newBuilder().setName("status").setText(savedProject.getStatus().toString().toLowerCase()));

                if (savedProject.getParentId() != 0) {
                    //change to cp number
                    documentBuilder.addField(Field.newBuilder().setName("parent").setText(String.valueOf(savedProject.getParentId())));
                }

                if (savedProject.getSponsor() != null) {
                    documentBuilder.addField(Field.newBuilder().setName("sponsor").setText(savedProject.getSponsor().getDisplayName().toLowerCase()));
                }

                if (savedProject.getProjectStartDate() != null) {
                    documentBuilder.addField(Field.newBuilder().setName("startDate").setText(savedProject.getProjectStartDate().toString().toLowerCase()));
                }


                documentBuilder.addField(Field.newBuilder().setName("type").setText(Globals.APP.APP_SMART.toString()));
                documentBuilder.addField(Field.newBuilder().setName("code").setText(savedProject.getProjectCode()));
                documentBuilder.addField(Field.newBuilder().setName("ownerEmail").setText(savedProject.getOwner().getEmail().toLowerCase()));
                documentBuilder.addField(Field.newBuilder().setName("owner").setText(savedProject.getOwner().getDisplayName().toLowerCase()));

                Document document = documentBuilder.build();

                SearchManager searchManager = new SearchManager();
                searchManager.indexADocument("E2E", document);
            }
        });
    }


    public List<SmartProject> getAvailableSmartsForCommsProject(Long commsParentProjectId){
        List<SmartProject> availableSmarts = query()
                .project("projectName")
                .project("projectCode")
                .project("owner")
                .filter("active", Boolean.TRUE)
                .list();

        if(commsParentProjectId != null) {
            availableSmarts.addAll(query()
                    .project("projectName")
                    .project("projectCode")
                    .project("owner")
                    .filter("linkedCommsProjectId", commsParentProjectId)
                    .list());
        }

        return availableSmarts;


    }
}
package io.fourcast.gae.dao;

import com.google.appengine.api.datastore.ReadPolicy.Consistency;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import io.fourcast.gae.model.cocamat.CocamatCodeGenerator;
import io.fourcast.gae.model.cocamat.CocamatProject;
import io.fourcast.gae.model.cocamat.CocamatScenarioTemplate;
import io.fourcast.gae.model.common.DSEntry;
import io.fourcast.gae.model.comms.CommsChannel;
import io.fourcast.gae.model.comms.CommsProject;
import io.fourcast.gae.model.comms.CommsTask;
import io.fourcast.gae.model.comms.codegenerators.CommsChannelCodeGenerator;
import io.fourcast.gae.model.comms.codegenerators.CommsProjectCodeGenerator;
import io.fourcast.gae.model.comms.codegenerators.CommsTaskCodeGenerator;
import io.fourcast.gae.model.lmd.LmdProject;
import io.fourcast.gae.model.lmd.LmdProjectCodeGenerator;
import io.fourcast.gae.model.lmd.LmdTask;
import io.fourcast.gae.model.lmd.LmdTaskCodeGenerator;
import io.fourcast.gae.model.smart.SmartProject;
import io.fourcast.gae.model.space.SpaceProject;
import io.fourcast.gae.model.space.SpaceTargetGroupCode;
import io.fourcast.gae.model.user.DSUser;
import io.fourcast.gae.utils.exceptions.ConstraintViolationsException;
import io.fourcast.gae.utils.exceptions.E2EServerException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public abstract class AbstractDao<T> implements Serializable {

    private final static Lock lock = new ReentrantLock();
    public static String SENTINEL = "\ufffd";
    private static boolean objectifyInitialized = false;
    private static ValidatorFactory VALIDATION_FACTORY = null;
    @SuppressWarnings("unchecked")
    private final Class<T> typeClass = ((Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]);
    final Logger log = Logger.getLogger(typeClass.getName());
    private UserDao userDao = null;

    public AbstractDao() {
        registerDatastore();
    }

    /**
     * get {@link ValidatorFactory} using lazy loading
     *
     * @return
     */
    public static ValidatorFactory getValidatorFactory() {
        lock.lock();
        try {
            if (VALIDATION_FACTORY == null) {
                VALIDATION_FACTORY = Validation.byDefaultProvider().configure().buildValidatorFactory();
            }
        } finally {
            lock.unlock();
        }
        return VALIDATION_FACTORY;
    }

    public abstract Key<?> ancestor();

    public Class<T> type() {
        return typeClass;
    }

    public Query<T> query() {
        return ofy().load().type(type()).ancestor(ancestor()); //.filter("active", Boolean.TRUE);
    }

    private Type getFieldType(Class clazz, String fieldname) throws E2EServerException {
        try {
            return clazz.getDeclaredField(fieldname).getGenericType();
        } catch (NoSuchFieldException ex) {
            if (clazz.getSimpleName().equals("Object")) {
                throw new E2EServerException("No field found with the name " + fieldname);
            }
            return getFieldType(clazz.getSuperclass(), fieldname);
        }
    }

    public List<T> executeQueryForFilters(Map<String, List<String>> filters) throws E2EServerException {

        return executeQueryForFilters(filters, typeClass);
    }

    public List<T> executeQueryForFilters(Map<String, List<String>> filters, Class clazz) throws E2EServerException {
        Query<T> q = query();
        Map<String, List<String>> postSelectFilters = new HashMap<>();
        if (filters != null && filters.keySet() != null && filters.keySet().size() >= 1) {
            for (String key : filters.keySet()) {
                /*
                * Determine type of param to convert to correct type so we are able to use it as a filter in datastore
                * */
                if (key.equals("id")) {
                    if (!this.getFieldType(clazz, key).toString().equals("class java.lang.Long")) {
                        throw new E2EServerException("not supported datatype for key");
                    }
                    /*Dirty fix*/
                    if (filters.get(key).size() == 0) {
                        filters.get(key).add("-1");
                    }
                    q = applyFilterKeys(q, filters.get(key));


                } else {
                    switch (this.getFieldType(clazz, key).toString()) {
                        case "com.googlecode.objectify.Ref<io.fourcast.gae.model.user.DSUser>":
                            q = prepareUsersForFilter(q, filters.get(key), key);
                            break;

                        case "class java.lang.Long":
                            /*dirty fix*/
                            if (key.equals("parentId") && filters.get(key).size() == 0) {
                                filters.get(key).add("-1");
                            }
                            q = prepareLongsForFilter(q, filters.get(key), key);
                            break;

                        case "java.util.List<java.lang.Long>":
                            q = prepareLongsForFilters(q, filters.get(key), key);
                            break;

                        case "class java.lang.Boolean":
                            q = prepareBooleanForFilter(q, filters.get(key), key);
                            break;

                        case "class java.util.Date":
                            postSelectFilters.put(key, filters.get(key));
                            break;

                        case "class java.lang.Integer":
                            q = prepareIntegerForFilter(q, filters.get(key), key);
                            break;

                        case "java.util.Map<java.lang.String, java.lang.Integer>":
                            postSelectFilters.put(key, filters.get(key));
                            break;

                        default:
                            System.out.println(this.getFieldType(clazz, key).toString());

                            q = applyFilter(q, key, filters.get(key));
                            break;
                    }
                }
            }
        }

        return filterListByFilters(q.list(), postSelectFilters, clazz);
    }


    private List<T> filterListByFilters(List<T> listToFilter, Map<String, List<String>> filters, Class clazz) throws E2EServerException {
        T filteredProjectsWithoutDateFilter;
        Object value;

        int index;
        for (String filterKey : filters.keySet()) {
            List<String> filterSettings = filters.get(filterKey);
            index = listToFilter.size() - 1;
            while (index >= 0) {

                filteredProjectsWithoutDateFilter = listToFilter.get(index);

                /*Get data from the field even if it is private ;-)*/
                value = getValue(filteredProjectsWithoutDateFilter, filterKey);

                if (this.getFieldType(clazz, filterKey).toString().equals("class java.util.Date") && validateValidDateFilter(value, filterKey, filterSettings)) {
                    listToFilter.remove(filteredProjectsWithoutDateFilter);
                }

                if (this.getFieldType(clazz, filterKey).toString().equals("java.util.Map<java.lang.String, java.lang.Integer>") && validateMapFilter(value, filterKey, filterSettings)) {
                    listToFilter.remove(filteredProjectsWithoutDateFilter);
                }

                --index;
            }
        }
        return listToFilter;

    }

    private Object getValue(T project, String fieldName) throws E2EServerException {
        try {
            java.lang.reflect.Field field = project.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(project);
        } catch (IllegalAccessException e) {
            throw new E2EServerException(e);
        } catch (NoSuchFieldException e) {
            throw new E2EServerException(e);
        }
    }


    protected Boolean validateMapFilter(Object value, String filterKey, List<String> filtersettings) {

        return true;
    }

    private Boolean validateValidDateFilter(Object value, String filterKey, List<String> filterSettings) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


        if (value != null) {
            Calendar minDateValue = Calendar.getInstance(), maxDateValue = Calendar.getInstance();
            if (filterSettings != null && filterSettings.size() == 2) {

                try {
                    minDateValue.setTime(sdf.parse(filterSettings.get(0)));
                    maxDateValue.setTime(sdf.parse(filterSettings.get(1)));
                    maxDateValue.add(Calendar.HOUR, -1);
                    maxDateValue.add(Calendar.HOUR, 1);

                    Calendar dateValue = Calendar.getInstance();
                    dateValue.setTime((Date) value);
                    return minDateValue.after(dateValue) || maxDateValue.before(dateValue);
                } catch (ParseException e) {
                    return true;

                }
            }
        }
        return true;
    }


    private Query<T> applyFilterKeys(Query<T> q, List<String> values) {
        if (values != null && values.size() >= 1) {
            List<Key<T>> keys = new ArrayList<>();
            for (String value : values) {
                keys.add(createKey(new Long(value)));
            }
            return q.filterKey("in", keys);
        }
        return q;
    }

    private Query<T> prepareUsersForFilter(Query<T> q, List<String> values, String key) {
        userDao = new UserDao();
        List<Ref<DSUser>> users = new ArrayList<>();
        List<String> usersIds = values;
        for (String userId : usersIds) {
            users.add(Ref.create(userDao.getUser(userId)));
        }
        return applyFilter(q, key, users);
    }

    private Query<T> prepareLongsForFilter(Query<T> q, List<String> values, String key) {
        userDao = new UserDao();
        List<Long> longValues = new ArrayList<>();
        for (String value : values) {
            longValues.add(new Long(value));
        }
        return applyFilter(q, key, longValues);
    }


    private Query<T> prepareIntegerForFilter(Query<T> q, List<String> values, String key) {
        userDao = new UserDao();
        List<Integer> integerValues = new ArrayList<>();
        for (String value : values) {
            integerValues.add(new Integer(value));
        }
        return applyFilter(q, key, integerValues);

    }

    private Query<T> prepareBooleanForFilter(Query<T> q, List<String> values, String key) {
        userDao = new UserDao();
        if (values != null && values.size() >= 1) {
            return applyFilter(q, key, new Boolean(values.get(0)));
        }
        return q;

    }

    private Query<T> prepareLongsForFilters(Query<T> q, List<String> values, String key) {
        userDao = new UserDao();
        List<Long> longValues = new ArrayList<>();
        for (String value : values) {
            longValues.add(new Long(value));
            q = applyFilter(q, key, new Long(value));
        }
        return q;
    }

    private Query<T> applyFilter(Query<T> q, String key, List<? extends Object> values) {
        if (values == null || values.size() == 0) {
            return q;
        }
        return q.filter(key + " in ", values);
    }

    private Query<T> applyFilter(Query<T> q, String key, Object values) {

        return q.filter(key, values);
    }

    public List<T> getByIds(List<Long> Ids) {
        return new ArrayList<>(ofy().load().keys(createKeys(Ids)).values());
    }


    public List<T> getByIds(List<Long> Ids, Boolean collectParent, Boolean collectChildren) {
        return this.getByIds(Ids);
    }


    public List<T> getByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.size() == 0) {
            return null;
        }
        return query().filter("parentId in ", parentIds).list();
    }

    public Key<T> createKey(Long id) {
        return Key.create(ancestor(), typeClass, id);
    }

    public List<Key<T>> createKeys(List<Long> ids) {
        List<Key<T>> keys = new ArrayList<>();

        if (ids == null) {
            return keys;
        }

        for (Long id : ids) {
            if(id != 0L) {
                keys.add(createKey(id));
            }
        }
        return keys;
    }

    public Key<T> createKey(String id) {
        return Key.create(ancestor(), typeClass, id);
    }

    public void registerDatastore() {
        lock.lock();
        try {
            if (!objectifyInitialized) {
                log.info("registering datastore entities"); //$NON-NLS-1$
                ObjectifyService.register(DSUser.class);
                ObjectifyService.register(SmartProject.class);
                ObjectifyService.register(CocamatProject.class);
                ObjectifyService.register(FieldOption.class);
                ObjectifyService.register(CocamatScenarioTemplate.class);
                ObjectifyService.register(ChannelOption.class);
                ObjectifyService.register(CocamatCodeGenerator.class);
                ObjectifyService.register(SmartCodeGenerator.class);
                ObjectifyService.register(LmdTaskCodeGenerator.class);
                ObjectifyService.register(LmdProjectCodeGenerator.class);
                ObjectifyService.register(SpaceProject.class);
                ObjectifyService.register(CommsProject.class);
                ObjectifyService.register(CommsChannel.class);
                ObjectifyService.register(CommsTask.class);
                ObjectifyService.register(LmdProject.class);
                ObjectifyService.register(LmdTask.class);
                ObjectifyService.register(SpaceTargetGroupCode.class);
                ObjectifyService.register(Notification.class);
                ObjectifyService.register(CommsProjectCodeGenerator.class);
                ObjectifyService.register(CommsChannelCodeGenerator.class);
                ObjectifyService.register(CommsTaskCodeGenerator.class);
                objectifyInitialized = true;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * get an instance of Objectify.
     * <p/>
     * If the transaction was still active, then the same instance is returned;
     *
     * @return
     */
    public Objectify ofy() {
        return ObjectifyService.ofy().cache(true).consistency(Consistency.STRONG);
    }

    /**
     * Get the memcacheService
     */
    public MemcacheService mc() {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        return syncCache;
    }

    public void validate(Object databaseObject) throws ConstraintViolationsException {
        validate(databaseObject, null);
    }

    public void validate(Object databaseObject, Class<?> groups) throws ConstraintViolationsException {
        String causeMessage = "";
        Set<ConstraintViolation<Object>> violations;
        if (groups == null) {
            violations = getValidatorFactory().getValidator().validate(databaseObject);
        } else {
            violations = getValidatorFactory().getValidator().validate(databaseObject, groups);
        }
        if (violations.size() > 0) {
            for (ConstraintViolation<Object> constraintViolation : violations) {
                String field = constraintViolation.getPropertyPath().toString();
                log.log(Level.WARNING,
                        "constraint violation " + databaseObject + ": " + constraintViolation.getMessage());
                causeMessage += "constraint violation " + databaseObject.getClass().getSimpleName() + " - " + field + ": " + constraintViolation.getMessage() + "\n";
            }
            //throw new ConstraintViolationsException(databaseObject, violations);
            throw new ConstraintViolationsException(causeMessage);

        }
    }

    public void validateTimestamp(DSEntry toSaveProject, DSEntry dbProject) throws E2EServerException {
        //no update, first save, so all OK
        if (dbProject == null) {
            return;
        }
        if (dbProject.getLastModified() == null) {
            throw new E2EServerException("Severe error: DSEntry " +
                    dbProject.getProjectName() +
                    " has been saved without timestamp!", dbProject);
        }
        if (toSaveProject == null) {
            throw new E2EServerException("Passed a NULL project to save");
        }
        //no timestamp provided
        if (toSaveProject.getLastModified() == null && dbProject != null) {
            throw new E2EServerException("DSEntry " +
                    dbProject.getProjectName() +
                    " already exists but is missing timestamp.", toSaveProject);
        }

        //data ok, is actual update --> check if project in db has been updated in the meanwhile
        //specific 412 code for FE to let them know its a TS error, different response needed in FE.
        if (toSaveProject.getLastModified().getTime() < dbProject.getLastModified().getTime()) {
            throw new E2EServerException(412, "DSEntry " + toSaveProject.getProjectName() + " was updated by another user.", dbProject);
        }
    }

    public Document.Builder indexGenericFields(DSEntry projectToIndex) {
        FieldOptionDao fieldOptionDao = new FieldOptionDao();
        Document.Builder documentBuilder = Document.newBuilder();
        // TODO check if cocamat could use a status as well as the other project types
        //documentBuilder.addField(Field.newBuilder().setName("status").setText(savedProject.getStatus().toString().toLowerCase()));
        if (projectToIndex.getProjectName() != null) {
            documentBuilder.addField(Field.newBuilder().setName("name").setText(projectToIndex.getProjectName()));

            String autoCompleteNameString = "";
            for (String partialName : projectToIndex.getProjectName().split(" ")) {
                for (int i = 3; i <= partialName.length(); i++) {
                    autoCompleteNameString += partialName.substring(0, i) + " ";
                }
            }
            documentBuilder.addField(Field.newBuilder().setName("autoName").setText(autoCompleteNameString));
        }

        if (projectToIndex.getLastModified() != null) {
            documentBuilder.addField(Field.newBuilder().setName("modified").setDate(projectToIndex.getLastModified()));
        }

        if (projectToIndex.getCreationDate() != null) {
            documentBuilder.addField(Field.newBuilder().setName("creation").setDate(projectToIndex.getCreationDate()));
        }

        if (projectToIndex.getBrand() != null) {
            documentBuilder.addField(Field.newBuilder().setName("brand").setText(projectToIndex.getBrand().toString()));

            if (projectToIndex.getDepartmentId() != null && projectToIndex.getDepartmentId() != 0L) {
                String departmentStringEN = fieldOptionDao.getFieldOptionDetails("E2E_DEPARTMENT",
                        String.valueOf(projectToIndex.getDepartmentId()), projectToIndex.getBrand()).getEN();
                String departmentStringFR = fieldOptionDao.getFieldOptionDetails("E2E_DEPARTMENT",
                        String.valueOf(projectToIndex.getDepartmentId()), projectToIndex.getBrand()).getFR();
                String departmentStringNL = fieldOptionDao.getFieldOptionDetails("E2E_DEPARTMENT",
                        String.valueOf(projectToIndex.getDepartmentId()), projectToIndex.getBrand()).getNL();
                documentBuilder.addField(Field.newBuilder().setName("department").setText(departmentStringEN));
                documentBuilder.addField(Field.newBuilder().setName("department").setText(departmentStringFR));
                documentBuilder.addField(Field.newBuilder().setName("department").setText(departmentStringNL));
            }
        }
        return documentBuilder;
    }

    protected DSEntry prepareProjectFromParent(DSEntry project, Boolean parentActive, Long parentId, String parentProjectCode) {
        if (parentId == null) {
            parentId = 0L;
        }

        if (project != null) {
            if (project.getActive() == null) {
                project.setActive(true);
            }
            project.setActive(project.getActive() && parentActive);
            project.setParentId(parentId);
            project.setRootProject(ancestor());
            project.setParentProjectCode(parentProjectCode);
        }
        return project;
    }


    protected DSEntry prepareProject(DSEntry project, Boolean parentActive, Long parentId, String parentProjectCode) {
        project = prepareProjectFromParent(project, parentActive, parentId, parentProjectCode);
        return project;
    }

    public List<? extends DSEntry> prepareProjects(List<? extends DSEntry> projects, Boolean parentActive, Long parentId, String parentProjectCode) {
        List<DSEntry> alteredTasks = new ArrayList<>();
        if (projects != null) {
            for (DSEntry project : projects) {
                alteredTasks.add(prepareProject(project, parentActive, parentId, parentProjectCode));
            }
        }
        return alteredTasks;
    }

    protected List<? extends DSEntry> prepareProjects(List<? extends DSEntry> projects) {
        List<DSEntry> alteredTasks = new ArrayList<>();
        if (projects != null) {
            for (DSEntry project : projects) {
                alteredTasks.add(prepareProject(project, project.getActive(), project.getParentId(), project.getParentProjectCode()));
            }
        }
        return alteredTasks;
    }

    public List<? extends DSEntry> prepareProjects(List<? extends DSEntry> project, DSEntry parentProject, String parentProjectCode) {
        return prepareProjects(project, parentProject.getActive(), parentProject.getId(), parentProjectCode);
    }
}
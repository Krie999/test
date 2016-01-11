package io.fourcast.gae.dao;

import com.google.appengine.api.datastore.ReadPolicy.Consistency;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;
import io.fourcast.gae.model.common.DSEntry;
import io.fourcast.gae.model.project.Project;
import io.fourcast.gae.model.user.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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


    private Object getValue(T project, String fieldName) throws Exception {
        try {
            java.lang.reflect.Field field = project.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(project);
        } catch (IllegalAccessException e) {
            throw new Exception(e);
        } catch (NoSuchFieldException e) {
            throw new Exception(e);
        }
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
                ObjectifyService.register(User.class);
                ObjectifyService.register(Project.class);
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

    public void validate(Object databaseObject) throws Exception {
        validate(databaseObject, null);
    }

    public void validate(Object databaseObject, Class<?> groups) throws Exception {
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
            throw new Exception(causeMessage);

        }
    }

    public void validateTimestamp(DSEntry toSaveProject, DSEntry dbProject) throws Exception {
        //no update, first save, so all OK
        if (dbProject == null) {
            return;
        }
        if (dbProject.getLastModified() == null) {
            throw new Exception("Severe error: DSEntry " +
                    dbProject.getId() +
                    " has been saved without timestamp!");
        }
        if (toSaveProject == null) {
            throw new Exception("Passed a NULL project to save");
        }
        //no timestamp provided
        if (toSaveProject.getLastModified() == null && dbProject != null) {
            throw new Exception("DSEntry " +
                    dbProject.getId() +
                    " already exists but is missing timestamp.");
        }

        //data ok, is actual update --> check if project in db has been updated in the meanwhile
        //specific 412 code for FE to let them know its a TS error, different response needed in FE.
        if (toSaveProject.getLastModified().getTime() < dbProject.getLastModified().getTime()) {
            throw new Exception("DSEntry " + toSaveProject.getId() + " was updated by another user.");
        }
    }

}
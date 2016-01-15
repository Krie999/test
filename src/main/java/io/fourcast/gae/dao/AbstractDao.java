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
import io.fourcast.gae.util.exceptions.ConstraintViolationsException;
import io.fourcast.gae.util.exceptions.FCServerException;
import io.fourcast.gae.util.exceptions.FCTimestampConflictException;
import io.fourcast.gae.util.exceptions.FCUserException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public abstract class AbstractDao<T> implements Serializable {

    private final static Lock lock = new ReentrantLock();
    private static boolean objectifyInitialized = false;
    private static ValidatorFactory VALIDATION_FACTORY;

    @SuppressWarnings("unchecked")
    private final Class<T> typeClass = ((Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]);
    final Logger log = Logger.getLogger(typeClass.getName());


    /**
     * Constructor of a Dao prepares OFY.
     */
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

    /**
     * Abstract method to define the ancestor of each Entity. This way each entity of the same kind
     * belongs to the same ancestor group, making it strongly consistent and usable in a txn
     *
     * @return the ancestor defined for this given Entity kind.
     */
    public abstract Key<?> ancestor();

    /**
     * The Entity kind for which this Dao is initialised
     *
     * @return the Entity kind class
     */
    public Class<T> type() {
        return typeClass;
    }

    /**
     * A default query for the Entity kind. Strongly consistent since it uses the ancestor that is defined
     * for this Entity kind.
     *
     * @return
     */
    public Query<T> query() {
        return ofy().load().type(type()).ancestor(ancestor()); //.filter("active", Boolean.TRUE);
    }

    /**
     * Creates an OFY key for this Entity kind with the given ID, and the ancestor defined.
     * Used to reconstruct keys when only ID is known.
     *
     * @param id the ID for which to generate a KEY.
     * @return
     */
    public Key<T> createKey(Long id) {
        return Key.create(ancestor(), typeClass, id);
    }

    /**
     * * Creates List of OFY keys for this Entity kind with the given IDs, and the ancestor defined.
     * Used to reconstruct keys when only IDs are known.
     *
     * @param ids the ID's for which to generate the Keys.
     * @return
     */
    public List<Key<T>> createKeys(List<Long> ids) {
        List<Key<T>> keys = new ArrayList<>();

        if (ids == null) {
            return keys;
        }

        for (Long id : ids) {
            if (id != 0L) {
                keys.add(createKey(id));
            }
        }
        return keys;
    }

    /**
     * Creates an OFY key for this Entity kind with the given name (String ID), and the ancestor defined.
     * Used to reconstruct keys when only the name (String ID) is known.
     *
     * @param name the name (String ID) for which to generate a KEY.
     * @return
     */
    public Key<T> createKey(String name) {
        return Key.create(ancestor(), typeClass, name);
    }

    /**
     * registers all OFY Entity classes
     */
    public void registerDatastore() {
        lock.lock();
        try {
            if (!objectifyInitialized) {
                log.info("registering Datastore entities");
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
     * If the transaction was still active, then the same instance is returned;
     *
     * @return the OFY instance
     */
    public Objectify ofy() {
        return ObjectifyService.ofy().cache(true).consistency(Consistency.STRONG);
    }


    /**
     * //TODO test for collection implementation. never used before!
     * Validates a single Entity based on the annotations provided on that Entity. Uses null as a group.
     *
     * @param databaseObject A Collection of Objects or an Object that needs validation.
     * @throws ConstraintViolationsException
     */
    public void validate(Object databaseObject) throws ConstraintViolationsException {
        if (databaseObject instanceof Collection<?>) {
            Iterator i = ((Collection<?>) databaseObject).iterator();
            while (i.hasNext()) {
                validate(i.next(), null);
            }
        } else {
            validate(databaseObject, null);
        }
    }

    /**
     * Validates a single Entity based on the annotations provided on that Entity.
     *
     * @param databaseObject The object to validate
     * @param groups         groups the group or list of groups targeted for validation. (defaults to {@link Default})
     * @throws ConstraintViolationsException
     */
    public void validate(Object databaseObject, Class<?> groups) throws ConstraintViolationsException {
        String causeMessage = "";
        Set<ConstraintViolation<Object>> violations;
        if (groups == null) {
            violations = getValidatorFactory().getValidator().validate(databaseObject);
        } else {
            violations = getValidatorFactory().getValidator().validate(databaseObject, groups);
        }

        //no violations
        if (violations.size() <= 0) {
            return;
        }
        //concat all violations to show FE all errors.
        for (ConstraintViolation<Object> constraintViolation : violations) {
            String field = constraintViolation.getPropertyPath().toString();
            log.log(Level.WARNING,
                    "constraint violation " + databaseObject + ": " + constraintViolation.getMessage());
            causeMessage += "constraint violation " + databaseObject.getClass().getSimpleName() + " - " + field + ": " + constraintViolation.getMessage() + "\n";
        }
        throw new ConstraintViolationsException(databaseObject, violations);
    }

    /**
     * @param toSaveProject
     * @param dbProject
     * @throws FCTimestampConflictException
     * @throws FCUserException
     * @throws FCServerException
     */
    public void validateTimestamp(DSEntry toSaveProject, DSEntry dbProject) throws FCTimestampConflictException, FCUserException, FCServerException {
        //no update, first save, so all OK
        if (dbProject == null) {
            return;
        }
        if (dbProject.getLastModified() == null) {
            throw new FCServerException("Severe error: DSEntry " +
                    dbProject.getId() +
                    " has been saved without timestamp!");
        }
        if (toSaveProject == null) {
            throw new FCUserException("Passed a NULL project to save");
        }
        //no timestamp provided
        if (toSaveProject.getLastModified() == null && dbProject != null) {
            throw new FCTimestampConflictException("DSEntry " +
                    dbProject.getId() +
                    " already exists but is missing timestamp.");
        }

        // data provided is ok, now check if project in db has been updated in the meanwhile by comparing timestamps.
        // specific 412 code for FE to let them know its a TS error, different response needed in FE.
        if (toSaveProject.getLastModified().getTime() < dbProject.getLastModified().getTime()) {
            throw new FCTimestampConflictException("DSEntry " + toSaveProject.getId() + " was updated by another user.");
        }
    }

    /**
     * Get the MemCacheService
     *
     * @return the GAE MemCacheService
     */
    public MemcacheService mc() {
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        return syncCache;
    }
}
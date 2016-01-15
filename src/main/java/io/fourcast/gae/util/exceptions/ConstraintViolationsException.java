package io.fourcast.gae.util.exceptions;

import com.google.api.server.spi.ServiceException;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Created by nbuekers on 15/01/16.
 */

@SuppressWarnings("serial")
public class ConstraintViolationsException extends ServiceException{

    private Object databaseObject;
    private Set<ConstraintViolation<Object>> violations;

    public ConstraintViolationsException(Object databaseObject,
                                         Set<ConstraintViolation<Object>> violations) {

        super(400, "constraint violation");

        this.databaseObject = databaseObject;
        this.violations = violations;
    }

    public ConstraintViolationsException(String cause) {

        super(400, cause);
    }

    public Object getDatabaseObject() {
        return databaseObject;
    }

    public void setDatabaseObject(Object databaseObject) {
        this.databaseObject = databaseObject;
    }

    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }

    public void setViolations(Set<ConstraintViolation<Object>> violations) {
        this.violations = violations;
    }

}

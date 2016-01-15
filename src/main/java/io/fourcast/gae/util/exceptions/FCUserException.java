package io.fourcast.gae.util.exceptions;

import com.google.api.server.spi.ServiceException;

/**
 * Created by nbuekers on 15/01/16.
 */
public class FCUserException extends ServiceException {
    public FCUserException(String description,String statusMessage) {
        super(400,"User Error\n----------------------------\n" + description + "\n" + statusMessage);
    }

    public FCUserException(String s) {
        super(400,s);
    }
}

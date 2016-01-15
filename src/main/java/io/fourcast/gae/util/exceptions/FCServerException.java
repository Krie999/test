package io.fourcast.gae.util.exceptions;

import com.google.api.server.spi.ServiceException;

/**
 * Created by nbuekers on 15/01/16.
 */
public class FCServerException extends ServiceException{
    public FCServerException(String description, String statusMessage) {
        super(500,"Server Error\n----------------------------\n" + description + "\n" + statusMessage);
    }

    public FCServerException(String s) {
        super(500,s);
    }
}

package io.fourcast.gae.util.exceptions;

import com.google.api.server.spi.ServiceException;

/**
 * Created by nbuekers on 15/01/16.
 */
public class FCTimestampConflictException extends ServiceException {
    public FCTimestampConflictException(String statusMessage) {
        super(412,statusMessage);
    }
}

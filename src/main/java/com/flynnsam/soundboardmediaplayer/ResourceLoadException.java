package com.flynnsam.soundboardmediaplayer;

/**
 * A runtime exception that is to be thrown when the system cannot
 * Created by Sam on 2017-07-30.
 */

public class ResourceLoadException extends RuntimeException {

    /**
     * Create an exception with a message and a cause
     * @param message The message to include with this exception
     * @param cause The underlying cause of this one
     */
    public ResourceLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

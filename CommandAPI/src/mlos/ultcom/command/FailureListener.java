package mlos.ultcom.command;

import java.util.EventListener;

/**
 * @author Marcin Los
 *
 * Listener interface for receiving failure notifications
 */
public interface FailureListener extends EventListener
{
    /**
     * Invoked when an error occurs
     * 
     * @param source Source of a failure
     * 
     * @param cause Cause of a failure
     */
    public void failed(Object source, Throwable cause);
}

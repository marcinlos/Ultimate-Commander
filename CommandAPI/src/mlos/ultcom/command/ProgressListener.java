package mlos.ultcom.command;

/**
 * Listner interface for receiving progress update events. 
 * 
 * @author Marcin Los
 */
public interface ProgressListener extends FailureListener
{
    /**
     * Invoked when progress value is changed.
     * 
     * @param e Information about the event
     */
    void progressChange(ProgressEvent e);
    
    /**
     * Invoked when progress reached an end, i.e. observed task
     * has finished.
     */
    void finished(Object source);
}

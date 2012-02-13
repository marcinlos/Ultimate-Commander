package mlos.ultcom.command;

import java.util.EventObject;

/**
 * Container for information about progress in some (potentially long-running)
 * process. Used as a parameter in {@code ProgressListener}.
 * 
 * @author Marcin Los
 */
public class ProgressEvent extends EventObject
{
    private int oldValue, newValue;
    
    /**
     * Creates new {@code ProgressEvent} from specified parameters.
     * 
     * @param source Object that fired this event
     * 
     * @param oldValue Old value of progress
     * 
     * @param newValue New value of progress
     */
    public ProgressEvent(Object source, int oldValue, int newValue)
    {
        super(source);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    /**
     * @return Old progress state
     */
    public int getOldValue()
    {
        return oldValue;
    }

    /**
     * @return New progress state. As a convention, {@code -1} denotes
     * unknown progress state.
     */
    public int getNewValue()
    {
        return newValue;
    }
}

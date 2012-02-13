package mlos.ultcom.core;

/**
 * Specifies required methods of a GUI implementation.
 * 
 * @author Marcin Los
 * 
 * @see ApplicationLoader
 */
public interface ApplicationInterface
{
    /**
     * @return {@code GUIBuilder} implementation, used to notify GUI when
     * new commands and UI elements are found in configuration file.
     */
    GUIBuilder getGUIBuilder();
    
    /**
     * Called by {@code ApplicationLoader} after the preparatory stage
     * (reading config files etc.) is completed.
     */
    void start();
}

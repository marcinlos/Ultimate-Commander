package mlos.ultcom.command;

/**
 * Abstract command interface. 
 * 
 * <p>
 * For commands binded in configuration file public default constructor
 * must be provided, for instances are obtained via reflection.
 * 
 * <p>
 * It should not be used for longer operations
 * (i.e. long enough to disturb event dispatch thread). For such, use
 * {@link mlos.ultcom.command.LongCommand}
 * 
 * @author Marcin Los
 */
public interface Command
{
    /**
     * Executes this command in application's main thread. Should not
     * be called directly. 
     * 
     * @param active Context object of an active panel, providing necessary 
     * information for the command
     * 
     * @param inactive Context object of an inactive panel
     */
    void execute(Context active, Context inactive);
}

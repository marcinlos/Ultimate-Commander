package mlos.ultcom.core;

/**
 * Indicates severe problem caused by the command, like handler creation
 * failure.
 * 
 * @author Marcin Los
 */
public class CommandException extends RuntimeException
{
    public CommandException()
    {
    }

    public CommandException(String message)
    {
        super(message);
    }

    public CommandException(Throwable cause)
    {
        super(cause);
    }

    public CommandException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

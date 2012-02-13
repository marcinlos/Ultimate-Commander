package mlos.ultcom.fs;

/**
 * Indicates an attempt to perform an operation which is not supported by
 * the file system, e.g. attempt to get input stream of a directory.
 * 
 * @author Marcin Los
 */
public class OperationNotSupportedException extends Exception
{
    public OperationNotSupportedException()
    {
    }
    
    public OperationNotSupportedException(String message)
    {
        super(message);
    }
    
    public OperationNotSupportedException(Throwable cause)
    {
        super(cause);
    }
    
    public OperationNotSupportedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}


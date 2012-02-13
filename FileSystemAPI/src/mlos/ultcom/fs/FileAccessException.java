package mlos.ultcom.fs;

/**
 * Thrown by various fs classes to indicate lack of permission
 * for certain operations.
 * 
 * @author Marcin Los
 */
public class FileAccessException extends FileSystemException
{
    public FileAccessException()
    {
    }
    
    public FileAccessException(String message)
    {
        super(message);
    }
    
    public FileAccessException(Throwable cause)
    {
        super(cause);
    }
    
    public FileAccessException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

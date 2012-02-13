package mlos.ultcom.fs;

/**
 * Base class for all file system related exceptions.
 * 
 * @author Marcin Los
 */
public class FileSystemException extends Exception
{
    public FileSystemException()
    {
    }
    
    public FileSystemException(String message)
    {
        super(message);
    }
    
    public FileSystemException(Throwable cause)
    {
        super(cause);
    }
    
    public FileSystemException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

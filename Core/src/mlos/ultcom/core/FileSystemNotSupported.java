package mlos.ultcom.core;

import mlos.ultcom.fs.FileSystemException;

/**
 * Thrown to indicate that requested file system has no implementation.
 * 
 * @author Marcin Los
 */
public class FileSystemNotSupported extends FileSystemException
{
    public FileSystemNotSupported()
    {
    }
    
    public FileSystemNotSupported(String message)
    {
        super(message);
    }
    
    public FileSystemNotSupported(Throwable cause)
    {
        super(cause);
    }
    
    public FileSystemNotSupported(String message, Throwable cause)
    {
        super(message, cause);
    }
}

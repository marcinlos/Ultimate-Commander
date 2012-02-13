package mlos.ultcom.fs;

/**
 * Indicates malformed URI address.
 * 
 * @author Marcin Los
 */
public class MalformedURI extends FileSystemException
{
    public MalformedURI()
    {
    }
    
    public MalformedURI(String message)
    {
        super(message);
    }
    
    public MalformedURI(Throwable cause)
    {
        super(cause);
    }
    
    public MalformedURI(String message, Throwable cause)
    {
        super(message, cause);
    }
}

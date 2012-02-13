package mlos.ultcom.core;

/**
 * Thrown to indicate XML configuration error.
 * 
 * @author Marcin Los
 */
public class XMLConfigException extends Exception
{
    public XMLConfigException()
    {
    }

    public XMLConfigException(String message)
    {
        super(message);
    }

    public XMLConfigException(Throwable cause)
    {
        super(cause);
    }

    public XMLConfigException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

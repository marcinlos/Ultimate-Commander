package mlos.ultcom.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileFactory;
import mlos.ultcom.fs.FileSystemException;
import mlos.ultcom.fs.MalformedURI;

/**
 * Singleton class responsible for managing file system implementations used
 * by the application. 
 * 
 * @author Marcin Los
 */
public class FileSystemFactory
{
    private static FileSystemFactory instance;
    
    private Map<String, FileFactory> providers = 
        new HashMap<String, FileFactory>();
    
    private Iterator<FileFactory> factoryLoader = 
        ServiceLoader.load(FileFactory.class).iterator();
    
    /*
     * Private constructor to ensure Singleton invariant
     */
    private FileSystemFactory()
    {
    }
    
    /**
     * Auxilary method to ensure file separator is '/'
     */
    public static String convertSeparator(String path)
    {
        String sep = System.getProperty("file.separator");
        return path.replace(sep, "/");
    }
    
    /**
     * @return The One instance of {@code FileSystemFactory}
     */
    public static FileSystemFactory getInstance()
    {
        if (instance == null)
        {
            instance = new FileSystemFactory();
        }
        return instance;
    }
   
    /**
     * Creates {@code File} object using factory associated with passed
     * URI's scheme.
     * 
     * @param uri URI of a requested file
     * 
     * @return {@code File} object representing requested file
     * 
     * @throws FileSystemNotSupported if there is no implementation for
     * requested file system
     * 
     * @throws FileSystemException if {@code FileFactory.newInstance} throws
     */
    public File getElement(URI uri) throws FileSystemException
    {
        uri = ensureScheme(uri);
        String scheme = uri.getScheme();
        
        FileFactory factory = providers.get(scheme);
        if (factory == null)
        {
            // Move the iterator forward
            while (factoryLoader.hasNext())
            {
                factory = factoryLoader.next();
                providers.put(factory.getScheme(), factory);
                if (factory.getScheme().equals(scheme))
                {
                    break;
                }
            }
        }
        if (factory == null)
        {
            throw new FileSystemNotSupported(scheme);
        }
        return factory.newInstance(uri);
    }
    
    /**
     * Creates {@code File} object based on URI created from passed string.
     * 
     * @param path Path to create URI from
     * 
     * @return {@code File} object representing requested file
     * 
     * @throws MalformedURI if passed string is not a valid URI
     * 
     * @throws FileSystemNotSupported if there is no implementation for
     * requested file system
     * 
     * @throws FileSystemException if {@code FileFactory.newInstance} throws
     */
    public File getElement(String path) throws FileSystemException
    {
        URI uri = null;
        try
        {
            uri = new URI(path);
        }
        catch (URISyntaxException e)
        {
            throw new MalformedURI(e);
        }
        return getElement(uri);
    }
    
    /*
     * If uri has no schema, file schema is set.
     */
    private URI ensureScheme(URI uri)
    {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("") || scheme.length() == 1)
        {
            // It throws unchecked exceptions, but here it cannot fail,
            // I think
            return URI.create("file:/" + uri.toString());
        }
        else 
        {
            return uri;
        }
    }
}

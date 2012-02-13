package mlos.ultcom.fs;

import java.net.URI;

/**
 * Provides an interface for file system service. Implementations of this
 * interface are loaded using {@code ServiceLoader}. When resolving a path
 * is requested, an implementation which scheme obtained via {@code getScheme}
 * matches scheme of a requested URI is used to create an instance of a file.
 * 
 * <p>
 * In order to provide extensions to protocols supported by UltimateCommander
 * one needs to create .jar file as described in {@code ServiceLoader}'s 
 * documentation, and place it inside {@code ${app_dir}/plugins} directory.
 * 
 * @author Marcin Los
 */
public interface FileFactory
{
    /**
     * Tries to create concret {@code File} object based on path.
     * 
     * @param path Path used to create a {@code File}
     * 
     * @return {@code File} object defined by {@code path}
     * 
     * @throws FileSystemException if an error occured
     */
    File newInstance(URI path) throws FileSystemException;
    
    /**
     * @return Scheme of a protocol supported by this implementation.
     */
    String getScheme();
}

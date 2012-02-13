package mlos.ultcom.localfs;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileFactory;

/**
 * Concrete implementation of {@code FileFactory}, creates {@code 
 * LocalFile} objects from passed URIs.
 * 
 * @author Marcin Los
 */
public class LocalFileFactory implements FileFactory
{
    private static final Logger logger = 
        Logger.getLogger(LocalFileFactory.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public File newInstance(URI path)
    {
        Path file = null;
        try
        {
            file = Paths.get(path);
        }
        catch (Exception e)
        {
            logger.warn(e);
            return null;
        }
        if (Files.isDirectory(file))
        {
            return new LocalDirectory(file);
        }
        return new LocalFile(file);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme()
    {
        return "file";
    }
}

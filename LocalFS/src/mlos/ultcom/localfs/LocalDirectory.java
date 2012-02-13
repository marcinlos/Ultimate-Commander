package mlos.ultcom.localfs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileAccessException;
import mlos.ultcom.fs.FileSystemException;

/**
 * {@code Directory} implementation for local file system.
 * 
 * @author Marcin Los
 * 
 * @see LocalFile
 */
public class LocalDirectory extends LocalFile implements Directory
{
    // private static final Logger logger = Logger.getLogger(LocalDirectory.class);
    /**
     * Creates Directory by wrapping java.io.File object. It doesn't have to
     * exists. Still, it has to actually be a directory if exists. Otherwise, 
     * exception is thrown.
     *    
     * @param file java.nio.path.File object to wrap.
     * 
     * @throws InvalidArgumentException if {@code file} is not a directory.
     */
    public LocalDirectory(Path file)
    {
        super(file);
        if (Files.exists(file) && ! Files.isDirectory(file))
        {
            throw new IllegalArgumentException("Passed file is not a " +
                "directory");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<File> getFiles() throws FileAccessException, IOException
    {
        List<File> result = new ArrayList<File>();
        DirectoryStream<Path> directory = null;
        try
        {
            Path file = getUnderlyingFile();
            directory = Files.newDirectoryStream(file);
            for (Path child : directory)
            {
                result.add(Files.isDirectory(child) ? new LocalDirectory(child) :
                    new LocalFile(child));
            }
        }
        catch (SecurityException e)
        {
            throw new FileAccessException("Unable to access the content of " + 
                "a directory", e);
        }
        finally
        {
            if (directory != null)
            {
                directory.close();
            }
        }   
        return result;
    }
    
    @Override
    public File createChild(String name) throws FileSystemException, 
        IOException
    {
        Path file = getUnderlyingFile();
        try
        {
            return new LocalFile(file.resolve(Paths.get(name)));
        }
        catch (InvalidPathException e)
        {
            throw new FileSystemException(e);
        }
    }
    
    /**
     * Calculates directory's total size by recursively summing sizes of
     * its content.
     * 
     * @see mlos.ultcom.fs.File#getSize()
     * @see mlos.ultcom.fs.AbstractDirectory#getSize()
     */
    @Override
    public long getSize() throws FileAccessException, IOException
    {
        long size = 0;
        for (File f : this.getFiles())
        {
            size += f.getSize();
        }
        return size;
    }
}

package mlos.ultcom.localfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileAccessException;
import mlos.ultcom.fs.FileSystemException;
import mlos.ultcom.fs.OperationNotSupportedException;

/**
 * Concrete {@code File} implementation for files in local file system.
 * It's currently implemented using {@code java.nio.file package.
 * 
 * @author Marcin Los
 */
public class LocalFile implements File
{
    private Path file;
    
    /**
     * Creates LocalFile object by wrapping java.io.File
     * 
     * @param file {@code java.nio.file.Path} object representing a path
     * in local filesystem. 
     * 
     * @throws NullPointerException if {@code file} argument is null
     */
    public LocalFile(Path file)
    {
        if (file == null)
        {
            throw new NullPointerException("Passed file is null");
        }
        this.file = file;
    }
    
    /**
     * @return Value of underlying java.io.File's {@code getParentFile}
     * wrapped as a LocalDirectory.
     * 
     * @see mlos.ultcom.fs.File#getParent()
     */
    @Override
    public Directory getParent()
    {
        Path parent = file.getParent();
        return parent == null ? null : new LocalDirectory(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize() throws FileAccessException, IOException
    {
        return Files.size(file);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return file.getFileName().toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override 
    public String getPath()
    {
        return file.toUri().toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws FileAccessException
    {
        try
        {
            return Files.exists(file);
        }
        catch (SecurityException e)
        {
            throw new FileAccessException("Cannot check whether the file " +
                "exists", e);
        }
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void create() throws FileAccessException, IOException
    {
        try
        {
            Files.createFile(file);
        }
        catch (SecurityException e)
        {
            throw new FileAccessException("Cannot create file, access " +
                "denied", e);
        }
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public Directory createDirectory() throws FileSystemException, IOException
    {
        try
        {
            if (! Files.exists(file))
            {
                Files.createDirectory(file);
            }
            else if (! Files.isDirectory(file))
            {
                throw new FileSystemException("Cannot create directory " +
                    file.toString() + ", regular file with such path " +
                    "already exists");
            }
            return new LocalDirectory(file);
        }
        catch (SecurityException e)
        {
            throw new FileAccessException("Cannot create file, access " +
                "denied", e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() throws OperationNotSupportedException,
        FileAccessException, IOException
    {
        if (Files.isDirectory(file))
        {
            throw new OperationNotSupportedException("Cannot open input " +
                "stream of a directory");
        }
        /*if (Files.isReadable(file))
        {
            throw new FileAccessException("Cannot read file, access denied");
        }*/
        return Files.newInputStream(file);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream() throws OperationNotSupportedException,
        FileAccessException, IOException
    {
        if (Files.isDirectory(file))
        {
            throw new OperationNotSupportedException("Cannot open output " +
                "stream of a directory");
        }
        if (! Files.exists(file))
        {
            create();
        }
        
        if (! Files.isWritable(file))
        {
            throw new FileAccessException("Cannot read file, access denied");
        }
        return Files.newOutputStream(file);
    }

    /**
     * @return Underlying java.nio.file.File object.
     */
    protected Path getUnderlyingFile()
    {
        return file;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (! (o instanceof LocalFile))
        {
            return false;
        }
        LocalFile other = (LocalFile) o;
        return file.equals(other.file);
    }
    
    @Override
    public int hashCode()
    {
        return file.hashCode();
    }
}

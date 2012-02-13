package mlos.ultcom.ubercp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mlos.ultcom.command.Context;
import mlos.ultcom.command.LongCommand;
import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileAccessException;

/**
 * Implementation of a copy command using simple streams to transfer data.
 * 
 * @author Marcin Los
 */
public class Copy extends LongCommand
{    
    private long totalSize = 0;
    private long copied = 0;
    
    public Copy()
    {
        setDescription("Copying...");
    }
    
    /**
     * Copies files selected in active view to the directory of the inactive
     * view
     * 
     * {@inheritDoc}
     */
    @Override
    public void execute(Context active, Context inactive)
    {
        try
        {
            calculateSize(active);
            Directory dest = inactive.getCurrentDirectory();
            for (File file : active.getSelectedFiles())
            {
                copyRecursively(file, dest);
            }
            finished();
        }
        catch (Exception e)
        {
            failed(e);
        }
    }
    
    /**
     * Recursively copies the first argument (i.e. if it is a directory, 
     * all the content is copied as well) to the second directory.
     */
    private void copyRecursively(File src, Directory dest) throws Exception
    {
        if (src instanceof Directory)
        {
            Directory source = (Directory) src;
            Directory destination = 
                dest.createChild(source.getName()).createDirectory();
            
            for (File file : source.getFiles())
            {
                copyRecursively(file, destination);
            }
        }
        else
        {
            copySingleFile(src, dest);
        }
    }
    
    /**
     * Copies single file src to dest directory. {@code src} must be a regular
     * file.
     */
    private void copySingleFile(File src, Directory dest) throws Exception
    {
        setDescription("Copying " + src.getName());
        InputStream in = null;
        try
        {
            in = new BufferedInputStream(src.getInputStream());
            File outputFile = dest.createChild(src.getName());
            OutputStream out = null;
            try
            {
                out = new BufferedOutputStream(outputFile.getOutputStream());
                byte[] buffer = new byte[8192];
                int length = 0;
                while ((length = in.read(buffer)) > 0)
                {
                    out.write(buffer, 0, length);
                    copied += length;
                    setProgress((int)(100 * (((double) copied) / totalSize)));
                }
            }
            finally
            {
                if (out != null)
                {
                    out.close();
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }
    
    /**
     * Calculates total size of files to copy.
     */
    private void calculateSize(Context active)
    {
        try
        {
            for (File file : active.getSelectedFiles())
            {
                totalSize += file.getSize();
            } 
        }
        catch (FileAccessException e)
        {
            failed(e);
        } 
        catch (IOException e)
        {
            failed(e);
        }
    }
}

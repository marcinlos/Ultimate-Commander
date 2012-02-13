package mlos.ultcom.ubercp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mlos.ultcom.command.Context;
import mlos.ultcom.command.LongCommand;
import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileAccessException;

import org.apache.log4j.Logger;

/**
 * Implementation of GZIP compression command. Compresses files selected
 * in an active panel to file 'compressed.zip' in its directory.
 * 
 * @author Marcin Los
 */
public class ZIPCompression extends LongCommand
{
    private static final Logger logger = Logger.getLogger(ZIPCompression.class);
    
    private long totalSize = 0;
    private long copied = 0;
    private ZipOutputStream out;
    private Context context;
    
    public ZIPCompression()
    {
        setDescription("Compressing...");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context active, Context inactive)
    {
        context = active;
        try
        {
            calculateSize(active);
            openOutputStream();

            for (File file : active.getSelectedFiles())
            {
                zipRecursively(file, "");
            }
            finished();
        } 
        catch (Exception e)
        {
            failed(e);
        }
        finally
        {
            closeOutputStream();
        }
    }
    
    /**
     * Opens output stream
     */
    private void openOutputStream() throws Exception
    {
        Directory outputDirectory = context.getCurrentDirectory();
        File output = outputDirectory.createChild("compressed.zip");
        
        out = new ZipOutputStream(
            new BufferedOutputStream(output.getOutputStream()));
    }
    
    /**
     * Closes output stream
     */
    private void closeOutputStream()
    {
        try
        {
            if (out != null)
            {
                out.close();
            }
            
        }
        catch (IOException e)
        {
            logger.warn(e);
        }
    }
    
    /**
     * 
     */
    private void zipRecursively(File file, String prefix) throws Exception
    {
        if (file instanceof Directory)
        {
            Directory dir = (Directory) file;
            String newPrefix = prefix + '/' + dir.getName();
            for (File f : dir.getFiles())
            {
                zipRecursively(f, newPrefix);
            }
        }
        else
        {
            zipRegularFile(file, prefix);
        }
    }
    
    private void zipRegularFile(File file, String prefix) throws Exception
    {
        setDescription("Zipping " + file.getName());
        InputStream in = null;
        try
        {
            in = new BufferedInputStream(file.getInputStream());
            String name = prefix + '/' + file.getName();
            ZipEntry entry = new ZipEntry(name);
            out.putNextEntry(entry);

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

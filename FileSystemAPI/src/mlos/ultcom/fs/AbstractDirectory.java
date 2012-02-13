package mlos.ultcom.fs;

import java.io.IOException;

/**
 * Provides skeletal implementation of {@code Directory} interface, to
 * facilitate creation of concrete implementations. To create it, 
 * programer only needs to implement {@code getFiles()} method.
 * 
 * <p>
 * All in all, it's unlikely to be very useful, since in most cases it's
 * probably going to be more beneficial to inherit concrete {@code File}
 * implementation. Still, it's present for sake of completness, and as
 * a reference implementation.
 * 
 * @author Marcin Los
 */
public abstract class AbstractDirectory implements Directory
{
    /**
     * @see mlos.ultcom.fs.File#getSize()
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

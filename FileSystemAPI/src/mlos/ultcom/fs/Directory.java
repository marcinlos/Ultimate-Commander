package mlos.ultcom.fs;

import java.io.IOException;
import java.util.List;

/**
 * Interface representing directory in arbitrary tree-structured file system.
 * 
 * @author Marcin Los
 */
public interface Directory extends File
{
    /**
     * @return List of files in this directory, or {@code null} if it
     * doesn't exist. If the directory is empty, empty list is returned.
     * 
     * @throws FileAccessException if application has no read permission
     * for this directory.
     * 
     * @throws IOException if I/O error occured during listing the files
     */
    List<File> getFiles() throws FileAccessException, IOException;
    
    /**
     * Creates the file with a given name in this directory. 
     * <strong> THE FILE IS NOT PHYSICALLY CREATED!</strong>
     * Instead, to ensure existence of a file in a file system, {@code create}
     * should be called at return value of this method.
     * 
     * @param name Name of a file to create
     * 
     * @return File with a given name in directory represented by this object
     * 
     * @throws FileSystemException when error connected with file system occured
     * @throws IOException when I/O error occured
     */
    File createChild(String name) throws FileSystemException, IOException;
}

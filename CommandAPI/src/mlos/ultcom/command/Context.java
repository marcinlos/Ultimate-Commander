package mlos.ultcom.command;

import java.util.List;

import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;

/**
 * Interface for context objects used for passing necessery information
 * to commands. Its purpose is to decouple command API from GUI objects,
 * such as {@code FilePanel}, which contains all the information used
 * by commands.
 * 
 * @author Marcin Los
 */
public interface Context
{
    /**
     * @return List of files selected by the user
     */
    List<File> getSelectedFiles();
    
    /**
     * @return First file from the group of all selected files
     */
    File getFirstSelectedFile();
    
    /**
     * @return All the files visible for the user in the active panel
     */
    List<File> getFiles();
    
    /**
     * @return Current directory of an active panel
     */
    Directory getCurrentDirectory();
}

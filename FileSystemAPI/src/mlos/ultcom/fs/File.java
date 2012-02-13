package mlos.ultcom.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface representing file in arbitrary tree-structured file system. The
 * file may or may not exist; it makes it possible to use {@code File} objects
 * as parameters for commands creating actual files, like 'Copy' command.
 * 
 * @author Marcin Los
 */
public interface File
{
    /**
     * @return File's parent directory, or {@code null} if it has none.
     */
    Directory getParent();
    
    /**
     * @return File size in bytes (0 if it doesn't exist). For directories
     * total size of its content is returned.
     * 
     * @throws FileAccessException if application has no read permission
     * for the file
     * 
     * @throws IOException if I/O error occured during calculating file size
     */
    long getSize() throws FileAccessException, IOException;
    
    /**
     * @return Name of the file/directory represented by this object (i.e.
     * last element of the path)
     */
    String getName();
    
    /**
     * @return Full absolute path of the file
     */
    String getPath();
    
    /**
     * @return {@code true} if such file exists, {@code false} otherwise
     * 
     * @throws FileAccessException if determining the answer requires
     * read permissions the application doesn't have
     */
    boolean exists() throws FileAccessException;
    
    /**
     * Creates the file if it doesn't exist.
     * 
     * @throws FileAccessException if creating the file was not permitted
     * 
     * @throws IOException if an I/O error occured while creating the file
     */
    void create() throws FileAccessException, IOException;
    
    /**
     * Creates the directory represented by this path if it doesn't exist.
     * 
     * @return {@code Directory} object representing created directory 
     * 
     * @throws FileSystemException if file system did not allow to create it
     * 
     * @throws IOException if an I/O error occured while creating the directory 
     */
    Directory createDirectory() throws FileSystemException, IOException;
    
    /**
     * @return Input stream providing content of the file. There are no
     * requirements as for the exact type of the stream.
     * 
     * @throws FileAccessException if application has no read permission for
     * the file
     * 
     * @throws OperationNotSupportedException if this type of file does not
     * provide input stream
     * 
     * @throws IOException if problem occured during opening of a stream
     */
    InputStream getInputStream() throws OperationNotSupportedException,
        FileAccessException, IOException;
    
    /**
     * Method for obtaining {@code OutputStream} object to write to this
     * file. If specified path does not denote existing file, it should
     * be created.
     * 
     * @return Output stream associated with this {@code File}
     * 
     * @throws OperationNotSupportedException if this type of file does not
     * provide output stream
     * 
     * @throws FileAccessException if application has no write permission for
     * this file
     * 
     * @throws IOException if problem occured during opening of a stream
     */
    OutputStream getOutputStream() throws OperationNotSupportedException,
        FileAccessException, IOException;
}
 
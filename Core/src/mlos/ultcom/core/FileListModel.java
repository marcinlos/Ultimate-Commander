package mlos.ultcom.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import mlos.ultcom.command.Context;
import mlos.ultcom.command.FailureListener;
import mlos.ultcom.command.LongCommand;
import mlos.ultcom.command.ProgressEvent;
import mlos.ultcom.command.ProgressListener;
import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileAccessException;

/**
 * @author Marcin Los
 */
public class FileListModel extends AbstractTableModel
{
    private Directory directory;
    private Directory parent;
    private List<File> fileList = new ArrayList<File>();
    private Map<File, Long> dirSizeCache = new HashMap<File, Long>();
    private Set<CalculateSize> runningTasks = new HashSet<CalculateSize>();
    
    /** Denotes "Name" column */
    public static final int NAME = 0;
    
    /** Denotes "Type" column (extension) */
    public static final int TYPE = 1;
    
    /** Denotes "Size" column */
    public static final int SIZE = 2;
    
    /*
     * Internal command to calculate size of a directory. It is designed
     * to be run asynchronously by {@code CommandExecutor}.
     */
    private class CalculateSize extends LongCommand
    {
        private File file;
        private long size;
        
        public CalculateSize(File file)
        {
            this.file = file;
        }
        
        @Override
        public void execute(Context active, Context inactive)
        {  
            try
            {
                size = file.getSize();
                finished();
            }
            catch (Exception e)
            {
                failed(e);
            }
        }
        
        public long getSize()
        {
            return size;
        }
        
        public File getFile()
        {
            return file;
        }
    }
    
    /*
     * Progress listener for asynchronous directory size calculations,
     * only handles {@code finished} event.
     */
    private class CalculationListener implements ProgressListener
    {
        private FailureListener listener; 
        
        /*
         * Listener may well be {@code null}
         */
        public CalculationListener(FailureListener listener)
        {
            this.listener = listener;
        }
        
        @Override
        public void finished(Object source)
        {
            if (source instanceof CalculateSize)
            {
                CalculateSize task = (CalculateSize) source;
                updateSize(task.getFile(), task.getSize());
                runningTasks.remove(task);
            }
        }

        @Override
        public void progressChange(ProgressEvent e)
        {
            // Empty, we're not interested for now
        }
        
        @Override
        public void failed(Object source, Throwable cause)
        {
            if (listener != null)
            {
                listener.failed(source, cause);
            }
        }
    }
    
    /*
     * Updates passed file's size in cache and notifies model
     * listeners.
     */
    private void updateSize(File file, long size)
    {
        int i = fileList.indexOf(file);
        if (i != -1)
        {
            dirSizeCache.put(file, size);
            this.fireTableCellUpdated(i, SIZE);
        }
    }
    
    /**
     * Rebuilds the list of elements. Should be used when panel's
     * directory content changes.
     * 
     * @throws FileAccessException if application does not have permissions
     * required to fetch content of a directory
     * 
     * @throws IOException if I/O error occured
     */
    public void refreshContent() throws FileAccessException, IOException
    {
        if (directory != null)
        {
            parent = directory.getParent();
            List<File> newFileList = new ArrayList<File>();
            if (parent != null)
            {
                newFileList.add(parent);
            }
            newFileList.addAll(directory.getFiles());
            fileList = newFileList;
            // TODO maybe implement a cache...?
            dirSizeCache.clear();
            fireTableDataChanged();
        }
    }
    
    /**
     * @return Number of columns. So far, it's constant, and
     * equals 3 (name, type, size).
     */
    @Override
    public int getColumnCount()
    {
        return 3;
    }
    
    @Override 
    public String getColumnName(int column)
    {
        switch (column)
        {
        case NAME:
            return "Name";
        case TYPE:
            return "Ext.";
        case SIZE:
            return "Size";
        default:
            throw new IndexOutOfBoundsException("Invalid column number: " +
                column);
        }
    }

    /**
     * @return Number of rows. Right now it's simply number of elements
     * in a directory.
     */
    @Override
    public int getRowCount()
    {
        return fileList.size();
    }

    /**
     * The only noteworthy part is returning size of an element.
     * For files, it's simply value of {@code getSize()}. Computing
     * size of a directory is potentially expensive operation, so
     * it's not done unless explicitly requested by the user. Computed
     * values are cached.
     * 
     * @return Value of the requested field.
     * 
     * @throws IndexOutOfBoundsException if column or row number is 
     * invalid
     */
    @Override
    public Object getValueAt(int row, int column)
    {
        File file = fileList.get(row);
        switch (column)
        {
        case NAME:
            return getName(file);
            
        case TYPE:
            return getType(file);
            
        case SIZE:
            return getSize(file);
            
        default:
            throw new IndexOutOfBoundsException("Invalid " + 
                "column number");
        }
    }
    
    /**
     * Returns file name to display. Default implementation uses
     * {@code File.getName}, unless {@code file} is model's current
     * directory, in which case ".." is returned.
     * 
     * @param file File to extract name from
     * 
     * @return File name to display
     */
    protected String getName(File file)
    {
        // Parent is specially treated
        if (file.equals(parent))
        {
            return "..";
        }
        else
        {   
            return file.getName();
        }
    }
    
    /**
     * Returns extension/type of a file as a string to display. Default
     * implementation returns "&lt;dir&gt;" if the file is a directory, and
     * substring of file name beginning at the first character after the
     * last dot otherwise.
     * 
     * @param file File to extract type from
     * 
     * @return File type to display
     */
    protected String getType(File file)
    {
        if (file instanceof Directory)
        {
            return "<dir>";
        }
        else
        {
            String name = file.getName();
            // Clever stuff: 'lookaround'
            String regex = "(?<=\\.)\\w*$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(name);
            if (matcher.find())
            {
                return matcher.group();
            }
            // File has no extension
            else return "-";
        }
    }
    
    /**
     * Returns size of a file. Default implementation returns {@code 
     * File.getSize} in case of ordinary files. If the file is a directory,
     * method tries to find the cached size. If it is not found, {@code 0}
     * is returned. Size of a directory is not calculated until explicitly
     * requested by {@code calculateSize}.
     * 
     * @param file File to get size of
     * 
     * @return Size of the file
     */
    protected long getSize(File file)
    {
        if (file instanceof Directory)
        {
            // Check the cache
            Long size = dirSizeCache.get(file);
            return size == null ? 0 : size;
        }
        else 
        {
            try
            {
                return file.getSize();
            }
            // It doesn't seem to be a good solution, but on the other
            // hand I don't feel like throwing stuff from the inside of 
            // swing's belly.
            catch (Exception e)
            {
                return 0;
            }
        }
    }
    
    /**
     * Calculates the size of a directory at a given row, and stores it
     * in the cache. If element at given row is not a directory, nothing
     * happens.
     *  
     * @param row Index of a row in the table
     * 
     * @param listener Listener to notify about errors while calculating
     * the size
     * 
     * @throws IndexOutOfBoundsException if the row number is invalid
     */
    public void calculateSize(int row, FailureListener listener)
    {
        File file = fileList.get(row);
        if (file instanceof Directory)
        {
            CalculateSize command = new CalculateSize(file);
            CommandExecutor executor = CommandExecutor.getInstance();
            executor.execute(command, null, null, 
                new CalculationListener(listener));
            runningTasks.add(command);
        }
    }
    
    /**
     * Equivalent to {@code calculateSize(row, null)}.
     * 
     * @param row Index of a row in the table
     * 
     * @param listener Listener to notify about errors while calculating
     * the size
     * 
     * @throws IndexOutOfBoundsException if the row number is invalid
     * 
     * @see calculateSize(int, FailureListener)
     */
    public void calculateSize(int row)
    {
        calculateSize(row, null);
    }
    
    /**
     * @return Unmodifiable list of files
     */
    public List<File> getFileList()
    {
        return Collections.unmodifiableList(fileList);
    }
    
    /**
     * Sets {@code dir} as the model's current directory. {@code dir}
     * cannot be {@code null}.
     * 
     * @param dir New current directory of this panel
     * 
     * @throws FileAccessException if application does not have permissions
     * required to access content of a directory
     * 
     * @throws IOException if I/O error occured
     * 
     * @throws NullPointerException if {@code dir} is {@code null}
     */
    public void setDirectory(Directory dir) throws FileAccessException,
        IOException
    {
        if (dir == null)
        {
            throw new NullPointerException("Current directory cannot " +
                "be set to null");
        }
        directory = dir;
        refreshContent();
    }
    
    /**
     * @return Current directory
     */
    public Directory getDirectory()
    {
        return directory;
    }
    
    /**
     * @return Current directory's parent, or {@code null} if there is none.
     */
    public Directory getParentDirectory()
    {
        return directory.getParent();
    }
}
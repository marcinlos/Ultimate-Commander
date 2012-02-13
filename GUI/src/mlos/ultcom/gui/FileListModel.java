package mlos.ultcom.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileAccessException;

/**
 * @author Marcin Los
 *
 * Model of our table. Uses directory stored in {@code FilePanel}
 * as a source of elements.
 */
class FileListModel extends AbstractTableModel
{
    private Directory directory;
    private Directory parent;
    private List<File> fileList = new ArrayList<File>();
    private Map<File, Long> dirSizeCache = new HashMap<File, Long>();
    
    public static final int NAME = 0;
    public static final int TYPE = 1;
    public static final int SIZE = 2;
    
    /**
     * Rebuilds the list of elements. Should be used when panel's
     * directory changes.
     * 
     * @throws FileAccessException if application does not have permissions
     * required to fetch content of a directory
     * 
     * @throws IOException if I/O error occured
     */
    void refreshContent() throws FileAccessException, IOException
    {
        fileList.clear();
        dirSizeCache.clear();
        try
        {
            if (directory != null)
            {
                parent = directory.getParent();
                if (parent != null)
                {
                    fileList.add(parent);
                }
                fileList.addAll(directory.getFiles());
            }
        }
        finally
        {
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
            throw new IndexOutOfBoundsException("Invalid column number");
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
            if (file == parent)
            {
                return "..";
            }
            else
            {    
                return file.getName();
            }
            
        case TYPE:
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
                else return "-";
            }
        case SIZE:
            if (file instanceof Directory)
            {
                Long size = dirSizeCache.get(file);
                return size == null ? 0 : size;
            }
            else 
            {
                try
                {
                    return file.getSize();
                }
                catch (Exception e)
                {
                    return 0;
                }
            }
        default:
            throw new IndexOutOfBoundsException("Invalid " + 
                "column number");
        }
    }
    
    /**
     * Calculates the size of a directory at a given row, and stores it
     * in the cache. If element at given row is not a directory, nothing
     * happens.
     *  
     * @param row Index of a row in the table
     * 
     * @throws IndexOutOfBoundsException if the row number is invalid
     * 
     * @throws FileAccessException if access to file size data is 
     * not permitted
     * 
     * @throws IOException if I/O error occured during an attempt to 
     * calculate file size
     * 
     * TODO: It really should be parallelized...
     */
    public void calculateSize(int row) throws FileAccessException,
        IOException
    {
        File file = fileList.get(row);
        if (file instanceof Directory)
        {
            long size = file.getSize();
            dirSizeCache.put(file, size);
            fireTableCellUpdated(row, 2);
        }
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
     * required to fetch content of a directory
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
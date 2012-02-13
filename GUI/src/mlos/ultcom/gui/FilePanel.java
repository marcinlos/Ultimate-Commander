package mlos.ultcom.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.table.TableRowSorter;

import mlos.ultcom.command.Context;
import mlos.ultcom.command.FailureListener;
import mlos.ultcom.core.FileListModel;
import mlos.ultcom.core.FileSystemFactory;
import mlos.ultcom.fs.Directory;
import mlos.ultcom.fs.File;
import mlos.ultcom.fs.FileAccessException;
import mlos.ultcom.fs.FileSystemException;

import org.apache.log4j.Logger;

/**
 * Class representing single file panel. It contains control components
 * in the upper part, the main component is file list. It is backed by
 * {@code FileListModel} from {@code mlos.ultcom.core}.
 * 
 * @author Marcin Los
 */
public class FilePanel extends JPanel
{
    static final Logger logger = Logger.getLogger(FilePanel.class);
    
    private JPanel topPanel;
    private JTextField location;
    private JTextField pattern;
    private FileListModel model;
    private JTable table;
    private JPanel panel;
    private TableRowSorter<FileListModel> filter;
    
    /*
     * Implementation of failure listener showing dialog with error
     * message. Used as a failure handler for calculating directory
     * size.
     */
    private FailureListener failureListener = new FailureListener()
    {
        @Override
        public void failed(Object source, Throwable cause)
        { 
            JOptionPane.showMessageDialog(FilePanel.this, 
                cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);            
        }  
    };
    
    /*
     * Implementation of mouse listener for manipulating file list
     * data.
     */
    private class TableMouseListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2)
            {
                File file = getSelectedFile();
                if (file != null)
                {
                    openElement(file);
                }
            }
        }
    }
    
    /*
     * Action for handling enter in location text file.
     */
    private class MoveToLocation implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String uri = location.getText();
            setDirectory(uri);
        }
    }
   
    /*
     * Action for recalclating size of selected directory.
     */
    private class RecalculateSize extends AbstractAction
    {
        public RecalculateSize()
        {
            super("Calculate size");
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            int[] selection = table.getSelectedRows();
            for (int i : selection)
            {
                recalculateSize(i);
            }
        }
    }
    
    /*
     * Action for changing current directory to one currently selected.
     */
    private class OpenElement extends AbstractAction
    {
        public OpenElement()
        {
            super("Open element");
        }
        
        @Override
        public void actionPerformed(ActionEvent evt)
        {
            File file = getSelectedFile();
            if (file != null)
            {
                openElement(file);
            }
        }
    }
    
    /*
     * Action for changing current directory to it's parent (if one
     * exists).
     */
    private class GoToParentDir extends AbstractAction
    {
        public GoToParentDir()
        {
            super("Go to parent dir");
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            goToParentDirectory();
        }
    }
    
    /*
     * The One implementation of {@code mlos.ultcom.command.Context}
     * interface, passed to invoked commands. Instances can be obtained
     * through {@code FilePanel.getContext()} method.
     */
    private class CommandContext implements Context
    {
        private Directory directory;
        private List<File> allFiles;
        private List<File> selectedFiles;
        private File firstSelected;
        
        // We need a deep copy of all the lists because of long-running
        // commands
        public CommandContext()
        {
            directory = model.getDirectory();
            allFiles = new ArrayList<File>(model.getFileList());
            selectedFiles = FilePanel.this.getSelectedFiles();
            
            int index = table.getSelectedRow();
            if (index != -1)
            {
                index = table.convertRowIndexToModel(index);
                firstSelected = model.getFileList().get(index);
            }
        }
        
        @Override
        public Directory getCurrentDirectory()
        {
            return directory;
        }

        @Override
        public List<File> getFiles()
        {
            return allFiles;
        }

        @Override
        public File getFirstSelectedFile()
        {
            return firstSelected;
        }

        @Override
        public List<File> getSelectedFiles()
        {
            return selectedFiles;
        }
    }
    
    
    /**
     * Creates all the GUI elements. Doesn't set any initial directory.
     */
    public FilePanel()
    {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 275));

        setupTopPanel();
        setupTablePanel();
        setupTableFilter();
        setupActions();
        
        location.setFocusTraversalKeysEnabled(false);
        pattern.setFocusTraversalKeysEnabled(false);
        
        // Make sure location text file gets the focus
        addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            { 
                table.requestFocusInWindow();
            }
        });
    }
    
    /**
     * Creates top panel with its content and layout.
     */
    private void setupTopPanel()
    {
        topPanel = new JPanel();
        add(topPanel, BorderLayout.PAGE_START);   
        
        // Top panel with two text fields for location and filter pattern
        // is controlled with SpringLayout, to keep width ratio fixed
        // (more or less, it's kinda buggy)
        SpringLayout layout = new SpringLayout();
        location = new JTextField(10);
        topPanel.add(location);
        pattern = new JTextField(10);
        topPanel.add(pattern);

        // Set location field's width to about 0.8 of width (- padding)
        SpringLayout.Constraints cons = layout.getConstraints(location);
        Spring width = layout.getConstraint(SpringLayout.WIDTH, topPanel);
        cons.setWidth(Spring.sum(Spring.scale(width, 0.8f), 
            Spring.constant(-5)));
        
        // Padding
        cons.setX(Spring.constant(3));
        cons.setY(Spring.constant(3));
        
        // Bind pattern's field to location's field
        layout.putConstraint(SpringLayout.WEST, pattern, 3, SpringLayout.EAST,
            location);
        
        // Set width as 20% of panel's width
        cons = layout.getConstraints(pattern);
        cons.setWidth(Spring.sum(Spring.scale(width, 0.2f), 
            Spring.constant(-4)));
        
        // Padding
        cons.setY(Spring.constant(3));      
        
        // Set panel's height. Sad necessity in SpringLayout.
        Spring height = layout.getConstraints(location).getHeight();
        layout.getConstraints(topPanel).setHeight(Spring.sum(height, 
            Spring.constant(6)));
        
        topPanel.setLayout(layout);
    }
    
    /**
     * Handles creaction and registration of actions used by the panel.
     */
    private void setupActions()
    {
        location.addActionListener(new MoveToLocation());
        
        InputMap inputMap = table.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "recalculateSize");
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "openElement");
        inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), "goToParentDir");
        
        InputMap tableDefaults = table.getInputMap(
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tableDefaults.put(KeyStroke.getKeyStroke("TAB"), "");
        tableDefaults.put(KeyStroke.getKeyStroke("shift TAB"), "");
        tableDefaults.put(KeyStroke.getKeyStroke("UP"), 
            "selectPreviousRowChangeLead");
        tableDefaults.put(KeyStroke.getKeyStroke("DOWN"), 
            "selectNextRowChangeLead");
        tableDefaults.put(KeyStroke.getKeyStroke("LEFT"), "");
        tableDefaults.put(KeyStroke.getKeyStroke("RIGHT"), "");
        tableDefaults.put(KeyStroke.getKeyStroke("INSERT"), "toggleAndAnchor");
        
        ActionMap actionMap = table.getActionMap();
        actionMap.put("recalculateSize", new RecalculateSize());
        actionMap.put("openElement", new OpenElement());
        actionMap.put("goToParentDir", new GoToParentDir());
        
        table.addMouseListener(new TableMouseListener());
    }
    
    /**
     * Handles creation of table panel with its content.
     */
    private void setupTablePanel()
    {
        panel = new JPanel();
        add(panel, BorderLayout.CENTER);
                
        model = new FileListModel();
        table = new JTable(model);
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(table));  
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
    }
    
    /**
     * Handles setting up a row filter
     */
    private void setupTableFilter()
    {
        pattern.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {   
                createFilter();
            }
        });
        filter = new TableRowSorter<FileListModel>(model);
        table.setRowSorter(filter);
    }
    
    /**
     * Creates row filter based on content of pattern text field
     */
    private void createFilter()
    {
        RowFilter<FileListModel, Integer> rowFilter = null;
        try
        {
            rowFilter = RowFilter.regexFilter(pattern.getText(), 0);
        }
        catch (PatternSyntaxException e)
        {
            JOptionPane.showMessageDialog(null, "Specified pattern is not " +
                "a valid regular expression", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        filter.setRowFilter(rowFilter);
    }
    
    /**
     * Sets the panel's current directory to {@code directory}
     * 
     * @param directory New current directory
     */
    public void setDirectory(Directory directory)
    {
        try
        {
            model.setDirectory(directory);
            location.setText(directory.getPath());
        }
        catch (FileAccessException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * @return Panel's current directory
     */
    public Directory getDirectory()
    {
        return model.getDirectory();
    }
    
    /**
     * Sets the panel's current directory to the one obtained from {@code 
     * FileSystemFactory} from the given URI in string form.
     * 
     * @param path Path of a directory
     */
    public void setDirectory(String path)
    {
        FileSystemFactory factory = FileSystemFactory.getInstance();
        File file = null;
        try
        {
            file = factory.getElement(path);   
            if (file instanceof Directory)
            {
                setDirectory((Directory) file);
            } 
            else
            {
                JOptionPane.showMessageDialog(null, "Cannot load following " +
                    "path:\n" + path, "Error", JOptionPane.ERROR_MESSAGE); 
            }
        }
        catch (FileSystemException e)
        {
            JOptionPane.showMessageDialog(null, "Cannot load file from " +
                "location: " + path, "Error", JOptionPane.ERROR_MESSAGE);
            logger.error(e);
        }
    }
    
    /**
     * Changes panel's directory to a parent of current directory if it exists.
     * @return {@code true} if the parent directory exists, otherwise 
     * {@code false}.
     */
    public boolean goToParentDirectory()
    {
        Directory parent = model.getParentDirectory();
        if (parent != null)
        {
            setDirectory(parent);
            return true;
        }
        return false;
    }
    
    /**
     * Forces recalculation of size of the file in a given row.
     * 
     * @param row Index of row to be updated
     * 
     * @throws IndexOutOfBoundsException if the row number is invalid
     */
    public void recalculateSize(int row)
    {
        int actualRow = table.convertRowIndexToModel(row);
        model.calculateSize(actualRow, failureListener);
    }
    
    /**
     * Performs an action on passed element: if it's a directory, then it is
     * set as the panel's current directory, and if it's a regular file,
     * it is opened.
     * 
     * @param file Element to open
     */
    public void openElement(File file)
    {
        if (file instanceof Directory)
        {
            setDirectory((Directory) file);
        }
        // TODO Actually open it
    }
    
    /**
     * @return List of selected {@code File} objects
     */
    public List<File> getSelectedFiles()
    {
        List<File> selected = new ArrayList<File>();
        for (int i : table.getSelectedRows())
        {
            int index = table.convertRowIndexToModel(i);
            selected.add(model.getFileList().get(index));
        }
        return selected;
    }
    
    /**
     * @return First selected file
     */
    public File getSelectedFile()
    {
        ListSelectionModel selection = table.getSelectionModel();
        int i = selection.getLeadSelectionIndex();
        i = table.convertRowIndexToModel(i);        
        return model.getFileList().get(i);
    }
    
    /**
     * @return {@code Context} object associated with this panel.
     * 
     * @see mlos.ultcom.command.Context
     */
    public Context getContext()
    {
        return new CommandContext();
    }
}

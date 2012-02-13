package mlos.ultcom.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mlos.ultcom.command.Command;
import mlos.ultcom.command.Context;
import mlos.ultcom.command.LongCommand;
import mlos.ultcom.core.ApplicationInterface;
import mlos.ultcom.core.CommandData;
import mlos.ultcom.core.CommandExecutor;
import mlos.ultcom.core.ConfigException;
import mlos.ultcom.core.FileSystemFactory;
import mlos.ultcom.core.GUIBuilder;
import mlos.ultcom.core.XMLProperties;
import mlos.ultcom.fs.Directory;

import org.apache.log4j.Logger;

/**
 * Main GUI class, representing top-level application window.
 * 
 * @author Marcin Los
 */
public class ApplicationWindow extends JFrame implements ApplicationInterface
{    
    private static final Logger logger = 
        Logger.getLogger(ApplicationWindow.class);
    
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JSplitPane mainPanel;
    private FilePanel[] panels = new FilePanel[2];
    private FocusTracker[] trackers = new FocusTracker[2];
    private int activePanel = LEFT_PANEL;
    private ProgressWindow progressWindow;
    
    private XMLProperties properties;
    
    /** Denotes the left file panel */
    public static final int LEFT_PANEL = 0;
    
    /** Denotes the right file panel */
    public static final int RIGHT_PANEL = 1;
    
    /** Path to GUI configuration file */
    private static final String CONFIG_PATH = "resources/config_gui.xml";
    
    private Map<String, Action> actions = new HashMap<String, Action>();  
    private Map<String, JMenu> menus = new HashMap<String, JMenu>();
    
    private Map<Object, UIManager.LookAndFeelInfo> lookAndFeelMenu =
        new HashMap<Object, UIManager.LookAndFeelInfo>(); 
    
    public ApplicationWindow()
    {
        super("Ultimate Commander 1.0");      
        setupInterface();
        setupActions();
        setupDefaultMenus();
        setupDirectories();
    }
    
    /*
     * Creates main components of the window
     */
    private void setupInterface()
    {
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        setLayout(new BorderLayout());
        
        toolBar = new JToolBar();
        toolBar.setFocusTraversalKeysEnabled(false);
        add(toolBar, BorderLayout.PAGE_START);
        
        panels[0] = new FilePanel();
        panels[1] = new FilePanel();
        
        trackers[0] = new PanelFocusTracker(panels[0]);
        trackers[1] = new PanelFocusTracker(panels[1]);
        
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            panels[0], panels[1]);
        mainPanel.setDividerSize(5);
        mainPanel.setResizeWeight(0.5);
        add(mainPanel, BorderLayout.CENTER);
        
        addWindowListener(new CloseListener());
        
        progressWindow = new ProgressWindow();
    }
    
    /*
     * Defines and registers actions
     */
    private void setupActions()
    {
        JPanel content = (JPanel)getContentPane();
        InputMap inputMap = content.getInputMap(
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("TAB"), "moveFocus");
        
        ActionMap actionMap = content.getActionMap();
        actionMap.put("moveFocus", new SwitchFocus());
    }
    
    /*
     * Sets initial directories for both panels
     */
    private void setupDirectories()
    {
        properties = new XMLProperties();
        try
        {
            properties.readFromFile(CONFIG_PATH);
        }
        catch (ConfigException e)
        {
            JOptionPane.showMessageDialog(null, "Cannot read configuration " +
                "file; default setting shall be used", "Problem", 
                JOptionPane.WARNING_MESSAGE);
            logger.warn(e);
        }
        
        String current = System.getProperty("user.dir");
        
        String leftPath = properties.getProperty("dir.path.left", current);
        leftPath = FileSystemFactory.convertSeparator(leftPath);
        String rightPath = properties.getProperty("dir.path.right", current);
        rightPath = FileSystemFactory.convertSeparator(rightPath);
        
        panels[LEFT_PANEL].setDirectory(leftPath);
        panels[RIGHT_PANEL].setDirectory(rightPath);
    }
    
    /*
     * Switches the focus between two file panels.
     */
    private class SwitchFocus extends AbstractAction
    {
        public SwitchFocus()
        {
            super("Switch focus");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            activePanel = 1 - activePanel;
            panels[activePanel].requestFocusInWindow();
        }
    }
    
    /*
     * Simple implementation of {@code FocusTracker} to keep track of an
     * active panel.
     */
    private class PanelFocusTracker extends FocusTracker
    {
        public PanelFocusTracker(Component component)
        {
            super(component);
        }

        @Override
        public void gainedFocus()
        {
            if (getComponent() == panels[LEFT_PANEL])
            {
                activePanel = LEFT_PANEL;
            }
            else
            {
                activePanel = RIGHT_PANEL;
            }
        }

        @Override
        public void lostFocus()
        {     
            // Not interested for now
        }
    }
    
    /*
     * Implementation of window listener to allow custom close response
     */
    private class CloseListener extends WindowAdapter
    {
        @Override
        public void windowClosing(WindowEvent e)
        {
            exit();
        }
    }
    
    /*
     * "Destructor"
     */
    private void exit()
    {
        logger.debug("The application is shutting down");
        try
        {
            saveDirectories();
            properties.storeToFile(CONFIG_PATH);
        }
        catch (ConfigException e)
        {
            logger.error("Problem while saving GUI configuration", e);
        }
        dispose();
        progressWindow.dispose();
        CommandExecutor executor = CommandExecutor.getInstance();
        executor.shutdown();
    }
    
    /*
     * Saves current directories to properties object
     */
    private void saveDirectories()
    {
        Directory dir = panels[LEFT_PANEL].getDirectory();
        properties.setProperty("dir.path.left", dir.getPath());
        dir = panels[RIGHT_PANEL].getDirectory();
        properties.setProperty("dir.path.right", dir.getPath());
    }
    
    /*
     * Creates default menus, independent of menu definitions in
     * configuraton files.
     */
    private void setupDefaultMenus()
    {      
        setupLookAndFeelMenu();
    }
    
    /*
     * Creates Look &amp; Feel submenu
     */
    private void setupLookAndFeelMenu()
    {
        JMenu menu = getMenu("View/Look & feel");
        
        ButtonGroup group = new ButtonGroup();
        
        // List available look & feels
        UIManager.LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
        String current = UIManager.getLookAndFeel().getName();
        
        // Common action listener
        ActionListener listener = new LookAndFeelChanger();
        
        for (UIManager.LookAndFeelInfo lf : lfs)
        {
            String name = lf.getName();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
            group.add(item);
            menu.add(item);
            lookAndFeelMenu.put(item, lf);
            item.addActionListener(listener);
            
            if (name.equals(current))
            {
                item.setSelected(true);
            }
        }
    }
    
    /*
     * Listener for Look &amp; Feel menu
     */
    private class LookAndFeelChanger implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            // L&F info was stored with menu item as a key
            String className = 
                lookAndFeelMenu.get(e.getSource()).getClassName();
            setLookAndFeel(className);
        }
    }
    
    
    /*
     * Sets look and feel specified by the string. In case of problems,
     * logs and shows appropriate error messages.
     */
    private void setLookAndFeel(String lookAndFeel)
    {
        try
        {
            UIManager.setLookAndFeel(lookAndFeel);
            SwingUtilities.updateComponentTreeUI(this);
            SwingUtilities.updateComponentTreeUI(progressWindow);
            // pack();
        } 
        catch (UnsupportedLookAndFeelException e)
        {
            String message = "Look and feel not supported: " + lookAndFeel;
            logger.error(message, e);
            JOptionPane.showMessageDialog(this, message, "L&F error",
                JOptionPane.ERROR_MESSAGE);
        } 
        catch (ClassNotFoundException e)
        {
            String message = "Cannot load class for specified look " +
            "and feel: " + lookAndFeel;
            logger.error(message, e);
            JOptionPane.showMessageDialog(this, message, "L&F error",
                JOptionPane.ERROR_MESSAGE);
        } 
        // No need for custom messages for instantiation and access problems,
        // user doesn't need to know, and it's logged anyway.
        catch (Exception e)
        {
            String message = "Cannot use specified look and feel: " + 
                lookAndFeel + "\nSee logs for details" ;
            logger.error(message, e);
            JOptionPane.showMessageDialog(this, message, "L&F error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * @return Context object of an active panel
     */
    private Context getActiveContextObject()
    {
        return panels[activePanel].getContext();
    }
    
    /**
     * @return Context object of an inactive panel
     */
    private Context getInactiveContextObject()
    {
        return panels[1 - activePanel].getContext();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GUIBuilder getGUIBuilder()
    {
        return new WindowGUIBuilder();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start()
    {        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        pack();
        setVisible(true);
    }
    
    /*
     * Method returning menu of a specified name. Name is in the following
     * format:
     * {@code [parent/]..[parent/]name
     * 
     * Menu is created as a child of its immediate parent if it does 
     * not exist.
     */
    private JMenu getMenu(String menuName)
    {
        JMenu menu = menus.get(menuName);
        if (menu == null)
        {
            menu = new JMenu(menuName);
            
            int index = menuName.lastIndexOf('/');
            if (index != -1)
            {
                String parentName = menuName.substring(0, index);
                JMenu parentMenu = getMenu(parentName);           
                parentMenu.add(menu);
            }
            else
            {
                menuBar.add(menu);
            }
            menus.put(menuName, menu);
        }
        return menu;
    }
    
    /*
     * Implementation of XML config parsing listener. Creates actions
     * for each parsed command and builds menu and toolbar items on the 
     * fly.
     */
    private class WindowGUIBuilder implements GUIBuilder
    {
        @Override
        public void registerCommand(CommandData commandData)
        {
            logger.debug("Registered command: " + commandData.getId());
            actions.put(commandData.getId(), new CommandAction(commandData));
        }
        
        @Override
        public void addMenu(String name, List<String> commandList)
        {
            logger.debug("Menu: " + name);
            JMenu menu = getMenu(name);
            for (String id : commandList)
            {
                logger.debug("item: " + id);
                Action action = actions.get(id);
                if (action == null)
                {
                    logger.error("Command not found: " + id);
                }
                else
                {
                    menu.add(action);
                }
            }
        }
        
        @Override
        public void addToolbarItems(List<String> commandList)
        {
            logger.debug("Toolbar:");
            for (String id : commandList)
            {
                logger.debug("item: " + id);
                Action action = actions.get(id);
                if (action == null)
                {
                    logger.error("Command not found: " + id);
                }
                else
                {
                    toolBar.add(action);
                }
            }
        }
         
        @Override
        public Icon createIcon(String location)
        {
            ClassLoader sys = ClassLoader.getSystemClassLoader();
            URL url = sys.getResource(location);
            if (url != null)
            {
                return new ImageIcon(url);
            }
            return null;
        }
    }
    
    /*
     * General menu and toolbar action listener, executing associated
     * commands.
     */
    private class CommandAction extends AbstractAction
    {
        private CommandData data;
        
        public CommandAction(CommandData data)
        {
            super(data.getName(), data.getIcon());
            putValue(SHORT_DESCRIPTION, data.getHint());
            putValue(ACCELERATOR_KEY, data.getKey());
            this.data = data;
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            // Commands should be executed by CommandExecutor
            CommandExecutor executor = CommandExecutor.getInstance();
            Command command = executor.createHandler(data.getHandler());
            
            boolean longRunning = executor.execute(command, 
                getActiveContextObject(), getInactiveContextObject(),
                progressWindow.getListener());
            
            // Add to progress-tracking window if necessary
            if (longRunning)
            {
                progressWindow.addOperation((LongCommand) command);
                progressWindow.setVisible(true);
            }
        }
    } 
}

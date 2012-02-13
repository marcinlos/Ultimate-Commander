package mlos.ultcom.core;

import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

/**
 * Class initializing all the application's crucial components. In particular,
 * it reads configuration file and creates GUI object.
 * 
 * @author Marcin Los
 * 
 * @see ApplicationInterface
 */
public class ApplicationLoader
{
    private static final Logger logger = 
        Logger.getLogger(ApplicationLoader.class);
    
    private static ApplicationLoader instance;
    
    private XMLProperties properties;
    
    public static final String COMMANDS_FILE = "resources/commands.xml";
    public static final String CONFIG_FILE = "resources/config_core.xml";
    
    /**
     * Private constructor - singleton
     */
    private ApplicationLoader()
    {
    }
    
    /**
     * @return instance of {@code ApplicationLoader}
     */
    public static ApplicationLoader getInstance()
    {
        if (instance == null)
        {
            instance = new ApplicationLoader();
        }
        return instance;
    }
    
    /**
     * @return unmodifiable map of properties
     */
    public Properties getProperties()
    {
        return properties;
    }
    
    /*
     * Method performing actual loading, invoked later in swing's event 
     * dispatch thread to let {@code ApplicationInterface} instance
     * safely build the gui.
     */
    private void start() throws Exception
    {
        // Parse main configuration file
        parseConfig();
        
        // Set initial look & feel to system one
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        // Load the app's interface class
        ApplicationInterface application = getInterface();
    
        // Load commands
        loadCommands(application);
        
        // Start the application
        application.start();
    }
    
    /**
     * Parses main configuration file
     */
    private void parseConfig()
    {
        properties = new XMLProperties();        
        try
        {
            properties.readFromFile(CONFIG_FILE);
        }
        catch (ConfigException e)
        {
            logger.warn("Cannot parse main configuration file", e);
        }
    }
    
    /**
     * Loads main GUI class
     */
    private ApplicationInterface getInterface() throws Exception
    {
        String className = getAppInterfaceName();
        logger.debug("Loading GUI implementation: " + className);
        
        Class<?> clazz = Class.forName(className);
        ApplicationInterface application = 
            (ApplicationInterface)clazz.newInstance();
       
        logger.debug("GUI implementation loaded");
        return application;
    }
    
    /**
     * @return Name of class to be used as an application interface
     * 
     * @throws ConfigException when name is not present in configuration
     */
    private String getAppInterfaceName() throws ConfigException
    {
        String className = properties.getProperty("application.interface");
        if (className == null)
        {
            throw new ConfigException("Cannot load name of application " + 
                "interface class from config file");
        }
        return className;
    }
    
    /**
     * Reads configuration file with commands
     */
    private void loadCommands(ApplicationInterface application) 
        throws ConfigException
    {
        logger.debug("Parsing config file: " + COMMANDS_FILE);
        XMLConfigReader reader = new XMLConfigReader();
        reader.readCommands(COMMANDS_FILE, application.getGUIBuilder());
        logger.debug("Config file parsed");
    }
    
    /**
     * Executes private method {@code start()} in Swing's Event Dispatch
     * Thread. Logs all the exceptions thrown by it.
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {   
                try
                {    
                    getInstance().start();
                }
                catch (Exception e) 
                {
                    logger.error("Critical error during application loading", 
                        e);
                }
            }
        });
    }

}

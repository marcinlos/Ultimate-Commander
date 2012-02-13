package mlos.ultcom.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import mlos.ultcom.command.Command;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class responsible for parsing XML file containing commands. Communication
 * with the rest of an application is realized by {@code GUIBuilder}
 * interface, which is notified about parsed elements.
 *
 * <p>
 * Passed {@code location} strings are interpreted as an absolute path,
 * file is searched in the JVM's classpath. XML configuration file is 
 * loaded using system classloader's {@code getResourceAsStream}, for
 * the details of its behaviour see 
 * {@link ClassLoader#getResourceAsStream(String)} 
 * 
 * @author Marcin Los
 * 
 * @see GUIBuilder
 */
public class XMLConfigReader
{
    private static final Logger logger = 
        Logger.getLogger(XMLConfigReader.class);
    
    private Map<String, CommandData> commandMap;
    
    public static final String SCHEMA_FILE = "resources/commands.xsd";
    
    /**
     * Parses the configuration XML file from passed {@code location}, and
     * creates map of available commands. File is validated using {@code
     * commands.xsd} as found by the system classloader.
     * 
     * <p>
     * {@code builder} is an example of an Observer pattern. It enables
     * external objects to recevie notifications about parsing events.
     * Its original purpose is to build the menu during parsing, hence
     * the name of a type {@code GUIBuilder}.
     *  
     * @param location Path of a configuration XML file
     * 
     * @param builder Receives notifications when some parts of a config
     * are parsed
     * 
     * @throws ConfigException if some error occured during reading
     */
    public void readCommands(String location, GUIBuilder builder)
        throws ConfigException
    {
        commandMap = new HashMap<String, CommandData>();       
        ClassLoader sys = ClassLoader.getSystemClassLoader();
        // Surprisingly, this doesn't throw any fancy exceptions
        InputStream fileStream = sys.getResourceAsStream(location);
        
        if (fileStream == null)
        {
            throw new ConfigException(String.format("Failed to load " +
                "configuration file: %s", location));
        }       
        try
        {
            readCommandsFromInputStream(fileStream, builder);
        }
        finally 
        {
            try
            {
                fileStream.close();
            }
            catch (IOException e)
            {
                logger.error("Exception thrown while closing the stream", e);
            }
        }
    }
    
    /*
     * Auxilary function reading configuration from passed input stream.
     * Just to keep {@code readCommands} reasonably short.
     */
    private void readCommandsFromInputStream(InputStream file, 
        GUIBuilder builder) throws ConfigException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false); // it's about DTD validation only!
            factory.setNamespaceAware(true);
    
            ClassLoader sys = ClassLoader.getSystemClassLoader();
            SchemaFactory schemaFactory = 
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            URL schemaFile = sys.getResource(SCHEMA_FILE);
            if (schemaFile == null)
            {
                throw new ConfigException("Cannot load schema file");
            }
            Schema schema = schemaFactory.newSchema(schemaFile);  
            factory.setSchema(schema);
            SAXParser parser = factory.newSAXParser();
            parser.parse(file, new TopLevelHandler(builder));
        }
        catch (SAXException e)
        {
            throw new ConfigException(e);
        }
        catch (ParserConfigurationException e)
        {
            throw new ConfigException(e);
        }
        catch (IOException e)
        {
            throw new ConfigException(e);
        }
    }
    
    /*
     * Handler logging problems using class' logger.
     */
    private class LoggingHandler extends DefaultHandler
    {
        @Override
        public void warning(SAXParseException e) throws SAXException
        {
            logger.warn(e);
            throw new SAXException(e);
        }
        
        @Override
        public void error(SAXParseException e) throws SAXException
        {
            logger.error(e);
            throw new SAXException(e);
        }
        
        @Override
        public void fatalError(SAXParseException e) throws SAXException
        {
            logger.error(e);
            throw new SAXException(e);
        }
    }
    
    /*
     * Top level handler. In order to keep it reasonably readable, the actual
     * logic is contained in helper handlers below. Events other than start of
     * an element are entirely delegated to these handlers.
     */
    private class TopLevelHandler extends LoggingHandler
    {
        private GUIBuilder builder;
        private DefaultHandler handler;
        
        public TopLevelHandler(GUIBuilder builder)
        {
            this.builder = builder;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
        {
            // We check if it's an opening tag of a major part of a config,
            // if so we create one of the helper handlers.
            if (qName.equals("commands"))
            {
                handler = new CommandReaderHandler(builder);
            }
            else if (qName.equals("menu"))
            {
                handler = new MenuBarReaderHandler(builder);
            }
            else if (qName.equals("toolbar"))
            {
                handler = new ToolbarReaderHandler(builder);
            }
            // Delegate to this.handler
            if (handler != null)
            {
                handler.startElement(uri, localName, qName, attributes);
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            // Delegate to this.handler
            if (handler != null)
            {
                handler.endElement(uri, localName, qName);
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws 
            SAXException
        {
            // Delegate to this.handler
            if (handler != null)
            {
                handler.characters(ch, start, length);
            }
        }
    }
    
    /*
     * Handler for parsing part of XML file containing command definitions.
     * Most complicated of the three, as it contains the logic of loading
     * command handlers.
     */
    private class CommandReaderHandler extends LoggingHandler
    {
        private GUIBuilder builder;
        private CommandData commandData;
        private String field;
        
        public CommandReaderHandler(GUIBuilder builder)
        {
            this.builder = builder; 
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
            Attributes attributes)
        {
            if (qName.equals("command"))
            {
                commandData = new CommandData();
                commandData.setId(attributes.getValue("id"));
            }
            else
            {
                field = qName;
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
        {
            if (qName.equals("command"))
            {
                commandMap.put(commandData.getId(), commandData);
                builder.registerCommand(commandData);
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws 
            SAXException
        {
            String value = new String(ch, start, length).trim();
            if (commandData != null && field != null)
            {
                if (field.equals("name"))
                {
                    commandData.setName(value);
                }
                else if (field.equals("handler"))
                {
                    // Throws if fails, perhaps it shouldn't be treated as a 
                    // fatal error, but oh well...
                    Class<? extends Command> handlerClass = 
                        getHandlerForName(value);
                    commandData.setHandler(handlerClass);
                }
                else if (field.equals("key"))
                {
                    KeyStroke key = KeyStroke.getKeyStroke(value);
                    // Just warning, I wouldn't call it 'critical error'...
                    if (key == null)
                    {
                        logger.warn(String.format("Invalid key specified " +
                            "for '%s' command", commandData.getId()));
                    }
                    commandData.setKey(key);
                }
                else if (field.equals("icon"))
                {
                    Icon icon = builder.createIcon(value);
                    // Just warning, I wouldn't call it 'critical error'...
                    if (icon == null)
                    {
                        logger.warn(String.format("Failed to load icon " + 
                            "specified for '%s' command", 
                            commandData.getId()));
                    }
                    commandData.setIcon(icon);
                }
                else if (field.equals("hint"))
                {
                    commandData.setHint(value);
                }
                // In order to ignore empty content between elements
                field = null;
            }
        }
    }
    
    /*
     * Auxilary function, trying to load command handler class. Exceptions 
     * are wrapped in {@code SAXException} to satisfy requirements of 
     * {@code DefaultHandler} exception specification.
     */
    private Class<? extends Command> getHandlerForName(String className) 
        throws SAXException
    {
        try
        {
            Class<?> clazz = Class.forName(className);
            return clazz.asSubclass(Command.class);
        }
        // ClassNotFoundException is runtime, but it's expected to come up
        // here and is not a programming error in this context.
        catch (ClassNotFoundException e)
        {
            throw new SAXException("Cannot find command handler", e);
        }
        catch (ClassCastException e)
        {
            throw new SAXException("Handler is not of Command type", e);
        }
    }
    
    /*
     * Handler for parsing part of XML config file containing menu 
     * definitions. Used by {@code TopLevelHandler}.
     */
    private class MenuBarReaderHandler extends LoggingHandler
    {        
        private GUIBuilder builder;
        private List<String> commandList;
        private String menuName;
        boolean readingCommand = false;
    
        public MenuBarReaderHandler(GUIBuilder builder)
        {
            this.builder = builder; 
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
            Attributes attributes)
        {
            if (qName.equals("menu"))
            {
                menuName = attributes.getValue("name");
                commandList = new ArrayList<String>();
            }
            else if (qName.equals("command"))
            {
                readingCommand = true;
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
        {
            if (qName.equals("menu"))
            {
                builder.addMenu(menuName, commandList);
            }
            else if (qName.equals("command"))
            {
                readingCommand = false;
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws 
            SAXException
        {
            // readingCommand ensures we ignore empty strings between
            // <command> tags
            if (readingCommand)
            {
                String commandId = new String(ch, start, length).trim();
                commandList.add(commandId);
            }
        }
    }
    
    /*
     * Handler for parsing part of XML config file containing toolbar 
     * definition. Used by {@code TopLevelHandler}.
     */
    private class ToolbarReaderHandler extends LoggingHandler
    {        
        private GUIBuilder builder;
        private boolean readingCommand = false;
        private List<String> commandList = new ArrayList<String>();
    
        public ToolbarReaderHandler(GUIBuilder builder)
        {
            this.builder = builder; 
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
            Attributes attributes)
        {
            if (qName.equals("command"))
            {
                readingCommand = true;
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName)
        {
            // Since handlers are changed when new part of a configuraton
            // begins, </toolbar> will be handled here. This is the place
            // to pass the list to the listener.
            if (qName.equals("toolbar"))
            {
                builder.addToolbarItems(commandList);
            }
            else if (qName.equals("command"))
            {
                readingCommand = false;
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws 
            SAXException
        {
            // readingCommand ensures we ignore empty strings between
            // <command> tags
            if (readingCommand)
            {
                String commandId = new String(ch, start, length).trim();
                commandList.add(commandId);
            }
        }
    }
}

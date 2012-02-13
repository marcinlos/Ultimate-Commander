package mlos.ultcom.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


import org.apache.log4j.Logger;

/**
 * Auxilary class for reading and storing configuration in form of properties
 * file.
 * 
 * @author Marcin Los
 */
public class XMLProperties extends Properties
{
    private static final Logger logger = Logger.getLogger(XMLProperties.class);
        
    /**
     * Reads properties from xml file at passed location.
     * 
     * @param path Path of the properties file
     * 
     * @throws ConfigException If error occured during reading
     */
    public void readFromFile(String path) throws ConfigException
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(path);
            loadFromXML(is);
        }
        catch (IOException e)
        {
            throw new ConfigException(e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    logger.error("Error during closing config file", e);
                }
            }
        }
    }
    
    /**
     * Stores properties in the file in xml format.
     * 
     * @param path File to store properties
     * 
     * @throws ConfigException if error occured during storing configuration
     */
    public void storeToFile(String path) throws ConfigException
    {
        OutputStream os = null;
        try
        {
            os = new FileOutputStream(path);
            this.storeToXML(os, null);
        } 
        catch (IOException e)
        {
            throw new ConfigException(e);
        }
        finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException e)
                {
                    logger.error("Error during closing config file", e);
                }
            }  
        }
    }
}

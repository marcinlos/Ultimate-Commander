package mlos.ultcom.core;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import mlos.ultcom.command.Command;

/**
 * Container for all the data about a single command. To be precise:
 * <ul>
 * <li> Command id
 * <li> Name (to display)
 * <li> Exact type of command handler ({@code Class} object)
 * <li> Key accelerator
 * <li> Icon
 * <li> Hint text
 * </ul>
 * 
 * @author Marcin Los
 * 
 * @see Command
 */
public class CommandData
{
    private String id;
    private String name;
    private Class<? extends Command> handler;
    private KeyStroke key;
    private Icon icon;
    private String hint;
    
    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return the handler
     */
    public Class<? extends Command> getHandler()
    {
        return handler;
    }
    
    /**
     * @param handler the handler to set
     */
    public void setHandler(Class<? extends Command> handler)
    {
        this.handler = handler;
    }
    
    /**
     * @return the key
     */
    public KeyStroke getKey()
    {
        return key;
    }
    
    /**
     * @param key the key to set
     */
    public void setKey(KeyStroke key)
    {
        this.key = key;
    }
    
    /**
     * @return the icon
     */
    public Icon getIcon()
    {
        return icon;
    }
    
    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon)
    {
        this.icon = icon;
    }
    
    /**
     * @return the hint
     */
    public String getHint()
    {
        return hint;
    }
    
    /**
     * @param hint the hint to set
     */
    public void setHint(String hint)
    {
        this.hint = hint;
    }
}

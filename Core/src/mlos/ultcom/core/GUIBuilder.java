package mlos.ultcom.core;

import java.util.List;

import javax.swing.Icon;

/**
 * Interface decoupling configuration parser from main window. 
 * 
 * @author Marcin Los
 */
public interface GUIBuilder
{
    /**
     * Notifies GUI about parsed command. It may be used for example for
     * creating actions responsible for executing this commands.
     * 
     * @param commandData Description of a command
     */
    public void registerCommand(CommandData commandData);
    
    /**
     * Notifies GUI about new menu.
     * 
     * @param name Name of the menu
     * @param commandList List of commands defined for this menu
     */
    public void addMenu(String name, List<String> commandList);
    
    /**
     * Notifies GUI about new items for toolbar.
     * 
     * @param commandList List of commands defined for toolbar
     */
    public void addToolbarItems(List<String> commandList);
    
    /**
     * Requests GUI to load icon specified by the {@code location} parameter.
     * 
     * @param location Location of an icon
     * 
     * @return New {@code Icon} object, or {@code null} if the implementation
     * was unable to create it.
     */
    public Icon createIcon(String location);
}

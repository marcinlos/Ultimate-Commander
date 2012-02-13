package mlos.ultcom.core;

import javax.swing.Icon;

/**
 * @author Marcin Los
 *
 * Simple interface for factory producing swing Icon.
 */
public interface IconFactory
{
    /**
     * @param location String describing location of an icon data
     * 
     * @return New {@code Icon} object, or {@code null} if an error occured
     */
    public Icon newInstance(String location);
}

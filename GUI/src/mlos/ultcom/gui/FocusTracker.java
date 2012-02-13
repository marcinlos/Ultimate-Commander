package mlos.ultcom.gui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Auxilary class to allow component keep track of focus and react when
 * any of its subcomponents obtain it.
 * 
 * @author Marcin Los
 */
public abstract class FocusTracker implements PropertyChangeListener
{
    private Component component;
    private KeyboardFocusManager focusManager;
    boolean focus = false;
    
    /**
     * @param component Component to notify about focus changes
     */
    public FocusTracker(Component component)
    {
        this.component = component;
        focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener("focusOwner", this);
    }
    
    public Component getComponent()
    {
        return component;
    }
    
    /*
     * Walks up through focus-owner's ancestors and determines whether
     * {@code component} contains it.
     */
    private boolean checkFocus()
    {
        Component focused = focusManager.getFocusOwner();
        // Go up, to the top-level component or our component
        while (focused != null)
        {
            if (focused == component)
            {
                return true;
            }
            focused = focused.getParent();
        }
        return false;
    }
    
    /**
     * Contains focus-finding logic.
     */
    @Override
    public final void propertyChange(PropertyChangeEvent e)
    {
        boolean newFocus = checkFocus();
        if (newFocus != focus)
        {
            if (newFocus)
            {
                gainedFocus();
            }
            else
            {
                lostFocus();
            }
            focus = newFocus;
        }
    }

    /**
     * Invoked when some component contained in the one passed in
     * constructor gained focus.
     */
    public abstract void gainedFocus();
    
    /**
     * Invoked when focus moved outside the component passed in
     * the constructor.
     */
    public abstract void lostFocus();
}

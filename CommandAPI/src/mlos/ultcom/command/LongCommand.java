package mlos.ultcom.command;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 * Abstract class providing means to implement long-running commands,
 * which properly comunicate with the application. Client programmer
 * needs only to implement {@code execute}, just like in an ordinary
 * {@code Command}.
 * 
 * <p>
 * Communication is realized by event listeners ({@code ProgressListener})
 * and protected {@code setProgress}, {@code finished} and {@code failed)
 * methods, which should be use by the client programmer to notify outside world about
 * changes in operation's state.
 * 
 * <p>
 * IMPORTANT: {@code finished()} should always be called at the end of
 * {@code execute}. This could easily be avoided by another level of 
 * indirection, but perhaps that would be too much.
 * 
 * @author Marcin Los
 */
public abstract class LongCommand implements Command
{
    /*
     * Shouldn't be messed with! Changed only by {@code setProgress}.
     */
    private int progress = 0;
    
    /*
     * Used by long-running operation manager to display the task info
     */
    private String description = "";
    
    /*
     * Cancel flag
     */
    private AtomicBoolean canceled = new AtomicBoolean(false);
    
    /*
     * Not sure if this is thread-safe enough...
     */
    private EventListenerList listenerList = new EventListenerList();
    
    /*
     * {@code Runnable} for {@code SwingUtilities.invokeLater}, to notify
     * listeners in swing's event dispatch thread.
     */
    private class EventPoster implements Runnable
    {
        private ProgressEvent event;
        
        public EventPoster(ProgressEvent event)
        {
            this.event = event;
        }
        
        @Override
        public void run()
        {
            fireProgressChanged(event);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see isCanceled
     */
    @Override
    public abstract void execute(Context active, Context inactive);
    
    /**
     * Sets cancel flag to {@code true}
     */
    public void cancel()
    {
        canceled.set(true);
    }
    
    /**
     * Checks if command is canceled. Should be called at reasonably regular
     * intervals in {@code execute} to check if the command should finish
     * running if it is supposed to support canceling.
     */
    public boolean isCanceled()
    {
        return canceled.get();
    }
    
    /**
     * Adds a progress listener to {@code LongCommand}.
     * 
     * @param listener {@code ProgressListener} to add
     */
    public void addProgressListener(ProgressListener listener)
    {
        listenerList.add(ProgressListener.class, listener);
    }
    
    /**
     * Removes a progress listener from {@code LongCommand}.
     * 
     * @param listener the listener to remove
     */
    public void removeProgressListener(ProgressListener listener)
    {
        listenerList.remove(ProgressListener.class, listener);
    }
    
    /**
     * @return Value of a progress
     */
    public final synchronized int getProgress()
    {
        return progress;
    }
    
    /**
     * @return Short description of the task
     */
    public final synchronized String getDescription()
    {
        return description;
    }
    
    /**
     * @param progress Value of progress to set. By convention, {@code -1}
     * denotes unknown progress state.
     */
    protected final synchronized void setProgress(int progress)
    {
        ProgressEvent e = new ProgressEvent(this, this.progress, progress);
        this.progress = progress;
        SwingUtilities.invokeLater(new EventPoster(e));
    }
    
    /**
     * Sets new description of a command. This change does not fire progress
     * change events. Instead, description should be checked inside progress
     * listener.
     * 
     * @param description New description of a command
     */
    protected final synchronized void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * Notifies listeners that the task has finished. Should be called at
     * the end of {@code execute}. Otherwise, notification will never
     * be delivered to listeners.
     */
    protected final void finished()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                fireTaskFinished();
            }
        });
    }
    
    /**
     * Notifies listeners that the task has failed. Should be called if
     * in normal situation exception would be thrown.
     * 
     * @param cause Cause of a failure
     */
    protected final void failed(final Throwable cause)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                fireTaskFailed(cause);
            }
        });
    }
    
    /*
     * Iterates through listeners and notifies about an event.
     */
    private void fireProgressChanged(ProgressEvent e)
    {
        Object[] listeners = listenerList.getListeners(ProgressListener.class);
        for (Object o : listeners)
        {
            ((ProgressListener)o).progressChange(e);
        }
    }    
    
    /*
     * Iterates through listeners and notifies about an end of a process.
     */
    private void fireTaskFinished()
    {
        Object[] listeners = listenerList.getListeners(ProgressListener.class);
        for (Object o : listeners)
        {
            ((ProgressListener)o).finished(this);
        }
    }
    
    /*
     * Iterates through listeners and notifies about an end of a process.
     */
    private void fireTaskFailed(Throwable cause)
    {
        Object[] listeners = listenerList.getListeners(ProgressListener.class);
        for (Object o : listeners)
        {
            ((ProgressListener)o).failed(this, cause);
        }
    }
}

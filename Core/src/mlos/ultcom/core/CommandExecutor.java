package mlos.ultcom.core;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mlos.ultcom.command.Command;
import mlos.ultcom.command.Context;
import mlos.ultcom.command.LongCommand;
import mlos.ultcom.command.ProgressListener;

import org.apache.log4j.Logger;

/**
 * Singleton class responsible for executing the commands. It uses a thread
 * pool for long commands, and executes short commands directly in event 
 * dispatch thread.
 * 
 * <p>
 * It's not thread-safe at the moment; doesn't seem to cause problems,
 * though. For now, that is.
 * 
 * @author Marcin Los
 * 
 * @see Command
 * @see LongCommand
 */
public class CommandExecutor
{
    private static final Logger logger = 
        Logger.getLogger(CommandExecutor.class);
    
    private static CommandExecutor instance;    
    
    private static final int DEFAULT_POOL_SIZE = 5;
    
    private ExecutorService threadPool;
    
    /**
     * Private constructor to ensure Singleton invariant. 
     */
    private CommandExecutor()
    {
        int size = determinePoolSize();
        threadPool = Executors.newFixedThreadPool(size);
    }
    
    /**
     * @return size of a thread pool, obtained from config file or default
     */
    private int determinePoolSize()
    {
        Properties properties = ApplicationLoader.getInstance().getProperties();
        String size = properties.getProperty("thread.pool.size");
        int poolSize = DEFAULT_POOL_SIZE;
        try
        {
            poolSize = Integer.parseInt(size);
        }
        catch (Exception e)
        {
            logger.warn("Cannot read thread pool size from config file; " + 
                "using default value [" + DEFAULT_POOL_SIZE + "]", e);
        }
        return poolSize;
    }
    
    /**
     * Frees the resources used by commmand executor (in particular, thread
     * pool). It should be called at the end of the program.
     */
    public void shutdown()
    {
        threadPool.shutdownNow();
    }
    
    /**
     * Auxilary Runnable implementation which executes passed Command
     */
    private class Executor implements Runnable
    {
        private Command handler;
        private Context active;
        private Context inactive;
        
        public Executor(Command handler, Context active, Context inactive)
        {
            this.handler = handler;
            this.active = active;
            this.inactive = inactive;
        }
        
        @Override
        public void run()
        {
            handler.execute(active, inactive);
        }
    }
    
    /**
     * @return The One instance of this class
     */
    public static CommandExecutor getInstance()
    {
        if (instance == null)
        {
            instance = new CommandExecutor();
        }
        return instance;
    }
    
    /**
     * Executes passed command with {@code context} parameter. If it's a 
     * long-running command (instance of {@code LongCommand}), it's executed
     * in other thread, and {@code listener} is used to pass notifications
     * about the progress state.
     * 
     * @param command Implementation of command to execute
     * 
     * @param active {@code Context} object of an active panel
     * 
     * @param inactive {@code Context} object of an inactive panel
     * 
     * @param listener Listener to get notifications about change in
     * command progress
     * 
     * @return {@code true} if passed command has been executed in other 
     * thread, so that {@code listener} will receive notifications
     */
    public boolean execute(Command handler, Context active, Context inactive, 
        ProgressListener listener)
    {        
        // Check actual type
        if (handler instanceof LongCommand)
        {
            if (listener != null)
            {
                ((LongCommand)handler).addProgressListener(listener);
            }
            threadPool.execute(new Executor(handler, active, inactive));
            return true;
        }
        else
        {
            handler.execute(active, inactive);
            return false;
        }
    }
    
    /**
     * Creates a handler based on passed {@code Command} subclass, and
     * executes it.
     * 
     * @see CommandExecutor#createHandler
     * @see CommandExecutor#execute(Command, Context, ProgressListener)
     * 
     * @throws CommandException if attempt to create command object fails.
     */
    public boolean execute(Class<? extends Command> commandClass,
        Context active, Context inactive, ProgressListener listener)
    {
        Command handler = createHandler(commandClass);
        return execute(handler, active, inactive, listener);
    }
    
    /**
     * Creates an instance of command handler of a specified class. Wraps
     * {@code Class.newInstance}'s exception in {@code CommandException}.
     * Should be used with {@link execute(Command, Context, ProgressListener)}
     * if it's necessary to have explicit reference to {@code Command} object,
     * e.g. to keep a list of pending operations. 
     * 
     * @throws CommandException if attempt to create command object fails.
     */
    public Command createHandler(Class<? extends Command> commandClass)
    {
        try
        {
            return commandClass.newInstance();
        } 
        catch (InstantiationException e)
        {
            throw new CommandException("Attempt to create handler object " +
                "has failed", e);
        } 
        catch (IllegalAccessException e)
        {
            throw new CommandException("Attempt to create handler object " +
                "has failed", e);
        }
    }
}

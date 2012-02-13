package mlos.ultcom.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import mlos.ultcom.command.LongCommand;
import mlos.ultcom.command.ProgressEvent;
import mlos.ultcom.command.ProgressListener;

/**
 * Window showing progress of long-running tasks.
 * 
 * @author Marcin Los
 */
public class ProgressWindow extends JFrame
{
    private JPanel panel;
    
    private List<LongCommand> commands = new ArrayList<LongCommand>();
    private List<TaskPanel> panels = new ArrayList<TaskPanel>();
    private ProgressListener listener = new ProgressUpdater();
    
    public ProgressWindow()
    {
        super("Operations");
        setLayout(new BorderLayout());
        panel = new JPanel();
        add(new JScrollPane(panel));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        setSize(300, 150);
    }
    
    /**
     * Adds a command to list of displayed operations.
     * 
     * @param command Command to add
     */
    public void addOperation(LongCommand command)
    {
        TaskPanel taskPanel = new TaskPanel(command);
        taskPanel.setAlignmentX(0.5f);
        commands.add(command);
        panels.add(taskPanel);
        panel.add(taskPanel);
    }
    
    /**
     * @return Listener for progress of {@code LongCommand}s
     */
    public ProgressListener getListener()
    {
        return listener;
    }
    
    /**
     * Receives notification about progress changes, updates progress bars 
     * and labels.
     */
    private class ProgressUpdater implements ProgressListener
    {
        @Override
        public void finished(Object source)
        {
            int i = commands.indexOf(source);
            if (i != -1)
            {
                commands.remove(i);
                panel.remove(panels.get(i));
                panels.remove(i);
                panel.revalidate();
                panel.repaint();
            }
        }

        @Override
        public void progressChange(ProgressEvent e)
        {
            if (e.getSource() instanceof LongCommand)
            {
                LongCommand command = (LongCommand) e.getSource();
                int i = commands.indexOf(command);
                if (i != -1)
                {
                    TaskPanel taskPanel = panels.get(i);                    
                    taskPanel.setText(command.getDescription());
                    taskPanel.setValue(e.getNewValue());
                }
            }
        }
        
        @Override
        public void failed(Object source, Throwable cause)
        {
            JOptionPane.showMessageDialog(null, cause.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
            finished(source);
        }
    }
    
    /**
     * Panel with information about command progress.
     */
    private class TaskPanel extends JPanel
    {
        private JLabel label;
        private JProgressBar progressBar;
        
        /**
         * @param command Command to display
         */
        public TaskPanel(LongCommand command)
        {
            SpringLayout layout = new SpringLayout();
            setLayout(layout);
            
            label = new JLabel(command.getDescription());
            progressBar = new JProgressBar(0, 100);
            add(label);
            add(progressBar);
            
            SpringLayout.Constraints cons = layout.getConstraints(label);
            cons.setX(Spring.constant(5));
            cons.setY(Spring.constant(5));
            cons.setWidth(Spring.constant(130));
            
            cons = layout.getConstraints(progressBar);
            cons.setY(Spring.constant(5)); 
            
            layout.putConstraint(SpringLayout.WEST, progressBar, 3, 
                SpringLayout.EAST, label);
            
            layout.putConstraint(SpringLayout.EAST, this, 5, 
                SpringLayout.EAST, progressBar);
            
            Spring height = cons.getHeight();
            height = Spring.sum(Spring.constant(4), height);
            cons = layout.getConstraints(this);
            cons.setHeight(height);
        }
        
        /**
         * @param text Text to set as a command description
         */
        public void setText(String text)
        {
            label.setText(text);
        }
        
        /**
         * @param value Value of progress to set
         */
        public void setValue(int value)
        {
            progressBar.setValue(value);
        }
    }
}

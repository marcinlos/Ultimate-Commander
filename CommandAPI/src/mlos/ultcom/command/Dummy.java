package mlos.ultcom.command;

import javax.swing.JOptionPane;

import mlos.ultcom.fs.File;

public class Dummy implements Command
{
    @Override
    public void execute(Context active, Context inactive)
    {
        StringBuilder sb = new StringBuilder("Selected files:\n");
        for (File f : active.getSelectedFiles())
        {
            sb.append(f.getName() + "\n");
        }
        sb.append("\nSelected files in nonactive panel:\n");
        for (File f : inactive.getSelectedFiles())
        {
            sb.append(f.getName() + "\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), "Blablabla", 
            JOptionPane.INFORMATION_MESSAGE);
    }

}

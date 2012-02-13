package mlos.ultcom.command;

public class LongDummy extends LongCommand
{
    public LongDummy()
    {
        setDescription("Doing nothing");
    }
    
    @Override
    public void execute(Context active, Context inactive)
    {
        for (int i = 0; i < 100; ++ i)
        {
            try
            {
                Thread.sleep(100);
                setProgress(i);
                setDescription("Doing nothing: " + i + "%");
            }
            catch (InterruptedException e)
            {
            }
        }
        finished();
    }
}

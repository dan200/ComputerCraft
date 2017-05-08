package dan200.computercraft.core.logger;

import java.util.logging.Level;

public class StdoutLogger implements ILogger
{

    @Override
    public void log( Level level, String message )
    {
        System.out.printf( "[%s] %s\n", level.getName(), message );
    }

    @Override
    public void log( Level level, String message, Throwable t )
    {
        System.out.printf( "[%s] %s\n", level.getName(), message );
        t.printStackTrace( System.out );
    }
}

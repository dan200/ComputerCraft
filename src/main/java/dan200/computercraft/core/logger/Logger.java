package dan200.computercraft.core.logger;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Logger
{
    private static ILogger instance;

    @Nonnull
    public static ILogger getInstance()
    {
        ILogger logger = instance;
        if( logger == null ) logger = instance = new StdoutLogger();
        return logger;
    }

    public static void setInstance( @Nonnull ILogger logger )
    {
        if( logger == null ) throw new NullPointerException( "Logger cannot be null" );
        instance = logger;
    }

    public static void log( Level level, String message )
    {
        getInstance().log( level, message );
    }

    public static void log( Level level, String message, Throwable t )
    {
        getInstance().log( level, message, t );
    }

    public static void error( String message )
    {
        getInstance().log( Level.SEVERE, message );
    }

    public static void error( String message, Throwable t )
    {
        getInstance().log( Level.SEVERE, message, t );
    }

    public static void warn( String message )
    {
        getInstance().log( Level.WARNING, message );
    }

    public static void warn( String message, Throwable t )
    {
        getInstance().log( Level.WARNING, message, t );
    }

    /**
     * Logs {@code message} and creates a new {@link RuntimeException} using the same message.
     *
     * @param message The message to log.
     * @return The exception using the same message.
     */
    public static RuntimeException loggedError( String message )
    {
        getInstance().log( Level.SEVERE, message );
        return new RuntimeException( message );
    }
}

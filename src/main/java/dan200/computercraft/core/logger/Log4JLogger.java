package dan200.computercraft.core.logger;

import org.apache.logging.log4j.Logger;

import java.util.logging.Level;

public class Log4JLogger implements ILogger
{
    private final org.apache.logging.log4j.Logger logger;

    public Log4JLogger( Logger logger )
    {
        this.logger = logger;
    }

    @Override
    public void log( Level level, String message )
    {
        logger.log( mapLevel( level ), message );
    }

    @Override
    public void log( Level level, String message, Throwable t )
    {
        logger.log( mapLevel( level ), message, t );
    }

    private static org.apache.logging.log4j.Level mapLevel( Level level )
    {
        if( level == Level.SEVERE )
        {
            return org.apache.logging.log4j.Level.ERROR;
        }
        else if( level == Level.WARNING )
        {
            return org.apache.logging.log4j.Level.WARN;
        }
        else if( level == Level.INFO )
        {
            return org.apache.logging.log4j.Level.INFO;
        }
        else if( level == Level.FINE )
        {
            return org.apache.logging.log4j.Level.DEBUG;
        }
        else if( level == Level.FINER )
        {
            return org.apache.logging.log4j.Level.TRACE;
        }
        else if( level == Level.ALL )
        {
            return org.apache.logging.log4j.Level.ALL;
        }
        else
        {
            return org.apache.logging.log4j.Level.INFO;
        }
    }
}

package dan200.computercraft.core.logger;

import java.util.logging.Level;

public interface ILogger
{
    void log( Level level, String message );

    void log( Level level, String message, Throwable t );
}

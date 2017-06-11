package dan200.computercraft.core.apis.http;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dan200.computercraft.core.apis.IAPIEnvironment;

import java.util.concurrent.*;

/**
 * A task which executes asynchronously on a new thread.
 *
 * This functions very similarly to a {@link Future}, but with an additional
 * method which is called on the main thread when the task is completed.
 */
public class HTTPTask
{
    public interface IHTTPTask extends Runnable
    {
        void whenFinished( IAPIEnvironment environment );
    }

    private static final ExecutorService httpThreads = new ThreadPoolExecutor(
        4, Integer.MAX_VALUE,
        60L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        new ThreadFactoryBuilder()
            .setDaemon( true )
            .setPriority( Thread.MIN_PRIORITY + (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2 )
            .setNameFormat( "ComputerCraft-HTTP-%d" )
            .build()
    );

    private final Future<?> future;
    private final IHTTPTask task;

    private HTTPTask( Future<?> future, IHTTPTask task )
    {
        this.future = future;
        this.task = task;
    }

    public static HTTPTask submit( IHTTPTask task )
    {
        Future<?> future = httpThreads.submit( task );
        return new HTTPTask( future, task );
    }

    public void cancel()
    {
        future.cancel( false );
    }

    public boolean isFinished()
    {
        return future.isDone();
    }

    public void whenFinished( IAPIEnvironment environment )
    {
        task.whenFinished( environment );
    }
}

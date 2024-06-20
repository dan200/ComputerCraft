/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.computer;

import dan200.computercraft.ComputerCraft;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ComputerThread
{
    private static final int QUEUE_LIMIT = 256;

    /**
     * Lock used for modifications to the object
     */
    private static final Object s_stateLock = new Object();

    /**
     * Lock for various task operations
     */
    private static final Object s_taskLock = new Object();

    /**
     * Map of objects to task list
     */
    private static final WeakHashMap<Object, BlockingQueue<ITask>> s_computerTaskQueues = new WeakHashMap<>();

    /**
     * Active queues to execute
     */
    private static final BlockingQueue<BlockingQueue<ITask>> s_computerTasksActive = new LinkedBlockingQueue<>();
    private static final Set<BlockingQueue<ITask>> s_computerTasksActiveSet = new HashSet<>();

    /**
     * The default object for items which don't have an owner
     */
    private static final Object s_defaultOwner = new Object();

    /**
     * Whether the thread is stopped or should be stopped
     */
    private static boolean s_stopped = false;

    /**
     * The thread tasks execute on
     */
    private static Thread s_thread = null;

    private static final AtomicInteger s_counter = new AtomicInteger( 1 );

    /**
     * Start the computer thread
     */
    public static void start()
    {
        synchronized( s_stateLock )
        {
            s_stopped = false;
            if( s_thread == null || !s_thread.isAlive() )
            {
                SecurityManager manager = System.getSecurityManager();
                final ThreadGroup group = manager == null ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup();

                Thread thread = s_thread = new Thread( group, new TaskExecutor(), "ComputerCraft-Computer-Manager" );
                thread.setDaemon( true );
                thread.start();
            }
        }
    }

    /**
     * Attempt to stop the computer thread
     */
    public static void stop()
    {
        synchronized( s_stateLock )
        {
            if( s_thread != null )
            {
                s_stopped = true;
                if( s_thread.isAlive() )
                {
                    s_thread.interrupt();
                }
            }
        }

        synchronized( s_taskLock )
        {
            s_computerTaskQueues.clear();
            s_computerTasksActive.clear();
            s_computerTasksActiveSet.clear();
        }
    }

    /**
     * Queue a task to execute on the thread
     *
     * @param task     The task to execute
     * @param computer The computer to execute it on, use {@code null} to execute on the default object.
     */
    public static void queueTask( ITask task, Computer computer )
    {
        Object queueObject = computer == null ? s_defaultOwner : computer;

        BlockingQueue<ITask> queue;
        synchronized( s_computerTaskQueues )
        {
            queue = s_computerTaskQueues.get( queueObject );
            if( queue == null )
            {
                s_computerTaskQueues.put( queueObject, queue = new LinkedBlockingQueue<>( QUEUE_LIMIT ) );
            }
        }

        synchronized( s_taskLock )
        {
            if( queue.offer( task ) && !s_computerTasksActiveSet.contains( queue ) )
            {
                s_computerTasksActive.add( queue );
                s_computerTasksActiveSet.add( queue );
            }
        }
    }

    /**
     * Responsible for pulling and managing computer tasks. This pulls a task from {@link #s_computerTasksActive},
     * creates a new thread using {@link TaskRunner} or reuses a previous one and uses that to execute the task.
     *
     * If the task times out, then it will attempt to interrupt the {@link TaskRunner} instance.
     */
    private static final class TaskExecutor implements Runnable
    {
        private TaskRunner runner;
        private Thread thread;

        @Override
        public void run()
        {
            try
            {
                while( true )
                {
                    // Wait for an active queue to execute
                    BlockingQueue<ITask> queue = s_computerTasksActive.take();

                    // If threads should be stopped then return
                    synchronized( s_stateLock )
                    {
                        if( s_stopped ) return;
                    }

                    execute( queue );
                }
            }
            catch( InterruptedException ignored )
            {
                Thread.currentThread().interrupt();
            }
        }

        private void execute( BlockingQueue<ITask> queue ) throws InterruptedException
        {
            ITask task = queue.remove();

            if( thread == null || !thread.isAlive() )
            {
                runner = new TaskRunner();

                SecurityManager manager = System.getSecurityManager();
                final ThreadGroup group = manager == null ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup();
                Thread thread = this.thread = new Thread( group, runner, "ComputerCraft-Computer-Runner" + s_counter.getAndIncrement() );
                thread.setDaemon( true );
                thread.start();
            }

            // Execute the task
            runner.submit( task );

            try
            {
                // If we timed out rather than exiting:
                boolean done = runner.await( 7000 );
                if( !done )
                {
                    // Attempt to soft then hard abort
                    Computer computer = task.getOwner();
                    if( computer != null )
                    {
                        computer.abort( false );

                        done = runner.await( 1500 );
                        if( !done )
                        {
                            computer.abort( true );
                            done = runner.await( 1500 );
                        }
                    }

                    // Interrupt the thread
                    if( !done )
                    {
                        thread.interrupt();
                        thread = null;
                        runner = null;
                    }
                }
            }
            finally
            {
                // Re-add it back onto the queue or remove it
                synchronized( s_taskLock )
                {
                    if( queue.isEmpty() )
                    {
                        s_computerTasksActiveSet.remove( queue );
                    }
                    else
                    {
                        s_computerTasksActive.add( queue );
                    }
                }
            }
        }
    }

    /**
     * Responsible for the actual running of tasks. It waitin for the {@link TaskRunner#input} semaphore to be
     * triggered, consumes a task and then triggers {@link TaskRunner#finished}.
     */
    private static final class TaskRunner implements Runnable
    {
        private final Semaphore input = new Semaphore();
        private final Semaphore finished = new Semaphore();
        private ITask task;

        @Override
        public void run()
        {
            try
            {
                while( true )
                {
                    input.await();
                    try
                    {
                        task.execute();
                    }
                    catch( RuntimeException e )
                    {
                        ComputerCraft.log.error( "Error running task.", e );
                    }
                    task = null;
                    finished.signal();
                }
            }
            catch( InterruptedException e )
            {
                ComputerCraft.log.error( "Error running task.", e );
                Thread.currentThread().interrupt();
            }
        }

        void submit( ITask task )
        {
            this.task = task;
            input.signal();
        }

        boolean await( long timeout ) throws InterruptedException
        {
            return finished.await( timeout );
        }
    }

    /**
     * A simple method to allow awaiting/providing a signal.
     *
     * Java does provide similar classes, but I only needed something simple.
     */
    private static final class Semaphore
    {
        private volatile boolean state = false;

        synchronized void signal()
        {
            state = true;
            notify();
        }

        synchronized void await() throws InterruptedException
        {
            while( !state ) wait();
            state = false;
        }

        synchronized boolean await( long timeout ) throws InterruptedException
        {
            if( !state )
            {
                wait( timeout );
                if( !state ) return false;
            }
            state = false;
            return true;
        }
    }
}

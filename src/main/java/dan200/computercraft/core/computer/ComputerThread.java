/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.computer;

import dan200.computercraft.ComputerCraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
    
public class ComputerThread
{
    private static final Object m_lock;
    
    private static Thread m_thread;
    private static final WeakHashMap <Object, LinkedBlockingQueue<ITask>> m_computerTasks;
    private static final ArrayList <LinkedBlockingQueue<ITask>> m_computerTasksActive;
    private static final ArrayList <LinkedBlockingQueue<ITask>> m_computerTasksPending;
    private static final Object m_defaultQueue;
    private static final Object m_monitor;

    private static boolean m_running;
    private static boolean m_stopped;
    
    static
    {
        m_lock = new Object();    
        m_thread = null;
        m_computerTasks = new WeakHashMap<>();
        m_computerTasksPending = new ArrayList<>();
        m_computerTasksActive = new ArrayList<>();
        m_defaultQueue = new Object();
        m_monitor = new Object();
        m_running = false;
        m_stopped = false;
    }
        
    public static void start()
    {
        synchronized( m_lock )
        {
            if( m_running )
            {
                m_stopped = false;
                return;
            }
        
            m_thread = new Thread( () ->
            {
                while( true )
                {
                    synchronized( m_computerTasksPending )
                    {
                        if (!m_computerTasksPending.isEmpty())
                        {
                            Iterator<LinkedBlockingQueue<ITask>> it = m_computerTasksPending.iterator();
                            while(it.hasNext())
                            {
                                LinkedBlockingQueue<ITask> queue = it.next();
                                
                                if (!m_computerTasksActive.contains(queue))
                                {
                                    m_computerTasksActive.add(queue);
                                }
                                it.remove();
                            }
                            /*
                            m_computerTasksActive.addAll(m_computerTasksPending); // put any that have been added since
                            m_computerTasksPending.clear();
                            */
                        }
                    }
                    
                    Iterator<LinkedBlockingQueue<ITask>> it = m_computerTasksActive.iterator();
                    
                    while (it.hasNext())
                    {
                        LinkedBlockingQueue<ITask> queue = it.next();
                        
                        if (queue == null || queue.isEmpty()) // we don't need the blocking part of the queue. Null check to ensure it exists due to a weird NPE I got
                        {
                            continue;
                        }
                        
                        synchronized( m_lock )
                        {
                            if( m_stopped )
                            {
                                m_running = false;
                                m_thread = null;
                                return;
                            }
                        }
                        
                        try
                        {
                            final ITask task = queue.take();

                            // Create the task
                            Thread worker = new Thread( () ->
                            {
                                try {
                                    task.execute();
                                } catch( Throwable e ) {
                                    ComputerCraft.log.error( "Error running task", e );
                                }
                            } );
                            
                            // Run the task
                            worker.setDaemon(true);
                            worker.start();
                            worker.join( 7000 );
                            
                            if( worker.isAlive() )
                            {
                                // Task ran for too long
                                // Initiate escape plan
                                Computer computer = task.getOwner();
                                if( computer != null )
                                {
                                    // Step 1: Soft abort
                                    computer.abort( false );
                                    worker.join( 1500 );
                            
                                    if( worker.isAlive() )
                                    {
                                        // Step 2: Hard abort
                                        computer.abort( true );
                                        worker.join( 1500 );
                                    }
                                }
                                
                                // Step 3: abandon
                                if( worker.isAlive() )
                                {
                                    // ComputerCraft.log.warn( "Failed to abort Computer " + computer.getID() + ". Dangling lua thread could cause errors." );
                                    worker.interrupt();
                                }
                            }                
                        }
                        catch( InterruptedException e )
                        {
                            continue;
                        }

                        synchronized (queue)
                        {
                            if (queue.isEmpty())
                            {
                                it.remove();
                            }
                        }
                    }
                    
                    while (m_computerTasksActive.isEmpty() && m_computerTasksPending.isEmpty())
                    {
                        synchronized (m_monitor)
                        {
                            try 
                            {
                                m_monitor.wait();
                            }
                            catch( InterruptedException e )
                            {
                            }
                        }
                    }
                }
            }, "Computer Dispatch Thread" );

            m_thread.setDaemon(true);
            m_thread.start();
            m_running = true;
        }
    }
    
    public static void stop()
    {
        synchronized( m_lock )
        {
            if( m_running )
            {
                m_stopped = true;
                m_thread.interrupt();
            }
        }
    }
    
    public static void queueTask( ITask _task, Computer computer )
    {
        Object queueObject = computer;
        
        if (queueObject == null)
        {
            queueObject = m_defaultQueue;
        }
        
        LinkedBlockingQueue<ITask> queue = m_computerTasks.get(queueObject);

        if (queue == null)
        {
            m_computerTasks.put(queueObject, queue = new LinkedBlockingQueue<>( 256 ));
        }
        
        synchronized ( m_computerTasksPending )
        {
            if( queue.offer( _task ) )
            {
                if( !m_computerTasksPending.contains( queue ) )
                {
                    m_computerTasksPending.add( queue );
                }
            }
            else
            {
                //System.out.println( "Event queue overflow" );
            }
        }
        
        synchronized (m_monitor)
        {
            m_monitor.notify();
        }
    }
}

/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.socket;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.socket.IAsyncObject;

public class AsyncAction {

    private List<Future<?>> m_futures;
	private ExecutorService threadPool;
	private String event_type = "async";
	
	public AsyncAction() {
		init();
	}
	
	public AsyncAction(String ev_t) {
		event_type = ev_t;
		init();
	}
	
	public void init() {
		threadPool = new ThreadPoolExecutor(
			4, 2048,
			60L, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>(),
			new ThreadFactoryBuilder()
				.setDaemon( true )
				.setPriority( Thread.MIN_PRIORITY + (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2 )
				.setNameFormat( "ComputerCraft-"+event_type+"-%d" )
				.build()
		);
		m_futures = new ArrayList<Future<?>>();
	}
	
	public int runAsyncAction (IAsyncObject callable, IAPIEnvironment environment, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) {
		AsyncMethod meth = new AsyncMethod(callable, environment, context, method, args, event_type);
		m_futures.add(threadPool.submit(meth));
		
		return meth.ID;
	}
	
	public void clear() {
		synchronized (m_futures) {
			for (Future<?> k: m_futures) {
				k.cancel( false );
			}
			m_futures.clear();
		}
		threadPool.shutdown();
	}
}      

class AsyncMethod implements Runnable {

    private IAsyncObject m_callable;
    private IAPIEnvironment m_environment;
    private ILuaContext m_context;
    private int m_method;
    private Object[] m_args;
	private String m_event_type;
    public int ID;

    public AsyncMethod(IAsyncObject callable, IAPIEnvironment environment, @Nonnull ILuaContext context, int method, @Nonnull Object[] args, @Nonnull String event_type) {
        m_callable = callable;
        m_environment = environment;
        m_context = context;
        m_method = method;
        m_args = args;
		m_event_type = event_type;
        ID = Counter.value;
        Counter.inc();
    }

    public void run() {
		try {
			Object[] rtn = m_callable.callAsyncMeth(m_context, m_method, m_args);
			Object[] finalRtn = new Object[rtn.length + 1];
			for (int i = 0; i < rtn.length; i++) {
				finalRtn[i + 1] = rtn[i];
			};
			finalRtn[0] = ID;
			m_environment.queueEvent(m_event_type, finalRtn);
		} catch (Exception e) {
			m_environment.queueEvent(m_event_type, new Object[] {ID, false, e.getMessage()});
		}
    }

}

class Counter {

    public static int value = 0;

    public static void inc() {
        if (value == Integer.MAX_VALUE) value = -1;
        value++;
    }

}
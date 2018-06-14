/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.socket;

import javax.annotation.Nonnull;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.socket.IAsyncObject;

public class AsyncAction
{
	
	private Thread m_thread;
	public int ID;
	
	public AsyncAction(IAsyncObject callable, IAPIEnvironment environment, @Nonnull ILuaContext context, int method, @Nonnull Object[] args)
	{
		ArgRunnable newThread = new ArgRunnable(callable, environment, context, method, args);
		ID = newThread.ID;
		m_thread = new Thread(newThread);
		m_thread.setDaemon(true);
		m_thread.start();
	}
	
	public boolean isDone()
	{
		return !(m_thread.isAlive());
	}
	
}

class ArgRunnable implements Runnable
{
	
	private IAsyncObject m_callable;
	private IAPIEnvironment m_environment;
	private ILuaContext m_context;
	private int m_method;
	private Object[] m_args;
	public int ID;
	
	public ArgRunnable(IAsyncObject callable, IAPIEnvironment environment, @Nonnull ILuaContext context, int method, @Nonnull Object[] args)
	{
		m_callable = callable;
		m_environment = environment;
		m_context = context;
		m_method = method;
		m_args = args;
		ID = Counter.value;
		Counter.inc();
	}
	
	public void run()
	{		
		Object[] rtn = m_callable.realCallMeth(m_context, m_method, m_args);
		Object[] finalRtn = new Object[rtn.length+1];
		for (int i = 0; i < rtn.length; i++)
		{
			finalRtn[i+1] = rtn[i];
		};
		finalRtn[0] = ID;
		m_environment.queueEvent( "async", finalRtn );
	}
	
}

class Counter
{
	
	public static int value = 0;
	
	public static void inc()
	{
		if (value==2147483647) value = -1;
		value++;
	}
	
}
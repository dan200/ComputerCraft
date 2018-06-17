/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.socket;

import javax.annotation.Nonnull;
import java.util.*;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.socket.IAsyncObject;

public class AsyncAction implements Runnable {

    private static final List<AsyncMethod> m_actions = new ArrayList<AsyncMethod>();

    public void startAsyncAction() {
        Thread m_thread = new Thread(this);
        m_thread.setDaemon(true);
        m_thread.start();
    }
	
	public static int runAsyncAction (IAsyncObject callable, IAPIEnvironment environment, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) {
		AsyncMethod meth = new AsyncMethod(callable, environment, context, method, args);
		synchronized (m_actions) {
			m_actions.add(meth);
		}
		return meth.ID;
	}
	
	public static void clear() {
		synchronized (m_actions) {
			m_actions.clear();
		}
	}
	
	public void run() {
		while (true) {
			synchronized (m_actions) {
				Iterator<AsyncMethod> acts = m_actions.iterator();
				while (acts.hasNext()) {
					AsyncMethod action = acts.next();
					action.run();
					acts.remove();
				}
			}
		}
	}
}      

class AsyncMethod {

    private IAsyncObject m_callable;
    private IAPIEnvironment m_environment;
    private ILuaContext m_context;
    private int m_method;
    private Object[] m_args;
    public int ID;

    public AsyncMethod(IAsyncObject callable, IAPIEnvironment environment, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) {
        m_callable = callable;
        m_environment = environment;
        m_context = context;
        m_method = method;
        m_args = args;
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
			m_environment.queueEvent("async", finalRtn);
		} catch (Exception e) {
			m_environment.queueEvent("async", new Object[] {ID, false, e.getMessage()});
		}
    }

}

class Counter {

    public static int value = 0;

    public static void inc() {
        if (value == 2147483647) value = -1;
        value++;
    }

}
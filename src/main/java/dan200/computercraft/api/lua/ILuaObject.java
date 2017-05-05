/**
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api.lua;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

/**
 * An interface for representing custom objects returned by {@link IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])}
 * calls.
 *
 * Return objects implementing this interface to expose objects with methods to lua.
 */
public interface ILuaObject
{
    /**
     * Get the names of the methods that this object implements. This works the same as {@link IPeripheral#getMethodNames()}.
     * See that method for detailed documentation.
     *
     * @see IPeripheral#getMethodNames()
     */
    public String[] getMethodNames();

    /**
     * Called when a user calls one of the methods that this object implements. This works the same as
     * {@link IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])}}. See that method for detailed
     * documentation.
     *
     * @throws LuaException         If the task could not be queued, or if the task threw an exception.
     * @throws InterruptedException If the user shuts down or reboots the computer the coroutine is suspended,
     *                              InterruptedException will be thrown. This exception must not be caught or
     *                              intercepted, or the computer will leak memory and end up in a broken state.w
     * @see IPeripheral#callMethod(IComputerAccess, ILuaContext, int, Object[])
     */
    public Object[] callMethod( ILuaContext context, int method, Object[] arguments ) throws LuaException, InterruptedException;
}

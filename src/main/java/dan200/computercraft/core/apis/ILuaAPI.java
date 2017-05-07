/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;
import dan200.computercraft.api.lua.ILuaObject;

public interface ILuaAPI extends ILuaObject
{
    public String[] getNames();

    public void startup(); // LT
    public void advance( double _dt ); // MT
    public void shutdown(); // LT
}

/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.socket;

import javax.annotation.Nonnull;
import dan200.computercraft.api.lua.ILuaContext;

public interface IAsyncObject
{
	Object[] callAsyncMeth( @Nonnull ILuaContext context, int method, @Nonnull Object[] args );
}
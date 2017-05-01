/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import dan200.computercraft.api.filesystem.IMount;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EmptyMount implements IMount
{	
	public EmptyMount()
	{
	}
	
	// IMount implementation
	
	@Override
	public boolean exists( String path ) throws IOException
	{
		return path.isEmpty();
	}
	
	@Override
	public boolean isDirectory( String path ) throws IOException
	{
		return path.isEmpty();
	}
	
	@Override
	public void list( String path, List<String> contents ) throws IOException
	{
	}
	
	@Override
	public long getSize( String path ) throws IOException
	{
		return 0;
	}

	@Override
	public InputStream openForRead( String path ) throws IOException
	{
		return null;
	}
}

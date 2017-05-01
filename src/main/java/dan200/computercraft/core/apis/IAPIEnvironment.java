/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.core.filesystem.FileSystem;
import dan200.computercraft.core.terminal.Terminal;

public interface IAPIEnvironment
{
	public static interface IPeripheralChangeListener
	{
		public void onPeripheralChanged( int side, IPeripheral newPeripheral );
	}
	
	public Computer getComputer();
	public int getComputerID();
	public IComputerEnvironment getComputerEnvironment();
	public Terminal getTerminal();
	public FileSystem getFileSystem();
	
	public void shutdown();
	public void reboot();
    public void queueEvent( String event, Object[] args );

	public void setOutput( int side, int output );
	public int getOutput( int side );
	public int getInput( int side );

	public void setBundledOutput( int side, int output );
	public int getBundledOutput( int side );
	public int getBundledInput( int side );
	
	public void setPeripheralChangeListener( IPeripheralChangeListener listener );
	public IPeripheral getPeripheral( int side );

    public String getLabel();
    public void setLabel( String label );
}

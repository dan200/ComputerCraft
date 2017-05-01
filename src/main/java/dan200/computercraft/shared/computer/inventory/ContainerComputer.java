/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.inventory;

import dan200.computercraft.shared.computer.blocks.TileComputer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerComputer extends Container
{
	private TileComputer m_computer;
	
	public ContainerComputer( TileComputer computer )
	{
		m_computer = computer;
	}
	
	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
        return m_computer.isUseableByPlayer( player );
	}
}

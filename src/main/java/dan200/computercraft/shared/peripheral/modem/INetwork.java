/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public interface INetwork
{
	public void addReceiver( IReceiver receiver );
	public void removeReceiver( IReceiver receiver );
	public void transmit( int channel, int replyChannel, Object payload, World world, Vec3 pos, double range, boolean interdimensional, Object senderObject );
	public boolean isWireless();
}

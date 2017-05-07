/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface INetwork
{
    void addReceiver( IReceiver receiver );
    void removeReceiver( IReceiver receiver );
    void transmit( int channel, int replyChannel, Object payload, World world, Vec3d pos, double range, boolean interdimensional, Object senderObject );
    boolean isWireless();
}

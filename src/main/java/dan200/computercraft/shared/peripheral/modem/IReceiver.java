/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IReceiver
{
    int getChannel();
    World getWorld();
    Vec3d getWorldPosition();
    boolean isInterdimensional();
    double getReceiveRange();
    void receiveSameDimension( int replyChannel, Object payload, double distance, Object senderObject );
    void receiveDifferentDimension( int replyChannel, Object payload, Object senderObject );
}

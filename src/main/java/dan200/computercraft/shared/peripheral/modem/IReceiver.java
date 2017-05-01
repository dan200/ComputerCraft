/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IReceiver
{
    public int getChannel();
    public World getWorld();
    public Vec3d getWorldPosition();
    public boolean isInterdimensional();
    public double getReceiveRange();
    public void receiveSameDimension( int replyChannel, Object payload, double distance, Object senderObject );
    public void receiveDifferentDimension( int replyChannel, Object payload, Object senderObject );
}

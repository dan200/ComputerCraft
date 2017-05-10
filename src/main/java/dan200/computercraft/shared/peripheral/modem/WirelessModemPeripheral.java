/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import dan200.computercraft.ComputerCraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class WirelessModemPeripheral extends ModemPeripheral
{
    private boolean m_advanced;

    public WirelessModemPeripheral( boolean advanced )
    {
        m_advanced = advanced;
    }

    @Override
    protected boolean isInterdimensional()
    {
        return m_advanced;
    }

    @Override
    protected double getTransmitRange()
    {
        if( m_advanced )
        {
            return (double)Integer.MAX_VALUE;
        }
        else
        {
            World world = getWorld();
            if( world != null )
            {
                Vec3d position = getPosition();
                double minRange = (double) ComputerCraft.modem_range;
                double maxRange = (double) ComputerCraft.modem_highAltitudeRange;
                if( world.isRaining() && world.isThundering() )
                {
                    minRange = (double) ComputerCraft.modem_rangeDuringStorm;
                    maxRange = (double) ComputerCraft.modem_highAltitudeRangeDuringStorm;
                }
                if( position.yCoord > 96.0 && maxRange > minRange )
                {
                    return minRange + ( position.yCoord - 96.0 ) * ( ( maxRange - minRange ) / ( ( world.getHeight() - 1 ) - 96.0 ) );
                }
                return minRange;
            }
            return 0.0;
        }
    }

    @Override
    protected INetwork getNetwork()
    {
        return WirelessNetwork.getUniversal();
    }
}

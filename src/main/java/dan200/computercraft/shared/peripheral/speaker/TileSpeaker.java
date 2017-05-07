/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.speaker;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.common.TilePeripheralBase;
import net.minecraft.util.EnumFacing;

public class TileSpeaker extends TilePeripheralBase
{
    // Statics
    public static final int MIN_TICKS_BETWEEN_SOUNDS = 1;

    // Members
    private SpeakerPeripheral m_peripheral;

    @Override
    public synchronized void update()
    {
        if (m_peripheral != null)
        {
            m_peripheral.updateClock();
        }

    }

    // IPeripheralTile implementation
    public IPeripheral getPeripheral(EnumFacing side)
    {
        m_peripheral = new SpeakerPeripheral(this);
        return m_peripheral;
    }

}

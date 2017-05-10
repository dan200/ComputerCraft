/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum PeripheralType implements IStringSerializable
{
    DiskDrive( "disk_drive" ),
    Printer( "printer" ),
    Monitor( "monitor" ),
    AdvancedMonitor( "advanced_monitor" ),
    WirelessModem( "wireless_modem" ),
    WiredModem( "wired_modem" ),
    Cable( "cable" ),
    WiredModemWithCable( "wired_modem_with_cable" ),
    AdvancedModem( "advanced_modem" );

    private String m_name;

    PeripheralType( String name )
    {
        m_name = name;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return m_name;
    }

    @Override
    public String toString() { return getName(); }
}

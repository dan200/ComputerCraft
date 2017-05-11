/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.blocks;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.core.ServerComputer;

import javax.annotation.Nonnull;

public class ComputerPeripheral
    implements IPeripheral
{
    private final String m_type;
    private final ServerComputer m_computer;

    public ComputerPeripheral( String type, ServerComputer computer )
    {
        m_type = type;
        m_computer = computer;
    }
        
    // IPeripheral implementation

    @Nonnull
    @Override
    public String getType()
    {
        return m_type;
    }

    @Nonnull
    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "turnOn",
            "shutdown",
            "reboot",
            "getID",
            "isOn",
        };
    }

    @Override
    public Object[] callMethod( @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments ) throws LuaException
    {
        switch( method )
        {
            case 0:
            {
                // turnOn
                m_computer.turnOn();
                return null;
            }
            case 1:
            {
                // shutdown
                m_computer.shutdown();
                return null;
            }
            case 2:
            {
                // reboot
                m_computer.reboot();
                return null;
            }
            case 3:
            {
                // getID
                return new Object[] {
                    m_computer.assignID()
                };
            }
            case 4:
            {
                // isOn
                return new Object[] { m_computer.isOn() };
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    public void attach( @Nonnull IComputerAccess computer )
    {
    }

    @Override
    public void detach( @Nonnull IComputerAccess computer )
    {
    }

    @Override
    public boolean equals( IPeripheral other )
    {
        return (other != null && other.getClass() == this.getClass());
    }
}

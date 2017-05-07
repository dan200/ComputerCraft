/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.core;

import dan200.computercraft.shared.common.ITerminal;

public interface IComputer extends ITerminal
{
    public int getInstanceID();
    public int getID();
    public String getLabel();

    public boolean isOn();
    public boolean isCursorDisplayed();
    public void turnOn();
    public void shutdown();
    public void reboot();

    public void queueEvent( String event );
    public void queueEvent( String event, Object[] arguments );
}

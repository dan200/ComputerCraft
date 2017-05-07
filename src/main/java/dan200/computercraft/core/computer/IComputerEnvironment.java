/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.computer;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;

public interface IComputerEnvironment
{
    public int getDay();
    public double getTimeOfDay();
    public boolean isColour();
    public long getComputerSpaceLimit();
    public String getHostString();

    public int assignNewID();
    public IWritableMount createSaveDirMount( String subPath, long capacity );
    public IMount createResourceMount( String domain, String subPath );
}

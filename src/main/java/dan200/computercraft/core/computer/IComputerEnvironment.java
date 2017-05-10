/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.computer;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;

public interface IComputerEnvironment
{
    int getDay();
    double getTimeOfDay();
    boolean isColour();
    long getComputerSpaceLimit();
    String getHostString();

    int assignNewID();
    IWritableMount createSaveDirMount( String subPath, long capacity );
    IMount createResourceMount( String domain, String subPath );
}

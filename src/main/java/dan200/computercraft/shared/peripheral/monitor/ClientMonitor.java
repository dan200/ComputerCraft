package dan200.computercraft.shared.peripheral.monitor;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.common.ClientTerminal;
import net.minecraft.util.math.BlockPos;

public class ClientMonitor extends ClientTerminal
{
    private final TileMonitor origin;

    public long lastRenderFrame = -1;
    public BlockPos lastRenderPos = null;
    public int renderDisplayList = -1;

    public ClientMonitor( boolean colour, TileMonitor origin )
    {
        super( colour );
        this.origin = origin;
    }

    public TileMonitor getOrigin()
    {
        return origin;
    }

    public void destroy()
    {
        if( renderDisplayList != -1 )
        {
            ComputerCraft.deleteDisplayLists( renderDisplayList, 3 );
            renderDisplayList = -1;
        }
    }
}

package dan200.computercraft;

import dan200.computercraft.api.ComputerCraftAPI.IComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.media.IMediaProvider;
import dan200.computercraft.api.network.IPacketNetwork;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.permissions.ITurtlePermissionProvider;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.common.DefaultBundledRedstoneProvider;
import dan200.computercraft.shared.peripheral.modem.WirelessNetwork;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ComputerCraftAPIImpl implements IComputerCraftAPI
{
    public static final IComputerCraftAPI INSTANCE = new ComputerCraftAPIImpl();

    private ComputerCraftAPIImpl()
    {
    }

    @Nonnull
    @Override
    public String getInstalledVersion()
    {
        return "${version}";
    }

    @Override
    public int createUniqueNumberedSaveDir( @Nonnull World world, @Nonnull String parentSubPath )
    {
        return ComputerCraft.createUniqueNumberedSaveDir( world, parentSubPath );
    }

    @Nullable
    @Override
    public IWritableMount createSaveDirMount( @Nonnull World world, @Nonnull String subPath, long capacity )
    {
        return ComputerCraft.createSaveDirMount( world, subPath, capacity );
    }

    @Nullable
    @Override
    public IMount createResourceMount( @Nonnull Class<?> modClass, @Nonnull String domain, @Nonnull String subPath )
    {
        return ComputerCraft.createResourceMount( modClass, domain, subPath );
    }

    @Override
    public void registerPeripheralProvider( @Nonnull IPeripheralProvider handler )
    {
        ComputerCraft.registerPeripheralProvider( handler );
    }

    @Override
    public void registerTurtleUpgrade( @Nonnull ITurtleUpgrade upgrade )
    {
        ComputerCraft.registerTurtleUpgrade( upgrade );
    }

    @Override
    public void registerBundledRedstoneProvider( @Nonnull IBundledRedstoneProvider handler )
    {
        ComputerCraft.registerBundledRedstoneProvider( handler );
    }

    @Override
    public int getBundledRedstoneOutput( @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side )
    {
        if( WorldUtil.isBlockInWorld( world, pos ) )
        {
            return DefaultBundledRedstoneProvider.getDefaultBundledRedstoneOutput( world, pos, side );
        }
        return -1;
    }

    @Override
    public void registerMediaProvider( @Nonnull IMediaProvider handler )
    {
        ComputerCraft.registerMediaProvider( handler );
    }

    @Override
    public void registerPermissionProvider( @Nonnull ITurtlePermissionProvider handler )
    {
        ComputerCraft.registerPermissionProvider( handler );
    }

    @Override
    public void registerPocketUpgrade( @Nonnull IPocketUpgrade upgrade )
    {
        ComputerCraft.registerPocketUpgrade( upgrade );
    }

    @Override
    public IPacketNetwork getWirelessNetwork()
    {
        return WirelessNetwork.getUniversal();
    }
}

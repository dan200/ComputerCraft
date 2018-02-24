/*
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computercraft.api;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.media.IMediaProvider;
import dan200.computercraft.api.network.IPacketNetwork;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.permissions.ITurtlePermissionProvider;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.redstone.IBundledRedstoneProvider;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The static entry point to the ComputerCraft API.
 * Members in this class must be called after mod_ComputerCraft has been initialised,
 * but may be called before it is fully loaded.
 */
public final class ComputerCraftAPI
{
    public static boolean isInstalled()
    {
        return tryLoadCC() != null;
    }

    @Nonnull
    public static String getInstalledVersion()
    {
        IComputerCraftAPI api = tryLoadCC();
        return api != null ? api.getInstalledVersion() : "";
    }

    @Nonnull
    public static String getAPIVersion()
    {
        return "${version}";
    }

    /**
     * Creates a numbered directory in a subfolder of the save directory for a given world, and returns that number.
     *
     * Use in conjunction with createSaveDirMount() to create a unique place for your peripherals or media items to store files.
     *
     * @param world         The world for which the save dir should be created. This should be the server side world object.
     * @param parentSubPath The folder path within the save directory where the new directory should be created. eg: "computercraft/disk"
     * @return The numerical value of the name of the new folder, or -1 if the folder could not be created for some reason.
     *
     * eg: if createUniqueNumberedSaveDir( world, "computer/disk" ) was called returns 42, then "computer/disk/42" is now
     * available for writing.
     * @see #createSaveDirMount(World, String, long)
     */
    public static int createUniqueNumberedSaveDir( @Nonnull World world, @Nonnull String parentSubPath )
    {
        IComputerCraftAPI api = tryLoadCC();
        return api != null ? api.createUniqueNumberedSaveDir( world, parentSubPath ) : -1;
    }

    /**
     * Creates a file system mount that maps to a subfolder of the save directory for a given world, and returns it.
     *
     * Use in conjunction with IComputerAccess.mount() or IComputerAccess.mountWritable() to mount a folder from the
     * users save directory onto a computers file system.
     *
     * @param world    The world for which the save dir can be found. This should be the server side world object.
     * @param subPath  The folder path within the save directory that the mount should map to. eg: "computer/disk/42".
     *                 Use createUniqueNumberedSaveDir() to create a new numbered folder to use.
     * @param capacity The amount of data that can be stored in the directory before it fills up, in bytes.
     * @return The mount, or null if it could be created for some reason. Use IComputerAccess.mount() or IComputerAccess.mountWritable()
     * to mount this on a Computers' file system.
     * @see #createUniqueNumberedSaveDir(World, String)
     * @see IComputerAccess#mount(String, IMount)
     * @see IComputerAccess#mountWritable(String, IWritableMount)
     * @see IMount
     * @see IWritableMount
     */
    @Nullable
    public static IWritableMount createSaveDirMount( @Nonnull World world, @Nonnull String subPath, long capacity )
    {
        IComputerCraftAPI api = tryLoadCC();
        return api != null ? api.createSaveDirMount( world, subPath, capacity ) : null;
    }

    /**
     * Creates a file system mount to a resource folder, and returns it.
     *
     * Use in conjunction with IComputerAccess.mount() or IComputerAccess.mountWritable() to mount a resource folder
     * onto a computer's file system.
     *
     * The files in this mount will be a combination of files in the specified mod jar, and resource packs that contain
     * resources with the same domain and path.
     *
     * @param modClass A class in whose jar to look first for the resources to mount. Using your main mod class is recommended. eg: MyMod.class
     * @param domain   The domain under which to look for resources. eg: "mymod".
     * @param subPath  The domain under which to look for resources. eg: "mymod/lua/myfiles".
     * @return The mount, or {@code null} if it could be created for some reason. Use IComputerAccess.mount() or
     * IComputerAccess.mountWritable() to mount this on a Computers' file system.
     * @see IComputerAccess#mount(String, IMount)
     * @see IComputerAccess#mountWritable(String, IWritableMount)
     * @see IMount
     */
    @Nullable
    public static IMount createResourceMount( @Nonnull Class<?> modClass, @Nonnull String domain, @Nonnull String subPath )
    {
        IComputerCraftAPI api = tryLoadCC();
        return api != null ? api.createResourceMount( modClass, domain, subPath ) : null;
    }

    /**
     * Registers a peripheral handler to convert blocks into {@link IPeripheral} implementations.
     *
     * @param handler The peripheral provider to register.
     * @see dan200.computercraft.api.peripheral.IPeripheral
     * @see dan200.computercraft.api.peripheral.IPeripheralProvider
     */
    public static void registerPeripheralProvider( @Nonnull IPeripheralProvider handler )
    {
        IComputerCraftAPI api = tryLoadCC();
        if( api != null ) api.registerPeripheralProvider( handler );
    }

    /**
     * Registers a new turtle turtle for use in ComputerCraft. After calling this,
     * users should be able to craft Turtles with your new turtle. It is recommended to call
     * this during the load() method of your mod.
     *
     * @param upgrade The turtle upgrade to register.
     * @see dan200.computercraft.api.turtle.ITurtleUpgrade
     */
    public static void registerTurtleUpgrade( @Nonnull ITurtleUpgrade upgrade )
    {
        IComputerCraftAPI api = tryLoadCC();
        if( api != null ) api.registerTurtleUpgrade( upgrade );
    }

    /**
     * Registers a bundled redstone handler to provide bundled redstone output for blocks.
     *
     * @param handler The bundled redstone provider to register.
     * @see dan200.computercraft.api.redstone.IBundledRedstoneProvider
     */
    public static void registerBundledRedstoneProvider( @Nonnull IBundledRedstoneProvider handler )
    {
        IComputerCraftAPI api = tryLoadCC();
        if( api != null ) api.registerBundledRedstoneProvider( handler );
    }

    /**
     * If there is a Computer or Turtle at a certain position in the world, get it's bundled redstone output.
     *
     * @param world The world this block is in.
     * @param pos   The position this block is at.
     * @param side  The side to extract the bundled redstone output from.
     * @return If there is a block capable of emitting bundled redstone at the location, it's signal (0-65535) will be returned.
     * If there is no block capable of emitting bundled redstone at the location, -1 will be returned.
     * @see dan200.computercraft.api.redstone.IBundledRedstoneProvider
     */
    public static int getBundledRedstoneOutput( @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side )
    {
        IComputerCraftAPI api = tryLoadCC();
        return api != null ? api.getBundledRedstoneOutput( world, pos, side ) : -1;
    }

    /**
     * Registers a media handler to provide {@link IMedia} implementations for Items
     *
     * @param handler The media provider to register.
     * @see dan200.computercraft.api.media.IMediaProvider
     */
    public static void registerMediaProvider( @Nonnull IMediaProvider handler )
    {
        IComputerCraftAPI api = tryLoadCC();
        if( api != null ) api.registerMediaProvider( handler );
    }

    /**
     * Registers a permission handler to restrict where turtles can move or build.
     *
     * @param handler The turtle permission provider to register.
     * @see dan200.computercraft.api.permissions.ITurtlePermissionProvider
     */
    public static void registerPermissionProvider( @Nonnull ITurtlePermissionProvider handler )
    {
        IComputerCraftAPI api = tryLoadCC();
        if( api != null ) api.registerPermissionProvider( handler );
    }

    public static void registerPocketUpgrade( @Nonnull IPocketUpgrade upgrade )
    {
        IComputerCraftAPI api = tryLoadCC();
        if( api != null ) api.registerPocketUpgrade( upgrade );
    }

    /**
     * Attempt to get the game-wide wireless network.
     *
     * @return The global wireless network, or {@code null} if it could not be fetched.
     */
    @Nullable
    public static IPacketNetwork getWirelessNetwork()
    {
        IComputerCraftAPI api = tryLoadCC();
        return api != null ? api.getWirelessNetwork() : null;
    }

    // The functions below here are private, and are used to interface with the non-API ComputerCraft classes.
    // Reflection is used here so you can develop your mod without decompiling ComputerCraft and including
    // it in your solution, and so your mod won't crash if ComputerCraft is installed.

    private static boolean ccSearched = false;
    private static IComputerCraftAPI ccAPI = null;

    @Nullable
    private static IComputerCraftAPI tryLoadCC()
    {
        if( !ccSearched )
        {
            try
            {
                ccAPI = (IComputerCraftAPI) Class.forName( "dan200.computercraft.ComputerCraftAPIImpl" )
                    .getField( "INSTANCE" ).get( null );
            }
            catch( Exception e )
            {
                System.out.println( "ComputerCraftAPI: ComputerCraft not found." );
            }
            finally
            {
                ccSearched = true;
            }
        }

        return ccAPI;
    }

    /**
     * The interface that will be implemented by CC. You should not need to access
     * this yourself.
     */
    public interface IComputerCraftAPI
    {
        @Nonnull
        String getInstalledVersion();

        int createUniqueNumberedSaveDir( @Nonnull World world, @Nonnull String parentSubPath );

        @Nullable
        IWritableMount createSaveDirMount( @Nonnull World world, @Nonnull String subPath, long capacity );

        @Nullable
        IMount createResourceMount( @Nonnull Class<?> modClass, @Nonnull String domain, @Nonnull String subPath );

        void registerPeripheralProvider( @Nonnull IPeripheralProvider handler );

        void registerTurtleUpgrade( @Nonnull ITurtleUpgrade upgrade );

        void registerBundledRedstoneProvider( @Nonnull IBundledRedstoneProvider handler );

        int getBundledRedstoneOutput( @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side );

        void registerMediaProvider( @Nonnull IMediaProvider handler );

        void registerPermissionProvider( @Nonnull ITurtlePermissionProvider handler );

        void registerPocketUpgrade( @Nonnull IPocketUpgrade upgrade );

        IPacketNetwork getWirelessNetwork();
    }
}

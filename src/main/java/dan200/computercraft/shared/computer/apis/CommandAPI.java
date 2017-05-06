/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.apis;

import com.google.common.collect.ImmutableMap;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaTask;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.shared.computer.blocks.TileCommandComputer;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class CommandAPI implements ILuaAPI
{
    private TileCommandComputer m_computer;

    public CommandAPI( TileCommandComputer computer )
    {
        m_computer = computer;
    }

    // ILuaAPI implementation

    @Override
    public String[] getNames()
    {
        return new String[] {
            "commands"
        };
    }

    @Override
    public void startup()
    {
    }

    @Override
    public void advance( double dt )
    {
    }

    @Override
    public void shutdown()
    {
    }

    @Override
    public String[] getMethodNames()
    {
        return new String[] {
            "exec",
            "execAsync",
            "list",
            "getBlockPosition",
            "getBlockInfos",
            "getBlockInfo"
        };
    }

    private Map<Object, Object> createOutput( String output )
    {
        Map<Object, Object> result = new HashMap<Object, Object>( 1 );
        result.put( 1, output );
        return result;
    }

    private Object[] doCommand( String command )
    {
        MinecraftServer server = m_computer.getWorld().getMinecraftServer();
        if( server != null && server.isCommandBlockEnabled() )
        {
            ICommandManager commandManager = server.getCommandManager();
            try
            {
                TileCommandComputer.CommandSender sender = m_computer.getCommandSender();
                sender.clearOutput();

                int result = commandManager.executeCommand( sender, command );
                return new Object[]{ (result > 0), sender.copyOutput() };
            }
            catch( Throwable t )
            {
                return new Object[]{ false, createOutput( "Java Exception Thrown: " + t.toString() ) };
            }
        }
        else
        {
            return new Object[] { false, createOutput( "Command blocks disabled by server" ) };
        }
    }

    private Object getBlockInfo( World world, BlockPos pos )
    {
        // Get the details of the block
        IBlockState state = world.getBlockState( pos );
        Block block = state.getBlock();
        String name = ((ResourceLocation)Block.REGISTRY.getNameForObject( block )).toString();
        int metadata = block.getMetaFromState( state );

        Map<Object, Object> table = new HashMap<Object, Object>();
        table.put( "name", name );
        table.put( "metadata", metadata );

        Map<Object, Object> stateTable = new HashMap<Object, Object>();
        for( Object o : state.getActualState( world, pos ).getProperties().entrySet() )
        {
            ImmutableMap.Entry<IProperty, Object> entry = (ImmutableMap.Entry<IProperty, Object>)o;
            String propertyName = entry.getKey().getName();
            Object value = entry.getValue();
            if( value instanceof String || value instanceof Number || value instanceof Boolean )
            {
                stateTable.put( propertyName, value );
            }
            else
            {
                stateTable.put( propertyName, value.toString() );
            }
        }
        table.put( "state", stateTable );
        // TODO: NBT data?
        return table;
    }

    @Override
    public Object[] callMethod( ILuaContext context, int method, Object[] arguments ) throws LuaException, InterruptedException
    {
        switch( method )
        {
            case 0:
            {
                // exec
                if( arguments.length < 1 || !(arguments[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                final String command = (String)arguments[0];
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        return doCommand( command );
                    }
                } );
            }
            case 1:
            {
                // execAsync
                if( arguments.length < 1 || !(arguments[0] instanceof String) )
                {
                    throw new LuaException( "Expected string" );
                }
                final String command = (String)arguments[0];
                long taskID = context.issueMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        return doCommand( command );
                    }
                } );
                return new Object[] { taskID };
            }
            case 2:
            {
                // list
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        int i = 1;
                        Map<Object, Object> result = new HashMap<Object, Object>();
                        MinecraftServer server = m_computer.getWorld().getMinecraftServer();
                        if( server != null )
                        {
                            ICommandManager commandManager = server.getCommandManager();
                            ICommandSender commmandSender = m_computer.getCommandSender();
                            Map commands = commandManager.getCommands();
                            for( Object entryObject : commands.entrySet() )
                            {
                                Map.Entry entry = (Map.Entry)entryObject;
                                String name = (String)entry.getKey();
                                ICommand command = (ICommand)entry.getValue();
                                try
                                {
                                    if( command.checkPermission( server, commmandSender ) )
                                    {
                                        result.put( i++, name );
                                    }
                                }
                                catch( Throwable t )
                                {
                                    // Ignore buggy command
                                }
                            }
                        }
                        return new Object[]{ result };
                    }
                } );
            }
            case 3:
            {
                // getBlockPosition
                // This is probably safe to do on the Lua thread. Probably.
                BlockPos pos = m_computer.getPos();
                return new Object[] { pos.getX(), pos.getY(), pos.getZ() };
            }
            case 4:
            {
                // getBlockInfos
                if( arguments.length < 6 ||
                    !(arguments[0] instanceof Number) ||
                    !(arguments[1] instanceof Number) ||
                    !(arguments[2] instanceof Number) ||
                    !(arguments[3] instanceof Number) ||
                    !(arguments[4] instanceof Number) ||
                    !(arguments[5] instanceof Number) )
                {
                    throw new LuaException( "Expected number, number, number, number, number, number" );
                }
                final int minx = ((Number)arguments[0]).intValue();
                final int miny = ((Number)arguments[1]).intValue();
                final int minz = ((Number)arguments[2]).intValue();
                final int maxx = ((Number)arguments[3]).intValue();
                final int maxy = ((Number)arguments[4]).intValue();
                final int maxz = ((Number)arguments[5]).intValue();
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        // Get the details of the block
                        World world = m_computer.getWorld();
                        BlockPos min = new BlockPos(
                                Math.min( minx, maxx ),
                                Math.min( miny, maxy ),
                                Math.min( minz, maxz )
                        );
                        BlockPos max = new BlockPos(
                                Math.max( minx, maxx ),
                                Math.max( miny, maxy ),
                                Math.max( minz, maxz )
                        );
                        if( !WorldUtil.isBlockInWorld( world, min ) || !WorldUtil.isBlockInWorld( world, max ) )
                        {
                            throw new LuaException( "Co-ordinates out or range" );
                        }
                        if( ( max.getX() - min.getX() + 1 ) * ( max.getY() - min.getY() + 1 ) * ( max.getZ() - min.getZ() + 1 ) > 4096 )
                        {
                            throw new LuaException( "Too many blocks" );
                        }
                        int i=1;
                        Map<Object, Object> results = new HashMap<Object, Object>();
                        for( int y=min.getY(); y<= max.getY(); ++y )
                        {
                            for( int z = min.getZ(); z <= max.getZ(); ++z )
                            {
                                for( int x = min.getX(); x <= max.getX(); ++x )
                                {
                                    BlockPos pos = new BlockPos( x, y, z );
                                    results.put( i++, getBlockInfo( world, pos ) );
                                }
                            }
                        }
                        return new Object[]{ results };
                    }
                } );
            }
            case 5:
            {
                // getBlockInfo
                if( arguments.length < 3 ||
                        !(arguments[0] instanceof Number) ||
                        !(arguments[1] instanceof Number) ||
                        !(arguments[2] instanceof Number) )
                {
                    throw new LuaException( "Expected number, number, number" );
                }
                final int x = ((Number)arguments[0]).intValue();
                final int y = ((Number)arguments[1]).intValue();
                final int z = ((Number)arguments[2]).intValue();
                return context.executeMainThreadTask( new ILuaTask()
                {
                    @Override
                    public Object[] execute() throws LuaException
                    {
                        // Get the details of the block
                        World world = m_computer.getWorld();
                        BlockPos position = new BlockPos( x, y, z );
                        if( WorldUtil.isBlockInWorld( world, position ) )
                        {
                            return new Object[]{ getBlockInfo( world, position ) };
                        }
                        else
                        {
                            throw new LuaException( "co-ordinates out or range" );
                        }
                    }
                } );
            }
            default:
            {
                return null;
            }
        }
    }
}

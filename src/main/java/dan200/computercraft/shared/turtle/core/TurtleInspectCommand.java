/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import com.google.common.collect.ImmutableMap;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class TurtleInspectCommand implements ITurtleCommand
{
    private static final boolean FAIL_ON_AIR = true;
    private final InteractDirection m_direction;

    public TurtleInspectCommand( InteractDirection direction )
    {
        m_direction = direction;
    }

    @Override
    public TurtleCommandResult execute( ITurtleAccess turtle )
    {
        // Get world direction from direction
        EnumFacing direction = m_direction.toWorldDir( turtle );

        // Check if thing in front is air or not
        World world = turtle.getWorld();
        BlockPos oldPosition = turtle.getPosition();
        BlockPos newPosition = WorldUtil.moveCoords( oldPosition, direction );

        if( WorldUtil.isBlockInWorld( world, newPosition ) )
        {
            if( !FAIL_ON_AIR || !world.isAirBlock( newPosition ) )
            {
                IBlockState state = world.getBlockState( newPosition );
                Block block = state.getBlock();
                String name = ((ResourceLocation)Block.REGISTRY.getNameForObject( block )).toString();
                int metadata = block.getMetaFromState( state );

                Map<Object, Object> table = new HashMap<Object, Object>();
                table.put( "name", name );
                table.put( "metadata", metadata );

                Map<Object, Object> stateTable = new HashMap<Object, Object>();
                for( Object o : state.getActualState( world, newPosition ).getProperties().entrySet() )
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

                return TurtleCommandResult.success( new Object[]{ table } );
            }
        }

        if( !FAIL_ON_AIR )
        {
            Map<Object, Object> table = new HashMap<Object, Object>();
            table.put( "name", "minecraft:air" );
            table.put( "metadata", 0 );
            table.put( "state", new HashMap<Object, Object>() );
            return TurtleCommandResult.success( new Object[]{ table } );
        }
        return TurtleCommandResult.failure( "No block to inspect" );
    }
}

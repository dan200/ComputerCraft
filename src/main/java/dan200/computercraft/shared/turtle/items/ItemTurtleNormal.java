/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

public class ItemTurtleNormal extends ItemTurtleBase
{
    public ItemTurtleNormal( Block block )
    {
        super( block );
        setUnlocalizedName( "computercraft:turtle" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    @Override
    public ItemStack create( int id, String label, Colour colour, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, int fuelLevel, ResourceLocation overlay )
    {
        // Build the stack
        ItemStack stack = new ItemStack( this, 1, 0 );
        NBTTagCompound nbt = new NBTTagCompound();
        if( leftUpgrade != null )
        {
            int leftUpgradeLegacyID = leftUpgrade.getLegacyUpgradeID();
            if( leftUpgradeLegacyID >= 0 )
            {
                nbt.setShort( "leftUpgrade", (short)leftUpgradeLegacyID );
            }
            else
            {
                nbt.setString( "leftUpgrade", leftUpgrade.getUpgradeID().toString() );
            }
        }
        if( rightUpgrade != null )
        {
            int rightUpgradeLegacyID = rightUpgrade.getLegacyUpgradeID();
            if( rightUpgradeLegacyID >= 0 )
            {
                nbt.setShort( "rightUpgrade", (short)rightUpgradeLegacyID );
            }
            else
            {
                nbt.setString( "rightUpgrade", rightUpgrade.getUpgradeID().toString() );
            }
        }
        if( id >= 0 )
        {
            nbt.setInteger( "computerID", id );
        }
        if( fuelLevel > 0 )
        {
            nbt.setInteger( "fuelLevel", fuelLevel );
        }
        if( colour != null )
        {
            nbt.setInteger( "colourIndex", colour.ordinal() );
        }
        if( overlay != null )
        {
            nbt.setString( "overlay_mod", overlay.getResourceDomain() );
            nbt.setString( "overlay_path", overlay.getResourcePath() );
        }
        stack.setTagCompound( nbt );

        // Return the stack
        if( label != null )
        {
            stack.setStackDisplayName( label );
        }
        return stack;
    }

    // IComputerItem implementation

    @Override
    public int getComputerID( ItemStack stack )
    {
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if( nbt.hasKey( "computerID" ) )
            {
                return nbt.getInteger( "computerID" );
            }
        }
        return -1;
    }

    @Override
    public ComputerFamily getFamily( int damage )
    {
        return ComputerFamily.Normal;
    }

    // ITurtleItem implementation

    @Override
    public ITurtleUpgrade getUpgrade( ItemStack stack, TurtleSide side )
    {
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            switch( side )
            {
                case Left:
                {
                    if( nbt.hasKey( "leftUpgrade" ) )
                    {
                        if( nbt.getTagId( "leftUpgrade" ) == Constants.NBT.TAG_STRING )
                        {
                            return ComputerCraft.getTurtleUpgrade( nbt.getString( "leftUpgrade" ) );
                        }
                        else
                        {
                            return ComputerCraft.getTurtleUpgrade( nbt.getShort( "leftUpgrade" ) );
                        }
                    }
                    break;
                }
                case Right:
                {
                    if( nbt.hasKey( "rightUpgrade" ) )
                    {
                        if( nbt.getTagId( "rightUpgrade" ) == Constants.NBT.TAG_STRING )
                        {
                            return ComputerCraft.getTurtleUpgrade( nbt.getString( "rightUpgrade" ) );
                        }
                        else
                        {
                            return ComputerCraft.getTurtleUpgrade( nbt.getShort( "rightUpgrade" ) );
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public Colour getColour( ItemStack stack )
    {
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if( nbt.hasKey( "colourIndex" ) )
            {
                int index = nbt.getInteger( "colourIndex" ) & 0xf;
                return Colour.values()[ index ];
            }
        }
        return null;
    }

    @Override
    public ResourceLocation getOverlay( ItemStack stack )
    {
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if( nbt.hasKey( "overlay_mod" ) && nbt.hasKey( "overlay_path" ) )
            {
                String overlay_mod = nbt.getString( "overlay_mod" );
                String overlay_path = nbt.getString( "overlay_path" );
                return new ResourceLocation( overlay_mod, overlay_path );
            }
        }
        return null;
    }

    @Override
    public int getFuelLevel( ItemStack stack )
    {
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if( nbt.hasKey( "fuelLevel" ) )
            {
                return nbt.getInteger( "fuelLevel" );
            }
        }
        return 0;
    }
}

/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ItemComputerBase;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemTurtleBase extends ItemComputerBase implements ITurtleItem
{
    protected ItemTurtleBase( Block block )
    {
        super( block );
        setMaxStackSize( 64 );
        setHasSubtypes( true );
    }

    public abstract ItemStack create( int id, String label, Colour colour, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, int fuelLevel, ResourceLocation overlay );

    @Override
    public void getSubItems( Item itemID, CreativeTabs tabs, List list )
    {
        List<ItemStack> all = new ArrayList<ItemStack>();
        ComputerCraft.addAllUpgradedTurtles( all );
        for( ItemStack stack : all )
        {
            if( stack.getItem() == this )
            {
                list.add( stack );
            }
        }
    }

    @Override
    public boolean placeBlockAt( ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState )
    {
        if( super.placeBlockAt( stack, player, world, pos, side, hitX, hitY, hitZ, newState ) )
        {
            TileEntity tile = world.getTileEntity( pos );
            if( tile != null && tile instanceof ITurtleTile )
            {
                ITurtleTile turtle = (ITurtleTile)tile;
                setupTurtleAfterPlacement( stack, turtle );
            }
            return true;
        }
        return false;
    }

    public void setupTurtleAfterPlacement( ItemStack stack, ITurtleTile turtle )
    {
        // Set ID
        int id = getComputerID( stack );
        if( id >= 0 )
        {
            turtle.setComputerID( id );
        }

        // Set Label
        String label = getLabel( stack );
        if( label != null )
        {
            turtle.setLabel( label );
        }

        // Set Upgrades
        for( TurtleSide side : TurtleSide.values() )
        {
            turtle.getAccess().setUpgrade( side, getUpgrade( stack, side ) );
        }

        // Set Fuel level
        int fuelLevel = getFuelLevel( stack );
        turtle.getAccess().setFuelLevel( fuelLevel );

        // Set colour
        Colour colour = getColour( stack );
        if( colour != null )
        {
            turtle.getAccess().setDyeColour( colour.ordinal() );
        }

        // Set overlay
        ResourceLocation overlay = getOverlay( stack );
        if( overlay != null )
        {
            ((TurtleBrain)turtle.getAccess()).setOverlay( overlay );
        }
    }

    @Override
    public String getUnlocalizedName( ItemStack stack )
    {
        ComputerFamily family = getFamily( stack );
        switch( family )
        {
            case Normal:
            default:
            {
                return "tile.computercraft:turtle";
            }
            case Advanced:
            {
                return "tile.computercraft:advanced_turtle";
            }
            case Beginners:
            {
                return "tile.computercraftedu:beginner_turtle";
            }
        }
    }

    @Override
    public String getItemStackDisplayName( ItemStack stack )
    {
        String baseString = getUnlocalizedName( stack );
        ITurtleUpgrade left = getUpgrade( stack, TurtleSide.Left );
        ITurtleUpgrade right = getUpgrade( stack, TurtleSide.Right );
        if( left != null && right != null )
        {
            return I18n.translateToLocalFormatted(
                baseString + ".upgraded_twice.name",
                I18n.translateToLocal( right.getUnlocalisedAdjective() ),
                I18n.translateToLocal( left.getUnlocalisedAdjective() )
            );
        }
        else if( left != null )
        {
            return I18n.translateToLocalFormatted(
                baseString + ".upgraded.name",
                I18n.translateToLocal( left.getUnlocalisedAdjective() )
            );
        }
        else if( right != null )
        {
            return I18n.translateToLocalFormatted(
                baseString + ".upgraded.name",
                I18n.translateToLocal( right.getUnlocalisedAdjective() )
            );
        }
        else
        {
            return I18n.translateToLocal( baseString + ".name" );
        }
    }

    // ITurtleItem implementation

    @Override
    public abstract ITurtleUpgrade getUpgrade( ItemStack stack, TurtleSide side );

    @Override
    public abstract Colour getColour( ItemStack stack );

    @Override
    public abstract ResourceLocation getOverlay( ItemStack stack );

    @Override
    public abstract int getFuelLevel( ItemStack stack );
}

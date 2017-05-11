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
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ItemTurtleLegacy extends ItemTurtleBase
{
    public ItemTurtleLegacy( Block block )
    {
        super( block );
        setUnlocalizedName( "computercraft:turtle" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    @Override
    public ItemStack create( int id, String label, Colour colour, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, int fuelLevel, ResourceLocation overlay )
    {
        // Legacy turtles only support pickaxes and modems
        if( (leftUpgrade != null && leftUpgrade != ComputerCraft.Upgrades.diamondPickaxe ) ||
            (rightUpgrade != null && rightUpgrade != ComputerCraft.Upgrades.wirelessModem) ||
            (colour != null) || (overlay != null) )
        {
            return null;
        }

        // Build the subtype
        int subType = 0;
        if( leftUpgrade != null )
        {
            subType = subType + 1;
        }
        if( rightUpgrade != null )
        {
            subType = subType + 2;
        }

        // Build the ID
        int damage = subType;
        if( id >= 0 && id <= ItemComputer.HIGHEST_DAMAGE_VALUE_ID )
        {
            damage += ((id + 1) << 2);
        }

        // Build the stack
        ItemStack stack = new ItemStack( this, 1, damage );
        if( fuelLevel > 0 || id > ItemComputer.HIGHEST_DAMAGE_VALUE_ID )
        {
            NBTTagCompound nbt = new NBTTagCompound();
            if( fuelLevel > 0 )
            {
                nbt.setInteger( "fuelLevel", fuelLevel );
            }
            if( id > ItemComputer.HIGHEST_DAMAGE_VALUE_ID )
            {
                nbt.setInteger( "computerID", id );
            }
            stack.setTagCompound( nbt );
        }
        if( label != null )
        {
            stack.setStackDisplayName( label );
        }

        // Return the stack
        return stack;
    }

    // IComputerItem implementation

    @Override
    public int getComputerID( ItemStack stack )
    {
        if( stack.hasTagCompound() && stack.getTagCompound().hasKey( "computerID" ) )
        {
            return  stack.getTagCompound().getInteger( "computerID" );
        }
        else
        {
            int damage = stack.getItemDamage();
            return ( ( damage & 0xfffc ) >> 2 ) - 1;
        }
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
        int damage = stack.getItemDamage();
        switch( side )
        {
            case Left:
            {
                if( (damage & 0x1) > 0 )
                {
                    return ComputerCraft.Upgrades.diamondPickaxe;
                }
                break;
            }
            case Right:
            {
                if( (damage & 0x2) > 0 )
                {
                    return ComputerCraft.Upgrades.wirelessModem;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Colour getColour( ItemStack stack )
    {
        return null;
    }

    @Override
    public ResourceLocation getOverlay( ItemStack stack ) { return null; }

    @Override
    public int getFuelLevel( ItemStack stack )
    {
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            return nbt.getInteger("fuelLevel");
        }
        return 0;
    }
}


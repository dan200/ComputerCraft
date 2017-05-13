/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.core;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TurtlePlayer extends FakePlayer
{
    private final static GameProfile s_profile = new GameProfile(
        UUID.fromString( "0d0c4ca0-4ff1-11e4-916c-0800200c9a66" ),
        "ComputerCraft"
    );

    public TurtlePlayer( WorldServer world )
    {
        super( world, s_profile );
    }

    public void loadInventory( ItemStack currentStack )
    {
        // Load up the fake inventory
        inventory.currentItem = 0;
        inventory.setInventorySlotContents( 0, currentStack );
    }

    public ItemStack unloadInventory( ITurtleAccess turtle )
    {
        // Get the item we placed with
        ItemStack results = inventory.getStackInSlot( 0 );
        inventory.setInventorySlotContents( 0, null );

        // Store (or drop) anything else we found
        BlockPos dropPosition = turtle.getPosition();
        EnumFacing dropDirection = turtle.getDirection().getOpposite();
        for( int i=0; i<inventory.getSizeInventory(); ++i )
        {
            ItemStack stack = inventory.getStackInSlot( i );
            if( stack != null )
            {
                ItemStack remainder = InventoryUtil.storeItems( stack, turtle.getItemHandler(), turtle.getSelectedSlot() );
                if( remainder != null )
                {
                    WorldUtil.dropItemStack( remainder, turtle.getWorld(), dropPosition, dropDirection );
                }
                inventory.setInventorySlotContents( i, null );
            }
        }
        inventory.markDirty();
        return results;
    }

    @Override
    public float getEyeHeight()
    {
        return 0.0f;
    }

    @Override
    public float getDefaultEyeHeight()
    {
        return 0.0f;
    }

    @Override
    public void mountEntityAndWakeUp()
    {
    }

    @Override
    public void dismountEntity( @Nonnull Entity entity )
    {
    }

    @Override
    public void openEditSign( TileEntitySign signTile )
    {
    }
}

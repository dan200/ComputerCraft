/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public abstract class ItemComputerBase extends ItemBlock implements IComputerItem, IMedia
{
    protected ItemComputerBase( Block block )
    {
        super( block );
    }

    public abstract ComputerFamily getFamily( int damage );

    @Override
    public final int getMetadata( int damage )
    {
        return damage;
    }

    @Override
    public void addInformation( ItemStack stack, EntityPlayer player, List list, boolean debug )
    {
        if( debug )
        {
            int id = getComputerID( stack );
            if( id >= 0 )
            {
                list.add( "(Computer ID: " + id + ")" );
            }
        }
    }

    // IComputerItem implementation

    @Override
    public abstract int getComputerID( ItemStack stack );

    @Override
    public String getLabel( ItemStack stack )
    {
        if( stack.hasDisplayName() )
        {
            return stack.getDisplayName();
        }
        return null;
    }

    @Override
    public final ComputerFamily getFamily( ItemStack stack )
    {
        int damage = stack.getItemDamage();
        return getFamily( damage );
    }

    // IMedia implementation

    @Override
    public boolean setLabel( ItemStack stack, String label )
    {
        if( label != null )
        {
            stack.setStackDisplayName( label );
        }
        else
        {
            stack.clearCustomName();
        }
        return true;
    }

    @Override
    public String getAudioTitle( ItemStack stack )
    {
        return null;
    }

    @Override
    public String getAudioRecordName( ItemStack stack )
    {
        return null;
    }

    @Override
    public IMount createDataMount( ItemStack stack, World world )
    {
        ComputerFamily family = getFamily( stack );
        if( family != ComputerFamily.Command )
        {
            int id = getComputerID( stack );
            if( id >= 0 )
            {
                return ComputerCraft.createSaveDirMount( world, "computer/" + id, ComputerCraft.computerSpaceLimit );
            }
        }
        return null;
    }
}

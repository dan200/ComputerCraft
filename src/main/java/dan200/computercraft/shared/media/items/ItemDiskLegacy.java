/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.shared.common.IColouredItem;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemDiskLegacy extends Item
    implements IMedia, IColouredItem
{
    public ItemDiskLegacy()
    {
        setMaxStackSize( 1 );
        setHasSubtypes( true );
        setUnlocalizedName( "computercraft:disk" );
        setCreativeTab( ComputerCraft.mainCreativeTab  );
    }
    
    @Override
    public void getSubItems( @Nonnull Item itemID, CreativeTabs tabs, List<ItemStack> list )
    {
        for( int colour=0; colour<16; ++colour )
        {
            ItemStack stack = createFromIDAndColour( -1, null, Colour.values()[ colour ].getHex() );
            if( stack.getItem() == this )
            {
                list.add( stack );
            }
        }
    }
            
    public static ItemStack createFromIDAndColour( int id, String label, int colour )
    {
        if( colour != Colour.Blue.getHex() )
        {
            return ItemDiskExpanded.createFromIDAndColour( id, label, colour );
        }
        
        ItemStack stack = new ItemStack( ComputerCraft.Items.disk, 1 );
        ComputerCraft.Items.disk.setDiskID( stack, id );
        ComputerCraft.Items.disk.setLabel( stack, label );
        return stack;
    }
    
    public int getDiskID( ItemStack stack )
    {
        int damage = stack.getItemDamage();
        if( damage > 0 )
        {
            return damage;
        }
        return -1;
    }

    protected void setDiskID( ItemStack stack, int id )
    {
        if( id > 0 ) {
            stack.setItemDamage( id );
        } else {
            stack.setItemDamage( 0 );
        }
    }

    @Override
    public void addInformation( ItemStack stack, EntityPlayer player, List<String> list, boolean debug )
    {
        if( debug )
        {
            int id = getDiskID( stack );
            if( id >= 0 )
            {
                list.add( "(Disk ID: " + id + ")" );
            }
        }
    }

    // IMedia implementation

    @Override
    public String getLabel( @Nonnull ItemStack stack )
    {
        if( stack.hasDisplayName() )
        {
            return stack.getDisplayName();
        }
        return null;
    }
    
    @Override
    public boolean setLabel( @Nonnull ItemStack stack, String label )
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
    public String getAudioTitle( @Nonnull ItemStack stack )
    {
        return null;
    }
    
    @Override
    public SoundEvent getAudio( @Nonnull ItemStack stack )
    {
        return null;
    }
    
    @Override
    public IMount createDataMount( @Nonnull ItemStack stack, @Nonnull World world )
    {
        int diskID = getDiskID( stack );
        if( diskID < 0 )
        {
            diskID = ComputerCraft.createUniqueNumberedSaveDir( world, "computer/disk" );
            setDiskID( stack, diskID );
        }
        return ComputerCraftAPI.createSaveDirMount( world, "computer/disk/" + diskID, ComputerCraft.floppySpaceLimit );
    }

    @Override
    public int getColour( ItemStack stack )
    {
        return Colour.Blue.getHex();
    }

    @Override
    public boolean doesSneakBypassUse( ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player )
    {
        return true;
    }

    @Override
    public ItemStack setColour( ItemStack stack, int colour )
    {
        return ItemDiskExpanded.createFromIDAndColour( getDiskID( stack ), getLabel( stack ), colour );
    }
}

/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.core.filesystem.SubMount;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class ItemTreasureDisk extends Item
    implements IMedia
{    
    public ItemTreasureDisk()
    {
        setMaxStackSize( 1 );
        setHasSubtypes( true );
        setUnlocalizedName( "computercraft:treasure_disk" );
    }
    
    @Override
    public void getSubItems( @Nonnull Item itemID, CreativeTabs tabs, List<ItemStack> list )
    {
    }
    
    @Override
    public void addInformation( ItemStack stack, EntityPlayer player, List<String> list, boolean bool )
    {
        String label = getTitle( stack );
        if( label != null && label.length() > 0 )
        {
            list.add( label );
        }
    }

    @Override
    public boolean doesSneakBypassUse( ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player )
    {
        return true;
    }
    
    // IMedia implementation

    @Override
    public String getLabel( @Nonnull ItemStack stack )
    {
        return getTitle( stack );
    }
    
    @Override
    public boolean setLabel( @Nonnull ItemStack stack, String label )
    {
        return false;
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
        IMount rootTreasure = getTreasureMount();
        String subPath = getSubPath( stack );
        try
        {
            if( rootTreasure.exists( subPath ) )
            {
                return new SubMount( rootTreasure, subPath );
            }
            else if( rootTreasure.exists( "deprecated/" + subPath ) )
            {
                return new SubMount( rootTreasure, "deprecated/" + subPath );
            }
            else
            {
                return null;
            }
        }
        catch( IOException e )
        {
            return null;
        }
    }
    
    public static ItemStack create( String subPath, int colourIndex )
    {    
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString( "subPath", subPath );
        
        int slash = subPath.indexOf( "/" );
        if( slash >= 0 )
        {
            String author = subPath.substring( 0, slash );
            String title = subPath.substring( slash + 1 );
            nbt.setString( "title", "\"" + title + "\" by " + author );
        }
        else
        {
            nbt.setString( "title", "untitled" );
        }
        nbt.setInteger( "colour", Colour.values()[ colourIndex ].getHex() );
        
        ItemStack result = new ItemStack( ComputerCraft.Items.treasureDisk, 1, 0 );
        result.setTagCompound( nbt );
        return result;
    }

    private static IMount getTreasureMount()
    {
        return ComputerCraft.createResourceMount( ComputerCraft.class, "computercraft", "lua/treasure" );
    }

    // private stuff
    
    public String getTitle( ItemStack stack )
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if( nbt != null && nbt.hasKey( "title" ) )
        {
            return nbt.getString( "title" );
        }
        return "'alongtimeago' by dan200";
    }
    
    public String getSubPath( ItemStack stack )
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if( nbt != null && nbt.hasKey( "subPath" ) )
        {
            return nbt.getString( "subPath" );
        }
        return "dan200/alongtimeago";
    }
    
    public int getColour( ItemStack stack )
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if( nbt != null && nbt.hasKey( "colour" ) )
        {
            return nbt.getInteger( "colour" );
        }
        return Colour.Blue.getHex();
    }
}

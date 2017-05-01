/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemTreasureDisk extends Item
	implements IMedia
{	
    private static ItemStack[] s_treasureItems = null;

    public ItemTreasureDisk()
    {
        setMaxStackSize( 1 );
		setHasSubtypes( true );
		setUnlocalizedName( "computercraft:treasure_disk" );
    }
    
	@Override
    public void getSubItems( Item itemID, CreativeTabs tabs, List list )
    {
        if( s_treasureItems != null )
        {
            Collections.addAll( list, s_treasureItems );
        }
    }
    
	@Override
    public void addInformation( ItemStack stack, EntityPlayer player, List list, boolean bool )
    {
		String label = getTitle( stack );
		if( label != null && label.length() > 0 )
		{
			list.add( label );
		}
    }
        	
	@Override
    public int getColorFromItemStack( ItemStack stack, int pass )
    {
        return pass == 0 ? 0xffffff : getColour(stack);
    }

	@Override
    public boolean doesSneakBypassUse( World world, BlockPos pos, EntityPlayer player )
    {
        return true;
    }
    
	// IMedia implementation

    @Override
	public String getLabel( ItemStack stack )
	{
		return getTitle( stack );
	}
	
    @Override
	public boolean setLabel( ItemStack stack, String label )
	{
		return false;
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

    public static void registerDungeonLoot()
    {
        if( s_treasureItems == null )
        {
            // Get the list of all programs
            List<String> paths = new ArrayList<String>();
            try
            {
                IMount treasure = getTreasureMount();
                if( treasure != null )
                {
                    List<String> authors = new ArrayList<String>();
                    treasure.list( "", authors );
                    for( String author : authors )
                    {
                        if( treasure.isDirectory( author ) && !author.equals( "deprecated" ) )
                        {
                            List<String> titles = new ArrayList<String>();
                            treasure.list( author, titles );
                            for( String title : titles )
                            {
                                String path = author + "/" + title;
                                if( treasure.isDirectory( path ) )
                                {
                                    paths.add( path );
                                }
                            }
                        }
                    }
                }
            }
            catch( java.io.IOException e )
            {
                // no items for us
            }

            // Build creative tab
            List<ItemStack> allTreasure = new ArrayList<ItemStack>();
            for( String path : paths )
            {
                ItemStack stack = create( path, 4 );
                allTreasure.add( stack );
            }
            s_treasureItems = allTreasure.toArray( new ItemStack[ allTreasure.size() ] );

            // Register the loot
            int n=0;
            Random random = new Random();
            WeightedRandomChestContent[] content = new WeightedRandomChestContent[ paths.size() * ComputerCraft.treasureDiskLootFrequency ];
            WeightedRandomChestContent[] commonContent = new WeightedRandomChestContent[ paths.size() * ComputerCraft.treasureDiskLootFrequency ];
            for( String path : paths )
            {
                // Don't use all the random colours
                // We don't want to overload the probability matrix
                for( int i=0; i<ComputerCraft.treasureDiskLootFrequency; ++i )
                {
                    ItemStack stack = create( path, random.nextInt( 16 ) );
                    content[ n ] = new WeightedRandomChestContent( stack, 1, 1, 1 );
                    commonContent[ n ] = new WeightedRandomChestContent( stack, 1, 1, 2 );
                    n++;
                }
            }
            registerLoot( ChestGenHooks.DUNGEON_CHEST, content );
            registerLoot( ChestGenHooks.MINESHAFT_CORRIDOR, content );
            registerLoot( ChestGenHooks.STRONGHOLD_CORRIDOR, content );
            registerLoot( ChestGenHooks.STRONGHOLD_CROSSING, content );
            registerLoot( ChestGenHooks.STRONGHOLD_LIBRARY, commonContent );
            registerLoot( ChestGenHooks.PYRAMID_DESERT_CHEST, content );
            registerLoot( ChestGenHooks.PYRAMID_JUNGLE_CHEST, content );
        }
	}
	
	private static void registerLoot( String category, WeightedRandomChestContent[] content )
	{
		for( int i=0; i<content.length; ++i )
		{
			ChestGenHooks.getInfo( category ).addItem( content[i] );
		}
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

/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.media.recipes;

import dan200.computercraft.shared.media.items.ItemDiskLegacy;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DiskRecipe implements IRecipe
{
    public DiskRecipe()
    {
    }

    @Override
    public boolean matches( @Nonnull InventoryCrafting inventory, @Nonnull World world )
    {
        boolean diskFound = false;
        boolean paperFound = false;
        boolean redstoneFound = false;
        boolean dyeFound = false;

        for (int var5 = 0; var5 < inventory.getSizeInventory(); ++var5)
        {
            ItemStack var6 = inventory.getStackInSlot(var5);

            if (var6 != null)
            {
                if (var6.getItem() instanceof ItemDiskLegacy )
                {
                    if (diskFound || redstoneFound || paperFound) // make sure no redstone or paper already accepted if disk there
                    {
                        return false;
                    }

                    diskFound = true;
                }
                else if( var6.getItem() == Items.DYE )
                {
                    dyeFound = true;
                }
                else if( var6.getItem() == Items.PAPER )
                {
                    if(paperFound || diskFound)
                    {
                        return false;
                    }
                    paperFound = true;
                }
                else if (var6.getItem() == Items.REDSTONE)
                {
                    if (redstoneFound || diskFound)
                    {
                        return false;
                    }
                    
                    redstoneFound = true;
                }
                else
                {
                    return false;
                }
            }
        }
        
        return (redstoneFound && paperFound) || (diskFound && dyeFound);
    }

    @Override
    public ItemStack getCraftingResult( @Nonnull InventoryCrafting par1InventoryCrafting)
    {
        int diskID = -1;
        String diskLabel = null;

        int[] var3 = new int[3];
        int var4 = 0;
        int var5 = 0;
        ItemDiskLegacy var6;
        int var7;
        int var9;
        float var10;
        float var11;
        int var17;
        boolean dyeFound = false;

        for (var7 = 0; var7 < par1InventoryCrafting.getSizeInventory(); ++var7)
        {
            ItemStack var8 = par1InventoryCrafting.getStackInSlot(var7);

            if (var8 != null)
            {
                if (var8.getItem() instanceof ItemDiskLegacy )
                {
                    var6 = (ItemDiskLegacy)var8.getItem();
                    diskID = var6.getDiskID( var8 );
                    diskLabel = var6.getLabel( var8 );
                }
                else if (var8.getItem() == Items.DYE)
                {
                    dyeFound = true;
                    float[] var14 = Colour.values()[ var8.getItemDamage() & 0xf ].getRGB();
                    int var16 = (int)(var14[0] * 255.0F);
                    int var15 = (int)(var14[1] * 255.0F);
                    var17 = (int)(var14[2] * 255.0F);
                    var4 += Math.max(var16, Math.max(var15, var17));
                    var3[0] += var16;
                    var3[1] += var15;
                    var3[2] += var17;                 
                    ++var5;
                }
                else if (!(var8.getItem() != Items.PAPER || var8.getItem() != Items.REDSTONE))
                {
                    return null;
                }
            }
        }
        
        if( !dyeFound )
        {
            return ItemDiskLegacy.createFromIDAndColour( diskID, diskLabel, Colour.Blue.getHex() );
        }
        
        var7 = var3[0] / var5;
        int var13 = var3[1] / var5;
        var9 = var3[2] / var5;
        var10 = (float)var4 / (float)var5;
        var11 = (float)Math.max(var7, Math.max(var13, var9));
        var7 = (int)((float)var7 * var10 / var11);
        var13 = (int)((float)var13 * var10 / var11);
        var9 = (int)((float)var9 * var10 / var11);
        var17 = (var7 << 8) + var13;
        var17 = (var17 << 8) + var9;
        return ItemDiskLegacy.createFromIDAndColour( diskID, diskLabel, var17 );
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return ItemDiskLegacy.createFromIDAndColour( -1, null, Colour.Blue.getHex() );
    }

    @Nonnull
    @Override
    public ItemStack[] getRemainingItems( @Nonnull InventoryCrafting inventoryCrafting )
    {
        ItemStack[] results = new ItemStack[ inventoryCrafting.getSizeInventory() ];
        for (int i = 0; i < results.length; ++i)
        {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            results[i] = net.minecraftforge.common.ForgeHooks.getContainerItem(stack);
        }
        return results;
    }
}

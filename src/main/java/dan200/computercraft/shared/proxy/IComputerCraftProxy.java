/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.proxy;

import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;

import java.io.File;

public interface IComputerCraftProxy
{
    void preInit();
    void init();
    void remap( FMLMissingMappingsEvent mappings);
    boolean isClient();

    boolean getGlobalCursorBlink();
    long getRenderFrame();
    void deleteDisplayLists( int list, int range );
    Object getFixedWidthFontRenderer();

    String getRecordInfo( ItemStack item );
    void playRecord( SoundEvent record, String recordInfo, World world, BlockPos pos );

    Object getDiskDriveGUI( InventoryPlayer inventory, TileDiskDrive drive );
    Object getComputerGUI( TileComputer computer );
    Object getPrinterGUI( InventoryPlayer inventory, TilePrinter printer );
    Object getTurtleGUI( InventoryPlayer inventory, TileTurtle turtle );
    Object getPrintoutGUI( EntityPlayer player, EnumHand hand );
    Object getPocketComputerGUI( EntityPlayer player, EnumHand hand );

    File getWorldDir( World world );
    void handlePacket( ComputerCraftPacket packet, EntityPlayer player );
}

/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.*;
import dan200.computercraft.client.render.TileEntityMonitorRenderer;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ItemComputer;
import dan200.computercraft.shared.media.inventory.ContainerHeldItem;
import dan200.computercraft.shared.media.items.ItemDiskLegacy;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.network.ComputerCraftPacket;
import dan200.computercraft.shared.peripheral.diskdrive.TileDiskDrive;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.peripheral.printer.TilePrinter;
import dan200.computercraft.shared.pocket.inventory.ContainerPocketComputer;
import dan200.computercraft.shared.pocket.items.ItemPocketComputer;
import dan200.computercraft.shared.proxy.ComputerCraftProxyCommon;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.entity.TurtleVisionCamera;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComputerCraftProxyClient extends ComputerCraftProxyCommon
{
    private long m_tick;
    private long m_renderFrame;
    private FixedWidthFontRenderer m_fixedWidthFontRenderer;

    public ComputerCraftProxyClient()
    {
    }

    // IComputerCraftProxy implementation

    @Override
    public void init()
    {
        super.init();
        m_tick = 0;
        m_renderFrame = 0;

        // Load textures
        Minecraft mc = Minecraft.getMinecraft();
        m_fixedWidthFontRenderer = new FixedWidthFontRenderer( mc.getTextureManager() );

        // Register item models
        registerItemModel( ComputerCraft.Blocks.computer, new ItemMeshDefinition()
        {
            private ModelResourceLocation computer = new ModelResourceLocation( "computercraft:CC-Computer", "inventory" );
            private ModelResourceLocation advanced_computer = new ModelResourceLocation( "computercraft:advanced_computer", "inventory" );

            @Override
            public ModelResourceLocation getModelLocation( ItemStack stack )
            {
                ItemComputer itemComputer = (ItemComputer) stack.getItem();
                ComputerFamily family = itemComputer.getFamily( stack.getItemDamage() );
                return ( family == ComputerFamily.Advanced ) ? advanced_computer : computer;
            }
        }, new String[]{ "CC-Computer", "advanced_computer" } );
        registerItemModel( ComputerCraft.Blocks.peripheral, 0, "CC-Peripheral" );
        registerItemModel( ComputerCraft.Blocks.peripheral, 1, "wireless_modem" );
        registerItemModel( ComputerCraft.Blocks.peripheral, 2, "monitor" );
        registerItemModel( ComputerCraft.Blocks.peripheral, 3, "printer" );
        registerItemModel( ComputerCraft.Blocks.peripheral, 4, "advanced_monitor" );
        registerItemModel( ComputerCraft.Blocks.cable, 0, "CC-Cable" );
        registerItemModel( ComputerCraft.Blocks.cable, 1, "wired_modem" );
        registerItemModel( ComputerCraft.Blocks.commandComputer, "command_computer" );
        registerItemModel( ComputerCraft.Blocks.advancedModem, "advanced_modem" );

        registerItemModel( ComputerCraft.Items.disk, "disk" );
        registerItemModel( ComputerCraft.Items.diskExpanded, "diskExpanded" );
        registerItemModel( ComputerCraft.Items.treasureDisk, "treasureDisk" );
        registerItemModel( ComputerCraft.Items.printout, 0, "printout" );
        registerItemModel( ComputerCraft.Items.printout, 1, "pages" );
        registerItemModel( ComputerCraft.Items.printout, 2, "book" );
        registerItemModel( ComputerCraft.Items.pocketComputer, new ItemMeshDefinition()
        {
            private ModelResourceLocation pocket_computer_off = new ModelResourceLocation( "computercraft:pocketComputer", "inventory" );
            private ModelResourceLocation pocket_computer_on = new ModelResourceLocation( "computercraft:pocket_computer_on", "inventory" );
            private ModelResourceLocation pocket_computer_blinking = new ModelResourceLocation( "computercraft:pocket_computer_blinking", "inventory" );
            private ModelResourceLocation pocket_computer_on_modem_on = new ModelResourceLocation( "computercraft:pocket_computer_on_modem_on", "inventory" );
            private ModelResourceLocation pocket_computer_blinking_modem_on = new ModelResourceLocation( "computercraft:pocket_computer_blinking_modem_on", "inventory" );
            private ModelResourceLocation advanced_pocket_computer_off = new ModelResourceLocation( "computercraft:advanced_pocket_computer_off", "inventory" );
            private ModelResourceLocation advanced_pocket_computer_on = new ModelResourceLocation( "computercraft:advanced_pocket_computer_on", "inventory" );
            private ModelResourceLocation advanced_pocket_computer_blinking = new ModelResourceLocation( "computercraft:advanced_pocket_computer_blinking", "inventory" );
            private ModelResourceLocation advanced_pocket_computer_on_modem_on = new ModelResourceLocation( "computercraft:advanced_pocket_computer_on_modem_on", "inventory" );
            private ModelResourceLocation advanced_pocket_computer_blinking_modem_on = new ModelResourceLocation( "computercraft:advanced_pocket_computer_blinking_modem_on", "inventory" );

            @Override
            public ModelResourceLocation getModelLocation( ItemStack stack )
            {
                ItemPocketComputer itemPocketComputer = (ItemPocketComputer)stack.getItem();
                boolean modemOn = itemPocketComputer.getModemState( stack );
                switch( itemPocketComputer.getFamily( stack ) )
                {
                    case Advanced:
                    {
                        switch( itemPocketComputer.getState( stack ) )
                        {
                            case Off:
                            default:
                            {
                                return advanced_pocket_computer_off;
                            }
                            case On:
                            {
                                return modemOn ? advanced_pocket_computer_on_modem_on : advanced_pocket_computer_on;
                            }
                            case Blinking:
                            {
                                return modemOn ? advanced_pocket_computer_blinking_modem_on : advanced_pocket_computer_blinking;
                            }
                        }
                    }
                    case Normal:
                    default:
                    {
                        switch( itemPocketComputer.getState( stack ) )
                        {
                            case Off:
                            default:
                            {
                                return pocket_computer_off;
                            }
                            case On:
                            {
                                return modemOn ? pocket_computer_on_modem_on : pocket_computer_on;
                            }
                            case Blinking:
                            {
                                return modemOn ? pocket_computer_blinking_modem_on : pocket_computer_blinking;
                            }
                        }
                    }
                }
            }
        }, new String[] {
            "pocketComputer", "pocket_computer_on", "pocket_computer_blinking", "pocket_computer_on_modem_on", "pocket_computer_blinking_modem_on",
            "advanced_pocket_computer_off", "advanced_pocket_computer_on", "advanced_pocket_computer_blinking", "advanced_pocket_computer_on_modem_on", "advanced_pocket_computer_blinking_modem_on",
        } );

        // Setup
		mc.getItemColors().registerItemColorHandler(new DiskColorHandler(ComputerCraft.Items.disk), ComputerCraft.Items.disk);
		mc.getItemColors().registerItemColorHandler(new DiskColorHandler(ComputerCraft.Items.diskExpanded), ComputerCraft.Items.diskExpanded);

        // Setup renderers
        ClientRegistry.bindTileEntitySpecialRenderer( TileMonitor.class, new TileEntityMonitorRenderer() );

        // Setup client forge handlers
        registerForgeHandlers();
    }

    private void registerItemModel( Block block, int damage, String name )
    {
        registerItemModel( Item.getItemFromBlock( block ), damage, name );
    }

    private void registerItemModel( Item item, int damage, String name )
    {
        ModelResourceLocation res = new ModelResourceLocation( "computercraft:" + name, "inventory" );
        ModelBakery.registerItemVariants( item, new ResourceLocation( "computercraft", name ) );
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, damage, res );
    }

    private void registerItemModel( Block block, String name )
    {
        registerItemModel( Item.getItemFromBlock( block ), name );
    }

    private void registerItemModel( Item item, String name )
    {
        final ModelResourceLocation res = new ModelResourceLocation( "computercraft:" + name, "inventory" );
        ModelBakery.registerItemVariants( item, new ResourceLocation( "computercraft", name ) );
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, new ItemMeshDefinition()
        {
            @Override
            public ModelResourceLocation getModelLocation( ItemStack stack )
            {
                return res;
            }
        } );
    }

    private void registerItemModel( Block block, ItemMeshDefinition definition, String[] names )
    {
        registerItemModel( Item.getItemFromBlock( block ), definition, names );
    }

    private void registerItemModel( Item item, ItemMeshDefinition definition, String[] names )
    {
        ResourceLocation[] resources = new ResourceLocation[names.length];
        for( int i=0; i<resources.length; ++i )
        {
            resources[i] = new ResourceLocation( "computercraft", names[i] );
        }
        ModelBakery.registerItemVariants( item, resources );
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, definition );
    }

    @Override
    public boolean isClient()
    {
        return true;
    }

    @Override
    public boolean getGlobalCursorBlink()
    {
        return ( m_tick / 8) % 2 == 0;
    }

    @Override
    public long getRenderFrame()
    {
        return m_renderFrame;
    }

    @Override
    public void deleteDisplayLists( int list, int range )
    {
        GlStateManager.glDeleteLists( list, range );
    }

    @Override
    public Object getFixedWidthFontRenderer()
    {
        return m_fixedWidthFontRenderer;
    }

    @Override
    public String getRecordInfo( ItemStack recordStack )
    {
        List info = new ArrayList(1);
        recordStack.getItem().addInformation( recordStack, null, info, false );
        if( info.size() > 0 ) {
            return info.get(0).toString();
        } else {
            return super.getRecordInfo( recordStack );
        }
    }

    @Override
    public void playRecord( SoundEvent record, String recordInfo, World world, BlockPos pos )
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        world.playRecord( pos, record );
        if( record != null )
        {
            mc.ingameGUI.setRecordPlayingMessage( recordInfo );
        }
    }

    @Override
    public Object getDiskDriveGUI( InventoryPlayer inventory, TileDiskDrive drive )
    {
        return new GuiDiskDrive( inventory, drive );
    }

    @Override
    public Object getComputerGUI( TileComputer computer )
    {
        return new GuiComputer( computer );
    }

    @Override
    public Object getPrinterGUI( InventoryPlayer inventory, TilePrinter printer )
    {
        return new GuiPrinter( inventory, printer );
    }

    @Override
    public Object getTurtleGUI( InventoryPlayer inventory, TileTurtle turtle )
    {
        return new GuiTurtle( turtle.getWorld(), inventory, turtle );
    }

    @Override
    public Object getPrintoutGUI( EntityPlayer player, EnumHand hand )
    {
        ContainerHeldItem container = new ContainerHeldItem( player, hand );
        if( container.getStack() != null && container.getStack().getItem() instanceof ItemPrintout )
        {
            return new GuiPrintout( container );
        }
        return null;
    }

    @Override
    public Object getPocketComputerGUI( EntityPlayer player, EnumHand hand )
    {
        ContainerPocketComputer container = new ContainerPocketComputer( player, hand );
        if( container.getStack() != null && container.getStack().getItem() instanceof ItemPocketComputer )
        {
            return new GuiPocketComputer( container );
        }
        return null;
    }

    @Override
    public File getWorldDir( World world )
    {
        return world.getSaveHandler().getWorldDirectory();
    }

    @Override
    public void handlePacket( final ComputerCraftPacket packet, final EntityPlayer player )
    {
        switch( packet.m_packetType )
        {
            case ComputerCraftPacket.ComputerChanged:
            case ComputerCraftPacket.ComputerDeleted:
            {
                // Packet from Server to Client
                IThreadListener listener = Minecraft.getMinecraft();
                if( listener != null )
                {
                    if( listener.isCallingFromMinecraftThread() )
                    {
                        processPacket( packet, player );
                    }
                    else
                    {
                        listener.addScheduledTask( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                processPacket( packet, player );
                            }
                        } );
                    }
                }
                break;
            }
            default:
            {
                // Packet from Client to Server
                super.handlePacket( packet, player );
                break;
            }
        }
    }

    private void processPacket( ComputerCraftPacket packet, EntityPlayer player )
    {
        switch( packet.m_packetType )
        {
            ///////////////////////////////////
            // Packets from Server to Client //
            ///////////////////////////////////
            case ComputerCraftPacket.ComputerChanged:
            {
                int instanceID = packet.m_dataInt[ 0 ];
                if( !ComputerCraft.clientComputerRegistry.contains( instanceID ) )
                {
                    ComputerCraft.clientComputerRegistry.add( instanceID, new ClientComputer( instanceID ) );
                }
                ComputerCraft.clientComputerRegistry.get( instanceID ).handlePacket( packet, (EntityPlayer) player );
                break;
            }
            case ComputerCraftPacket.ComputerDeleted:
            {
                int instanceID = packet.m_dataInt[ 0 ];
                if( ComputerCraft.clientComputerRegistry.contains( instanceID ) )
                {
                    ComputerCraft.clientComputerRegistry.remove( instanceID );
                }
                break;
            }
        }
    }

    private void registerForgeHandlers()
    {
        ForgeHandlers handlers = new ForgeHandlers();
        FMLCommonHandler.instance().bus().register( handlers );
        MinecraftForge.EVENT_BUS.register( handlers );
    }

    public class ForgeHandlers
    {
        public ForgeHandlers()
        {
        }

        @SubscribeEvent
        public void onRenderHand( RenderHandEvent event )
        {
            // Don't draw the player arm when in turtle vision
            Minecraft mc = Minecraft.getMinecraft();
            if( mc.getRenderViewEntity() instanceof TurtleVisionCamera )
            {
                event.setCanceled( true );
            }
        }

        @SubscribeEvent
        public void onRenderPlayer( RenderPlayerEvent.Pre event )
        {
            Minecraft mc = Minecraft.getMinecraft();
            if( event.getEntityPlayer().isUser() && mc.getRenderViewEntity() instanceof TurtleVisionCamera )
            {
                // HACK: Force the 'livingPlayer' variable to the player, this ensures the entity is drawn
                //event.getRenderer().getRenderManager().livingPlayer = event.getEntityPlayer();
            }
        }

        @SubscribeEvent
        public void onRenderPlayer( RenderPlayerEvent.Post event )
        {
            Minecraft mc = Minecraft.getMinecraft();
            if( event.getEntityPlayer().isUser() && mc.getRenderViewEntity() instanceof TurtleVisionCamera )
            {
                // HACK: Restore the 'livingPlayer' variable to what it was before the RenderPlayerEvent.Pre hack
                //event.getRenderer().getRenderManager().livingPlayer = mc.getRenderViewEntity();
            }
        }

        @SubscribeEvent
        public void onPreRenderGameOverlay( RenderGameOverlayEvent.Pre event )
        {
            Minecraft mc = Minecraft.getMinecraft();
            if( mc.getRenderViewEntity() instanceof TurtleVisionCamera )
            {
                switch( event.getType() )
                {
                    case HELMET:
                    case PORTAL:
                    //case CROSSHAIRS:
                    case BOSSHEALTH:
                    case ARMOR:
                    case HEALTH:
                    case FOOD:
                    case AIR:
                    case HOTBAR:
                    case EXPERIENCE:
                    case HEALTHMOUNT:
                    case JUMPBAR:
                    {
                        event.setCanceled( true );
                        break;
                    }
                }
            }
        }

        @SubscribeEvent
        public void onTick( TickEvent.ClientTickEvent event )
        {
            if( event.phase == TickEvent.Phase.START )
            {
                m_tick++;
            }
        }

        @SubscribeEvent
        public void onRenderTick( TickEvent.RenderTickEvent event )
        {
            if( event.phase == TickEvent.Phase.START )
            {
                m_renderFrame++;
            }
        }
    }

	@SideOnly(Side.CLIENT)
	private static class DiskColorHandler implements IItemColor
	{
		private final ItemDiskLegacy disk;

		private DiskColorHandler(ItemDiskLegacy disk)
		{
			this.disk = disk;
		}

		@Override
		public int getColorFromItemstack(ItemStack stack, int layer)
		{
			return layer == 0 ? 0xFFFFFF : disk.getColor(stack);
		}
	}
}

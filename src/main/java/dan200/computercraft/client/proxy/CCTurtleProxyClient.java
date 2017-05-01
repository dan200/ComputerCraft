/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.proxy;

import com.google.common.base.Function;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.render.TileEntityTurtleRenderer;
import dan200.computercraft.client.render.TurtleSmartItemModel;
import dan200.computercraft.shared.proxy.CCTurtleProxyCommon;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;

public class CCTurtleProxyClient extends CCTurtleProxyCommon
{
	public CCTurtleProxyClient()
	{
	}
	
	// IComputerCraftProxy implementation
	
	@Override		
	public void init()
	{	
		super.init();

        // Register item models
        ItemMeshDefinition turtleMeshDefinition = new ItemMeshDefinition()
        {
            private ModelResourceLocation turtle_dynamic = new ModelResourceLocation( "computercraft:turtle_dynamic", "inventory" );

            @Override
            public ModelResourceLocation getModelLocation( ItemStack stack )
            {
                return turtle_dynamic;
            }
        };
        String[] turtleModelNames = new String[] {
            "turtle_dynamic",
            "CC-Turtle", "CC-TurtleAdvanced",
            "turtle_black", "turtle_red", "turtle_green", "turtle_brown",
            "turtle_blue", "turtle_purple", "turtle_cyan", "turtle_lightGrey",
            "turtle_grey", "turtle_pink", "turtle_lime", "turtle_yellow",
            "turtle_lightBlue", "turtle_magenta", "turtle_orange", "turtle_white",
            "turtle_elf_overlay"
        };
        registerItemModel( ComputerCraft.Blocks.turtle, turtleMeshDefinition, turtleModelNames );
        registerItemModel( ComputerCraft.Blocks.turtleExpanded, turtleMeshDefinition, turtleModelNames );
        registerItemModel( ComputerCraft.Blocks.turtleAdvanced, turtleMeshDefinition, turtleModelNames );

        // Setup renderers
        ClientRegistry.bindTileEntitySpecialRenderer( TileTurtle.class, new TileEntityTurtleRenderer() );

		// Setup client forge handlers
		registerForgeHandlers();
	}

    private void registerItemModel( Block block, ItemMeshDefinition definition, String[] names )
    {
        registerItemModel( Item.getItemFromBlock( block ), definition, names );
    }

    private void registerItemModel( Item item, ItemMeshDefinition definition, String[] names )
    {
        for( int i=0; i<names.length; ++i )
        {
            ModelBakery.addVariantName( item, "computercraft:" + names[ i ] );
        }
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, definition );
    }

	private void registerForgeHandlers()
	{
        ForgeHandlers handlers = new ForgeHandlers();
        MinecraftForge.EVENT_BUS.register( handlers );
	}

    public class ForgeHandlers
    {
        private TurtleSmartItemModel m_turtleSmartItemModel;

        public ForgeHandlers()
        {
            m_turtleSmartItemModel = new TurtleSmartItemModel();
            IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
            if( resourceManager instanceof SimpleReloadableResourceManager )
            {
                SimpleReloadableResourceManager reloadableResourceManager = (SimpleReloadableResourceManager)resourceManager;
                reloadableResourceManager.registerReloadListener( m_turtleSmartItemModel );
            }
        }

        @SubscribeEvent
        public void onTick( TickEvent.ClientTickEvent event )
        {
            if( event.phase == TickEvent.Phase.END )
            {
                TurtleBrain.cleanupBrains();
            }
        }

        @SubscribeEvent
        public void onTextureStitchEvent( TextureStitchEvent.Pre event )
        {
            event.map.registerSprite( new ResourceLocation( "computercraft", "blocks/craftyUpgrade" ) );
        }

        @SubscribeEvent
        public void onModelBakeEvent( ModelBakeEvent event )
        {
            loadModel( event, "turtle_modem_off_left" );
            loadModel( event, "turtle_modem_on_left" );
            loadModel( event, "turtle_modem_off_right" );
            loadModel( event, "turtle_modem_on_right" );
            loadModel( event, "turtle_crafting_table_left" );
            loadModel( event, "turtle_crafting_table_right" );
            loadModel( event, "advanced_turtle_modem_off_left" );
            loadModel( event, "advanced_turtle_modem_on_left" );
            loadModel( event, "advanced_turtle_modem_off_right" );
            loadModel( event, "advanced_turtle_modem_on_right" );
            loadSmartModel( event, "turtle_dynamic", m_turtleSmartItemModel );
        }

        private void loadModel( ModelBakeEvent event, String name )
        {
            try
            {
                IModel model = event.modelLoader.getModel(
                    new ResourceLocation( "computercraft", "block/" + name )
                );
                IBakedModel bakedModel = model.bake(
                    model.getDefaultState(),
                    DefaultVertexFormats.ITEM,
                    new Function<ResourceLocation, TextureAtlasSprite>()
                    {
                        @Override
                        public TextureAtlasSprite apply( ResourceLocation location )
                        {
                            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( location.toString() );
                        }
                    }
                );
                event.modelRegistry.putObject(
                    new ModelResourceLocation( "computercraft:" + name, "inventory" ),
                    bakedModel
                );
            }
            catch( IOException e )
            {
                System.out.println( "Could not load model: name" );
                e.printStackTrace();
            }
        }

        private void loadSmartModel( ModelBakeEvent event, String name, ISmartItemModel smartModel )
        {
            event.modelRegistry.putObject(
                new ModelResourceLocation( "computercraft:" + name, "inventory" ),
                smartModel
            );
        }
    }
}

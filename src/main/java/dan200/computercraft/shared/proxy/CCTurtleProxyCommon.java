/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.proxy;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.items.ComputerItemFactory;
import dan200.computercraft.shared.turtle.blocks.BlockTurtle;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.blocks.TileTurtleAdvanced;
import dan200.computercraft.shared.turtle.blocks.TileTurtleExpanded;
import dan200.computercraft.shared.turtle.items.ItemTurtleAdvanced;
import dan200.computercraft.shared.turtle.items.ItemTurtleLegacy;
import dan200.computercraft.shared.turtle.items.ItemTurtleNormal;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.turtle.recipes.TurtleRecipe;
import dan200.computercraft.shared.turtle.recipes.TurtleUpgradeRecipe;
import dan200.computercraft.shared.turtle.upgrades.*;
import dan200.computercraft.shared.util.IEntityDropConsumer;
import dan200.computercraft.shared.util.ImpostorRecipe;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

import java.util.*;

public abstract class CCTurtleProxyCommon implements ICCTurtleProxy
{    
    private Map<Integer, ITurtleUpgrade> m_legacyTurtleUpgrades;
    private Map<String, ITurtleUpgrade> m_turtleUpgrades;
    private Map<Entity, IEntityDropConsumer> m_dropConsumers;

    public CCTurtleProxyCommon()
    {
        m_legacyTurtleUpgrades = new HashMap<Integer, ITurtleUpgrade>();
        m_turtleUpgrades = new HashMap<String, ITurtleUpgrade>();
        m_dropConsumers = new WeakHashMap<Entity, IEntityDropConsumer>();
    }
    
    // ICCTurtleProxy implementation
    
    @Override        
    public void preInit()
    {
        registerItems();
    }
    
    @Override        
    public void init()
    {
        registerForgeHandlers();
        registerTileEntities();
    }

    @Override
    public void registerTurtleUpgrade( ITurtleUpgrade upgrade )
    {
        // Check conditions
        int id = upgrade.getLegacyUpgradeID();
        if( id >= 0 && id < 64 )
        {
            throw new RuntimeException( "Error registering '"+upgrade.getUnlocalisedAdjective()+" Turtle'. Legacy UpgradeID '"+id+"' is reserved by ComputerCraft" );
        }
        
        // Register
        registerTurtleUpgradeInternal( upgrade );
    }

    @Override
    public ITurtleUpgrade getTurtleUpgrade( String id )
    {
        return m_turtleUpgrades.get( id );
    }

    @Override
    public ITurtleUpgrade getTurtleUpgrade( int legacyId )
    {
        return m_legacyTurtleUpgrades.get( legacyId );
    }
    
    @Override
    public ITurtleUpgrade getTurtleUpgrade( ItemStack stack )
    {
        for( ITurtleUpgrade upgrade : m_turtleUpgrades.values() )
        {
            try
            {
                ItemStack upgradeStack = upgrade.getCraftingItem();
                if( InventoryUtil.areItemsStackable( upgradeStack, stack ) )
                {
                    return upgrade;
                }
            }
            catch( Exception e )
            {
                continue;
            }
        }
        return null;
    }

    public static boolean isUpgradeVanilla( ITurtleUpgrade upgrade )
    {
        return upgrade instanceof TurtleTool || upgrade instanceof TurtleModem || upgrade instanceof TurtleCraftingTable;
    }

    public static boolean isUpgradeSuitableForFamily( ComputerFamily family, ITurtleUpgrade upgrade )
    {
        if( family == ComputerFamily.Beginners )
        {
            return upgrade.getType().isTool();
        }
        else
        {
            return true;
        }
    }
    
    private void addAllUpgradedTurtles( ComputerFamily family, List<ItemStack> list )
    {
        ItemStack basicStack = TurtleItemFactory.create( -1, null, null, family, null, null, 0, null );
        if( basicStack != null )
        {
            list.add( basicStack );
        }
        addUpgradedTurtle( family, ComputerCraft.Upgrades.diamondPickaxe, list );
        addUpgradedTurtle( family, ComputerCraft.Upgrades.diamondAxe, list );
        addUpgradedTurtle( family, ComputerCraft.Upgrades.diamondSword, list );
        addUpgradedTurtle( family, ComputerCraft.Upgrades.diamondShovel, list );
        addUpgradedTurtle( family, ComputerCraft.Upgrades.diamondHoe, list );
        addUpgradedTurtle( family, ComputerCraft.Upgrades.craftingTable, list );
        addUpgradedTurtle( family, ComputerCraft.Upgrades.wirelessModem, list );
        addUpgradedTurtle( family, ComputerCraft.Upgrades.advancedModem, list );
    }

    private void addUpgradedTurtle( ComputerFamily family, ITurtleUpgrade upgrade, List<ItemStack> list )
    {
        if ( isUpgradeSuitableForFamily( family, upgrade ) )
        {
            ItemStack stack = TurtleItemFactory.create( -1, null, null, family, upgrade, null, 0, null );
            if( stack != null )
            {
                list.add( stack );
            }
        }
    }
    
    @Override
    public void addAllUpgradedTurtles( List<ItemStack> list )
    {
        addAllUpgradedTurtles( ComputerFamily.Normal, list );
        addAllUpgradedTurtles( ComputerFamily.Advanced, list );
    }
    
    @Override
    public void setEntityDropConsumer( Entity entity, IEntityDropConsumer consumer )
    {
        if( !m_dropConsumers.containsKey( entity ) )
        {
            boolean captured = ObfuscationReflectionHelper.<Boolean, Entity>getPrivateValue(
                Entity.class,
                entity, 
                "captureDrops"
            ).booleanValue();
            
            if( !captured )
            {
                ObfuscationReflectionHelper.setPrivateValue(
                        Entity.class,
                        entity,
                        new Boolean( true ),
                        "captureDrops"
                );
                
                ArrayList<EntityItem> items = ObfuscationReflectionHelper.getPrivateValue(
                        Entity.class,
                        entity,
                        "capturedDrops"
                );
                
                if( items == null || items.size() == 0 )
                {
                    m_dropConsumers.put( entity, consumer );
                }
            }
        }
    }    
    
    @Override
    public void clearEntityDropConsumer( Entity entity )
    {
        if( m_dropConsumers.containsKey( entity ) )
        {
            boolean captured = ObfuscationReflectionHelper.<Boolean, Entity>getPrivateValue(
                    Entity.class,
                    entity,
                    "captureDrops"
            );
            
            if( captured )
            {
                ObfuscationReflectionHelper.setPrivateValue(
                    Entity.class,
                    entity, 
                    new Boolean( false ),
                    "captureDrops"
                );
                
                ArrayList<EntityItem> items = ObfuscationReflectionHelper.getPrivateValue(
                        Entity.class,
                        entity,
                        "capturedDrops"
                );
                
                if( items != null )
                {
                    dispatchEntityDrops( entity, items );
                    items.clear();
                }
            }
            m_dropConsumers.remove( entity );
        }
    }

    private void registerTurtleUpgradeInternal( ITurtleUpgrade upgrade )
    {
        // Check conditions
        int legacyID = upgrade.getLegacyUpgradeID();
        if( legacyID >= 0 )
        {
            if( legacyID >= Short.MAX_VALUE )
            {
                throw new RuntimeException( "Error registering '"+upgrade.getUnlocalisedAdjective()+" Turtle'. UpgradeID '"+legacyID+"' is out of range" );
            }

            ITurtleUpgrade existing = m_legacyTurtleUpgrades.get( legacyID );
            if( existing != null )
            {
                throw new RuntimeException( "Error registering '" + upgrade.getUnlocalisedAdjective() + " Turtle'. UpgradeID '" + legacyID + "' is already registered by '" + existing.getUnlocalisedAdjective() + " Turtle'" );
            }
        }

        String id = upgrade.getUpgradeID().toString();
        ITurtleUpgrade existing = m_turtleUpgrades.get( id );
        if( existing != null )
        {
            throw new RuntimeException( "Error registering '" + upgrade.getUnlocalisedAdjective() + " Turtle'. UpgradeID '" + id + "' is already registered by '" + existing.getUnlocalisedAdjective() + " Turtle'" );
        }

        // Register
        if( legacyID >= 0 )
        {
            m_legacyTurtleUpgrades.put( legacyID, upgrade );
        }
        m_turtleUpgrades.put( id, upgrade );

        // Add a bunch of impostor recipes
        if( isUpgradeVanilla( upgrade )  )
        {
            // Add fake recipes to fool NEI
            List recipeList = CraftingManager.getInstance().getRecipeList();
            ItemStack craftingItem = upgrade.getCraftingItem();

            // A turtle just containing this upgrade
            for( ComputerFamily family : ComputerFamily.values() )
            {
                if( !isUpgradeSuitableForFamily( family, upgrade ) )
                {
                    continue;
                }

                ItemStack baseTurtle = TurtleItemFactory.create( -1, null, null, family, null, null, 0, null );
                if( baseTurtle != null )
                {
                    ItemStack craftedTurtle = TurtleItemFactory.create( -1, null, null, family, upgrade, null, 0, null );
                    ItemStack craftedTurtleFlipped = TurtleItemFactory.create( -1, null, null, family, null, upgrade, 0, null );
                    recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { baseTurtle, craftingItem }, craftedTurtle ) );
                    recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { craftingItem, baseTurtle }, craftedTurtleFlipped ) );

                    // A turtle containing this upgrade and another upgrade
                    for( ITurtleUpgrade otherUpgrade : m_turtleUpgrades.values() )
                    {
                        if( isUpgradeVanilla( otherUpgrade ) && isUpgradeSuitableForFamily( family, otherUpgrade ) )
                        {
                            ItemStack otherCraftingItem = otherUpgrade.getCraftingItem();

                            ItemStack otherCraftedTurtle = TurtleItemFactory.create( -1, null, null, family, null, otherUpgrade, 0, null );
                            ItemStack comboCraftedTurtle = TurtleItemFactory.create( -1, null, null, family, upgrade, otherUpgrade, 0, null );

                            ItemStack otherCraftedTurtleFlipped = TurtleItemFactory.create( -1, null, null, family, otherUpgrade, null, 0, null );
                            ItemStack comboCraftedTurtleFlipped = TurtleItemFactory.create( -1, null, null, family, otherUpgrade, upgrade, 0, null );

                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { otherCraftingItem, craftedTurtle }, comboCraftedTurtle ) );
                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { otherCraftedTurtle, craftingItem }, comboCraftedTurtle ) );
                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { craftedTurtleFlipped, otherCraftingItem }, comboCraftedTurtleFlipped ) );
                            recipeList.add( new ImpostorRecipe( 2, 1, new ItemStack[] { craftingItem, otherCraftedTurtleFlipped }, comboCraftedTurtleFlipped ) );
                            recipeList.add( new ImpostorRecipe( 3, 1, new ItemStack[] { otherCraftingItem, baseTurtle, craftingItem,  }, comboCraftedTurtle ) );
                            recipeList.add( new ImpostorRecipe( 3, 1, new ItemStack[] { craftingItem, baseTurtle, otherCraftingItem }, comboCraftedTurtleFlipped ) );
                        }
                    }
                }
            }
        }
    }
    
    private void registerItems()
    {
        // Blocks
        // Turtle
        ComputerCraft.Blocks.turtle = BlockTurtle.createTurtleBlock();
        GameRegistry.registerBlock( ComputerCraft.Blocks.turtle, ItemTurtleLegacy.class, "CC-Turtle" );

        ComputerCraft.Blocks.turtleExpanded = BlockTurtle.createTurtleBlock();
        GameRegistry.registerBlock( ComputerCraft.Blocks.turtleExpanded, ItemTurtleNormal.class, "CC-TurtleExpanded" );

        // Advanced Turtle
        ComputerCraft.Blocks.turtleAdvanced = BlockTurtle.createTurtleBlock();
        GameRegistry.registerBlock( ComputerCraft.Blocks.turtleAdvanced, ItemTurtleAdvanced.class, "CC-TurtleAdvanced" );

        // Recipe types
        RecipeSorter.register( "computercraft:turtle", TurtleRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shapeless" );
        RecipeSorter.register( "computercraft:turtle_upgrade", TurtleUpgradeRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shapeless" );

        // Recipes
        // Turtle
        GameRegistry.addRecipe( new TurtleRecipe( new Item[] {
            Items.IRON_INGOT, Items.IRON_INGOT, Items.IRON_INGOT,
            Items.IRON_INGOT, Item.getItemFromBlock( ComputerCraft.Blocks.computer ), Items.IRON_INGOT,
            Items.IRON_INGOT, Item.getItemFromBlock( Blocks.CHEST ), Items.IRON_INGOT,
        }, ComputerFamily.Normal ) );
        GameRegistry.addRecipe( new TurtleUpgradeRecipe() );

        // Impostor Turtle recipe (to fool NEI)
        ItemStack iron = new ItemStack( Items.IRON_INGOT, 1 );
        GameRegistry.addRecipe( new ImpostorRecipe( 3, 3,
            new ItemStack[] {
                iron, iron, iron,
                iron, ComputerItemFactory.create( -1, null, ComputerFamily.Normal ), iron,
                iron, new ItemStack( Blocks.CHEST, 1 ), iron,
            },
            TurtleItemFactory.create( -1, null, null, ComputerFamily.Normal, null, null, 0, null )
        ) );

        // Advanced Turtle
        GameRegistry.addRecipe( new TurtleRecipe( new Item[] {
            Items.GOLD_INGOT, Items.GOLD_INGOT, Items.GOLD_INGOT,
            Items.GOLD_INGOT, Item.getItemFromBlock( ComputerCraft.Blocks.computer ), Items.GOLD_INGOT,
            Items.GOLD_INGOT, Item.getItemFromBlock( Blocks.CHEST ), Items.GOLD_INGOT,
        }, ComputerFamily.Advanced ) );

        // Impostor Advanced Turtle recipe (to fool NEI)
        ItemStack gold = new ItemStack( Items.GOLD_INGOT, 1 );
        GameRegistry.addRecipe( new ImpostorRecipe( 3, 3,
            new ItemStack[] {
                gold, gold, gold,
                gold, ComputerItemFactory.create( -1, null, ComputerFamily.Advanced ), gold,
                gold, new ItemStack( Blocks.CHEST, 1 ), gold,
            },
            TurtleItemFactory.create( -1, null, null, ComputerFamily.Advanced, null, null, 0, null )
        ) );

        // Upgrades
        ComputerCraft.Upgrades.wirelessModem =  new TurtleModem( false, new ResourceLocation( "computercraft", "wireless_modem" ), 1 );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.wirelessModem );

        ComputerCraft.Upgrades.craftingTable = new TurtleCraftingTable( 2 );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.craftingTable );

        ComputerCraft.Upgrades.diamondSword = new TurtleSword( new ResourceLocation( "minecraft", "diamond_sword" ), 3, "upgrade.minecraft:diamond_sword.adjective", Items.DIAMOND_SWORD );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.diamondSword );

        ComputerCraft.Upgrades.diamondShovel = new TurtleShovel( new ResourceLocation( "minecraft", "diamond_shovel" ), 4, "upgrade.minecraft:diamond_shovel.adjective", Items.DIAMOND_SHOVEL );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.diamondShovel );

        ComputerCraft.Upgrades.diamondPickaxe = new TurtleTool( new ResourceLocation( "minecraft", "diamond_pickaxe" ), 5, "upgrade.minecraft:diamond_pickaxe.adjective", Items.DIAMOND_PICKAXE );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.diamondPickaxe );

        ComputerCraft.Upgrades.diamondAxe = new TurtleAxe( new ResourceLocation( "minecraft", "diamond_axe" ), 6, "upgrade.minecraft:diamond_axe.adjective", Items.DIAMOND_AXE );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.diamondAxe );

        ComputerCraft.Upgrades.diamondHoe = new TurtleHoe( new ResourceLocation( "minecraft", "diamond_hoe" ), 7, "upgrade.minecraft:diamond_hoe.adjective", Items.DIAMOND_HOE );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.diamondHoe );

        ComputerCraft.Upgrades.advancedModem =  new TurtleModem( true, new ResourceLocation( "computercraft", "advanced_modem" ), -1 );
        registerTurtleUpgradeInternal( ComputerCraft.Upgrades.advancedModem );
    }

    private void registerTileEntities()
    {
        // TileEntities
        GameRegistry.registerTileEntity( TileTurtle.class, "turtle" );
        GameRegistry.registerTileEntity( TileTurtleExpanded.class, "turtleex" );
        GameRegistry.registerTileEntity( TileTurtleAdvanced.class, "turtleadv" );
    }
    
    private void registerForgeHandlers()
    {
        ForgeHandlers handlers = new ForgeHandlers();
        MinecraftForge.EVENT_BUS.register( handlers );
    }
        
    public class ForgeHandlers
    {
        private ForgeHandlers()
        {
        }

        // Forge event responses 
        @SubscribeEvent
        public void onEntityLivingDrops( LivingDropsEvent event )
        {
            dispatchEntityDrops( event.getEntity(), event.getDrops() );
        }
    }
    
    private void dispatchEntityDrops( Entity entity, java.util.List<EntityItem> drops )
    {
        IEntityDropConsumer consumer = m_dropConsumers.get( entity );
        if( consumer != null )
        {
            // All checks have passed, lets dispatch the drops
            Iterator<EntityItem> it = drops.iterator();
            while( it.hasNext() )
            {
                EntityItem entityItem = (EntityItem)it.next();
                consumer.consumeDrop( entity, entityItem.getEntityItem() );
            }
            drops.clear();
        }
    }
}

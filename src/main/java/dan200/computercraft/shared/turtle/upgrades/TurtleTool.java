/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import dan200.computercraft.shared.turtle.core.TurtlePlaceCommand;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import dan200.computercraft.shared.util.IEntityDropConsumer;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.Iterator;

public class TurtleTool implements ITurtleUpgrade
{
    private ResourceLocation m_id;
    private int m_legacyId;
    private String m_adjective;
    protected ItemStack m_item;

    public TurtleTool( ResourceLocation id, int legacyID, String adjective, Item item )
    {
        m_id = id;
        m_legacyId = legacyID;
        m_adjective = adjective;
        m_item = new ItemStack( item, 1, 0 );
    }

    @Override
    public ResourceLocation getUpgradeID()
    {
        return m_id;
    }

    @Override
    public int getLegacyUpgradeID()
    {
        return m_legacyId;
    }

    @Override
    public String getUnlocalisedAdjective()
    {
        return m_adjective;
    }

    @Override
    public TurtleUpgradeType getType()
    {
        return TurtleUpgradeType.Tool;
    }

    @Override
    public ItemStack getCraftingItem()
    {
        return m_item.copy();
    }

    @Override
    public IPeripheral createPeripheral( ITurtleAccess turtle, TurtleSide side )
    {
        return null;
    }

    @Override
    @SideOnly( Side.CLIENT )
    public Pair<IBakedModel, Matrix4f> getModel( ITurtleAccess turtle, TurtleSide side )
    {
        float xOffset = (side == TurtleSide.Left) ? -0.40625f : 0.40625f;
        Matrix4f transform = new Matrix4f(
            0.0f, 0.0f, -1.0f, 1.0f + xOffset,
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );
        Minecraft mc = Minecraft.getMinecraft();
        return Pair.of(
            mc.getRenderItem().getItemModelMesher().getItemModel( m_item ),
            transform
        );
    }

    @Override
    public void update( ITurtleAccess turtle, TurtleSide side )
    {
    }

    @Override
    public TurtleCommandResult useTool( ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, EnumFacing direction )
    {
        switch( verb )
        {
            case Attack:
            {
                return attack( turtle, direction );
            }
            case Dig:
            {
                return dig( turtle, direction );
            }
            default:
            {
                return TurtleCommandResult.failure( "Unsupported action" );
            }
        }
    }

    protected boolean canBreakBlock( World world, BlockPos pos )
    {
        IBlockState state = world.getBlockState( pos );
        Block block = state.getBlock();
        if( block.isAir( state, world, pos ) || block == Blocks.BEDROCK || state.getBlockHardness( world, pos ) <= -1.0F )
        {
            return false;
        }
        return true;
    }
    
    protected boolean canHarvestBlock( World world, BlockPos pos )
    {
        Block block = world.getBlockState( pos ).getBlock();
        TurtlePlayer turtlePlayer = new TurtlePlayer( (WorldServer)world );
        turtlePlayer.loadInventory( m_item.copy() );
        return ForgeHooks.canHarvestBlock( block, turtlePlayer, world, pos );
    }
    
    protected float getDamageMultiplier()
    {
        return 3.0f;
    }
    
    private TurtleCommandResult attack( final ITurtleAccess turtle, EnumFacing direction )
    {
        // Create a fake player, and orient it appropriately
        final World world = turtle.getWorld();
        final BlockPos position = turtle.getPosition();
        final TurtlePlayer turtlePlayer = TurtlePlaceCommand.createPlayer( turtle, position, direction );

        // See if there is an entity present
        Vec3d turtlePos = new Vec3d( turtlePlayer.posX, turtlePlayer.posY, turtlePlayer.posZ );
        Vec3d rayDir = turtlePlayer.getLook( 1.0f );
        Vec3d rayStart = turtlePos;
        Pair<Entity, Vec3d> hit = WorldUtil.rayTraceEntities( world, rayStart, rayDir, 1.5 );
        if( hit != null )
        {
            // Load up the turtle's inventory
            ItemStack stackCopy = m_item.copy();
            turtlePlayer.loadInventory( stackCopy );

            // Start claiming entity drops
            Entity hitEntity = hit.getKey();
            ComputerCraft.setEntityDropConsumer( hitEntity, new IEntityDropConsumer()
            {
                @Override
                public void consumeDrop( Entity entity, ItemStack drop )
                {
                    ItemStack remainder = InventoryUtil.storeItems( drop, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot() );
                    if( remainder != null )
                    {
                        WorldUtil.dropItemStack( remainder, world, position, turtle.getDirection().getOpposite() );
                    }
                }
            } );

            // Place on the entity
            boolean placed = false;
            if( hitEntity.canBeAttackedWithItem() && !hitEntity.hitByEntity( turtlePlayer ) )
            {
                float damage = (float)turtlePlayer.getEntityAttribute( SharedMonsterAttributes.ATTACK_DAMAGE ).getAttributeValue();
                damage *= getDamageMultiplier();
                if( damage > 0.0f )
                {
                    DamageSource source = DamageSource.causePlayerDamage( turtlePlayer );
                    if( hitEntity instanceof EntityArmorStand )
                    {
                        // Special case for armor stands: attack twice to guarantee destroy
                        hitEntity.attackEntityFrom( source, damage );
                        if( !hitEntity.isDead )
                        {
                            hitEntity.attackEntityFrom( source, damage );
                        }
                        placed = true;
                    }
                    else
                    {
                        if( hitEntity.attackEntityFrom( source, damage ) )
                        {
                            placed = true;
                        }
                    }
                }
            }

            // Stop claiming drops
            ComputerCraft.clearEntityDropConsumer( hitEntity );

            // Put everything we collected into the turtles inventory, then return
            if( placed )
            {
                turtlePlayer.unloadInventory( turtle );
                return TurtleCommandResult.success();
            }
        }

        return TurtleCommandResult.failure( "Nothing to attack here" );
    }
    
    private TurtleCommandResult dig( ITurtleAccess turtle, EnumFacing direction )
    {
        // Get ready to dig
        World world = turtle.getWorld();
        BlockPos position = turtle.getPosition();
        BlockPos newPosition = WorldUtil.moveCoords( position, direction );

        if( WorldUtil.isBlockInWorld( world, newPosition ) &&
            !world.isAirBlock( newPosition ) &&
            !WorldUtil.isLiquidBlock( world, newPosition ) )
        {
            if( ComputerCraft.turtlesObeyBlockProtection )
            {
                // Check spawn protection
                TurtlePlayer turtlePlayer = TurtlePlaceCommand.createPlayer( turtle, position, direction );
                if( !ComputerCraft.isBlockEditable( world, newPosition, turtlePlayer ) )
                {
                    return TurtleCommandResult.failure( "Cannot break protected block" );
                }
            }

            // Check if we can break the block
            if( !canBreakBlock( world, newPosition ) )
            {
                return TurtleCommandResult.failure( "Unbreakable block detected" );
            }

            // Consume the items the block drops
            if( canHarvestBlock( world, newPosition ) )
            {
                java.util.List<ItemStack> items = getBlockDropped( world, newPosition );
                if( items != null && items.size() > 0 )
                {
                    Iterator<ItemStack> it = items.iterator();
                    while( it.hasNext() )
                    {
                        ItemStack stack = it.next();
                        ItemStack remainder = InventoryUtil.storeItems( stack, turtle.getInventory(), 0, turtle.getInventory().getSizeInventory(), turtle.getSelectedSlot() );
                        if( remainder != null )
                        {
                            // If there's no room for the items, drop them
                            WorldUtil.dropItemStack( remainder, world, position, direction );
                        }
                    }
                }
            }

            // Destroy the block
            IBlockState previousState = world.getBlockState( newPosition );
			world.playEvent(2001, newPosition, Block.getStateId(previousState));
            world.setBlockToAir( newPosition );

            // Remember the previous block
            if( turtle instanceof TurtleBrain )
            {
                TurtleBrain brain = (TurtleBrain)turtle;
                brain.saveBlockChange( newPosition, previousState );
            }

            return TurtleCommandResult.success();
        }

        return TurtleCommandResult.failure( "Nothing to dig here" );
    }

    private java.util.List<ItemStack> getBlockDropped( World world, BlockPos pos )
    {
        Block block = world.getBlockState( pos ).getBlock();
        return block.getDrops( world, pos, world.getBlockState( pos ), 0 );
    }
}

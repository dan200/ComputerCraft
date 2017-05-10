/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.items;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.blocks.ComputerState;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.pocket.apis.PocketAPI;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import dan200.computercraft.shared.util.StringUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemPocketComputer extends Item implements IComputerItem, IMedia
{
    public ItemPocketComputer()
    {
        setMaxStackSize( 1 );
        setHasSubtypes( true );
        setUnlocalizedName( "computercraft:pocket_computer" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    public ItemStack create( int id, String label, ComputerFamily family, IPocketUpgrade upgrade )
    {
        // Ignore types we can't handle
        if( family != ComputerFamily.Normal && family != ComputerFamily.Advanced )
        {
            return null;
        }

        // Build the stack
        int damage = (family == ComputerFamily.Advanced) ? 1 : 0;
        ItemStack result = new ItemStack( this, 1, damage );
        if( id >= 0 || upgrade != null )
        {
            NBTTagCompound compound = new NBTTagCompound();
            if( id >= 0 )
            {
                compound.setInteger( "computerID", id );
            }
            if( upgrade != null )
            {
                compound.setString( "upgrade", upgrade.getUpgradeID().toString() );
            }
            result.setTagCompound( compound );
        }
        if( label != null )
        {
            result.setStackDisplayName( label );
        }
        return result;
    }

    @Override
    public void getSubItems( @Nonnull Item itemID, CreativeTabs tabs, List<ItemStack> list )
    {
        getSubItems( list, ComputerFamily.Normal );
        getSubItems( list, ComputerFamily.Advanced );
    }

    private void getSubItems( List<ItemStack> list, ComputerFamily family )
    {
        list.add( PocketComputerItemFactory.create( -1, null, family, null ) );
        for (IPocketUpgrade upgrade : ComputerCraft.getVanillaPocketUpgrades())
        {
            list.add( PocketComputerItemFactory.create( -1, null, family, upgrade ) );
        }
    }

    @Override
    public void onUpdate( ItemStack stack, World world, Entity entity, int slotNum, boolean selected )
    {
        if( !world.isRemote )
        {
            // Server side
            IInventory inventory = (entity instanceof EntityPlayer) ? ((EntityPlayer) entity).inventory : null;
            PocketServerComputer computer = createServerComputer( world, inventory, entity, stack );
            if( computer != null )
            {
                IPocketUpgrade upgrade = getUpgrade( stack );

                // Ping computer
                computer.keepAlive();
                computer.setWorld( world );
                computer.updateValues( entity, stack, upgrade );

                // Sync ID
                int id = computer.getID();
                if( id != getComputerID( stack ) )
                {
                    setComputerID( stack, id );
                    if( inventory != null )
                    {
                        inventory.markDirty();
                    }
                }

                // Sync label
                String label = computer.getLabel();
                if( !Objects.equal( label, getLabel( stack ) ) )
                {
                    setLabel( stack, label );
                    if( inventory != null )
                    {
                        inventory.markDirty();
                    }
                }

                // Update pocket upgrade
                if( upgrade != null )
                {
                    upgrade.update( computer, computer.getPeripheral( 2 ) );
                }
            }
        }
        else
        {
            // Client side
            ClientComputer computer = createClientComputer( stack );
            if( computer != null )
            {
                // Todo: things here?
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick( @Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand )
    {
        if( !world.isRemote )
        {
            PocketServerComputer computer = createServerComputer( world, player.inventory, player, stack );

            boolean stop = false;
            if( computer != null )
            {
                computer.turnOn();

                IPocketUpgrade upgrade = getUpgrade( stack );
                if( upgrade != null )
                {
                    computer.updateValues( player, stack, upgrade );
                    stop = upgrade.onRightClick( world, computer, computer.getPeripheral( 2 ) );
                }
            }

            if( !stop ) ComputerCraft.openPocketComputerGUI( player, hand );
        }
        return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, stack );
    }

    @Nonnull
    @Override
    public String getUnlocalizedName( ItemStack stack )
    {
        switch( getFamily( stack ) )
        {
            case Normal:
            default:
            {
                return "item.computercraft:pocket_computer";
            }
            case Advanced:
            {
                return "item.computercraft:advanced_pocket_computer";
            }
        }
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName( @Nonnull ItemStack stack )
    {
        String baseString = getUnlocalizedName( stack );
        IPocketUpgrade upgrade = getUpgrade( stack );
        if( upgrade != null )
        {
            return StringUtil.translateToLocalFormatted(
                baseString + ".upgraded.name",
                StringUtil.translateToLocal( upgrade.getUnlocalisedAdjective() )
            );
        }
        else
        {
            return StringUtil.translateToLocal( baseString + ".name" );
        }
    }

    @Override
    public void addInformation( ItemStack stack, EntityPlayer player, List<String> list, boolean debug )
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

    private PocketServerComputer createServerComputer( final World world, IInventory inventory, Entity entity, ItemStack stack )
    {
        if( world.isRemote )
        {
            return null;
        }

        PocketServerComputer computer;
        int instanceID = getInstanceID( stack );
        int sessionID = getSessionID( stack );
        int correctSessionID = ComputerCraft.serverComputerRegistry.getSessionID();

        if( instanceID >= 0 && sessionID == correctSessionID &&
            ComputerCraft.serverComputerRegistry.contains( instanceID ) )
        {
            computer = (PocketServerComputer) ComputerCraft.serverComputerRegistry.get( instanceID );
        }
        else
        {
            if( instanceID < 0 || sessionID != correctSessionID )
            {
                instanceID = ComputerCraft.serverComputerRegistry.getUnusedInstanceID();
                setInstanceID( stack, instanceID );
                setSessionID( stack, correctSessionID );
            }
            int computerID = getComputerID( stack );
            if( computerID < 0 )
            {
                computerID = ComputerCraft.createUniqueNumberedSaveDir( world, "computer" );
                setComputerID( stack, computerID );
            }
            computer = new PocketServerComputer(
                world,
                computerID,
                getLabel( stack ),
                instanceID,
                getFamily( stack )
            );
            computer.updateValues( entity, stack, getUpgrade( stack ) );
            computer.addAPI( new PocketAPI( computer ) );
            ComputerCraft.serverComputerRegistry.add( instanceID, computer );
            if( inventory != null )
            {
                inventory.markDirty();
            }
        }
        computer.setWorld( world );
        return computer;
    }

    public ServerComputer getServerComputer( ItemStack stack )
    {
        int instanceID = getInstanceID( stack );
        if( instanceID >= 0 )
        {
            return ComputerCraft.serverComputerRegistry.get( instanceID );
        }
        return null;
    }

    public ClientComputer createClientComputer( ItemStack stack )
    {
        int instanceID = getInstanceID( stack );
        if( instanceID >= 0 )
        {
            if( !ComputerCraft.clientComputerRegistry.contains( instanceID ) )
            {
                ComputerCraft.clientComputerRegistry.add( instanceID, new ClientComputer( instanceID ) );
            }
            return ComputerCraft.clientComputerRegistry.get( instanceID );
        }
        return null;
    }

    private ClientComputer getClientComputer( ItemStack stack )
    {
        int instanceID = getInstanceID( stack );
        if( instanceID >= 0 )
        {
            return ComputerCraft.clientComputerRegistry.get( instanceID );
        }
        return null;
    }

    // IComputerItem implementation

    @Override
    public int getComputerID( ItemStack stack )
    {
        NBTTagCompound compound = stack.getTagCompound();
        if( compound != null && compound.hasKey( "computerID" ) )
        {
            return compound.getInteger( "computerID" );
        }
        return -1;
    }

    private void setComputerID( ItemStack stack, int computerID )
    {
        if( !stack.hasTagCompound() )
        {
            stack.setTagCompound( new NBTTagCompound() );
        }
        stack.getTagCompound().setInteger( "computerID", computerID );
    }

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
    public ComputerFamily getFamily( ItemStack stack )
    {
        int damage = stack.getItemDamage();
        switch( damage )
        {
            case 0:
            default:
            {
                return ComputerFamily.Normal;
            }
            case 1:
            {
                return ComputerFamily.Advanced;
            }
        }
    }

    // IMedia

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
        ServerComputer computer = createServerComputer( world, null, null, stack );
        if( computer != null )
        {
            return computer.getRootMount();
        }
        return null;
    }

    private int getInstanceID( ItemStack stack )
    {
        NBTTagCompound compound = stack.getTagCompound();
        if( compound != null && compound.hasKey( "instanceID" ) )
        {
            return compound.getInteger( "instanceID" );
        }
        return -1;
    }

    private void setInstanceID( ItemStack stack, int instanceID )
    {
        if( !stack.hasTagCompound() )
        {
            stack.setTagCompound( new NBTTagCompound() );
        }
        stack.getTagCompound().setInteger( "instanceID", instanceID );
    }

    private int getSessionID( ItemStack stack )
    {
        NBTTagCompound compound = stack.getTagCompound();
        if( compound != null && compound.hasKey( "sessionID" ) )
        {
            return compound.getInteger( "sessionID" );
        }
        return -1;
    }

    private void setSessionID( ItemStack stack, int sessionID )
    {
        if( !stack.hasTagCompound() )
        {
            stack.setTagCompound( new NBTTagCompound() );
        }
        stack.getTagCompound().setInteger( "sessionID", sessionID );
    }

    @SideOnly(Side.CLIENT)
    public ComputerState getState( ItemStack stack )
    {
        ClientComputer computer = getClientComputer( stack );
        if( computer != null && computer.isOn() )
        {
            return computer.isCursorDisplayed() ? ComputerState.Blinking : ComputerState.On;
        }
        return ComputerState.Off;
    }

    @SideOnly(Side.CLIENT)
    public int getLightState( ItemStack stack )
    {
        ClientComputer computer = getClientComputer( stack );
        if( computer != null && computer.isOn() )
        {
            NBTTagCompound computerNBT = computer.getUserData();
            if( computerNBT != null )
            {
                return computerNBT.getInteger( "modemLight" );
            }
        }
        return 0;
    }

    public IPocketUpgrade getUpgrade( ItemStack stack )
    {
        NBTTagCompound compound = stack.getTagCompound();
        if( compound != null )
        {
            if( compound.hasKey( "upgrade", Constants.NBT.TAG_STRING ) )
            {
                String name = compound.getString( "upgrade" );
                return ComputerCraft.getPocketUpgrade( name );
            }
            else if( compound.hasKey( "upgrade", Constants.NBT.TAG_ANY_NUMERIC ) )
            {
                int id = compound.getInteger( "upgrade" );
                if( id == 1 )
                {
                    return ComputerCraft.getPocketUpgrade( "computercraft:wireless_modem" );
                }
            }
        }

        return null;
    }

    public void setUpgrade( ItemStack stack, IPocketUpgrade upgrade )
    {
        NBTTagCompound compound = stack.getTagCompound();
        if( compound == null ) stack.setTagCompound( compound = new NBTTagCompound() );

        if( upgrade == null )
        {
            compound.removeTag( "upgrade" );
        }
        else
        {
            compound.setString( "upgrade", upgrade.getUpgradeID().toString() );
        }

        compound.removeTag( "upgrade_info" );
    }

    public NBTTagCompound getUpgradeInfo( ItemStack stack )
    {
        NBTTagCompound tag = stack.getTagCompound();
        if( tag == null )
        {
            tag = new NBTTagCompound();
            stack.setTagCompound( tag );
        }

        if( tag.hasKey( "upgrade_info", Constants.NBT.TAG_COMPOUND ) )
        {
            return tag.getCompoundTag( "upgrade_info" );
        }
        else
        {
            NBTTagCompound sub = new NBTTagCompound();
            tag.setTag( "upgrade_info", sub );
            return sub;
        }
    }
}

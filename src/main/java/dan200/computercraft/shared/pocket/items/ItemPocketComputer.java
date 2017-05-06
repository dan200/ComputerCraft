/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.pocket.items;

import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.media.IMedia;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.computer.blocks.ComputerState;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.items.IComputerItem;
import dan200.computercraft.shared.pocket.apis.PocketAPI;
import dan200.computercraft.shared.pocket.peripherals.PocketModemPeripheral;
import dan200.computercraft.shared.util.StringUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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

import java.util.List;

;

public class ItemPocketComputer extends Item implements IComputerItem, IMedia
{
    public ItemPocketComputer()
    {
        setMaxStackSize( 1 );
        setHasSubtypes( true );
        setUnlocalizedName( "computercraft:pocket_computer" );
        setCreativeTab( ComputerCraft.mainCreativeTab );
    }

    public ItemStack create( int id, String label, ComputerFamily family, boolean modem )
    {
        // Ignore types we can't handle
        if( family != ComputerFamily.Normal && family != ComputerFamily.Advanced )
        {
            return null;
        }

        // Build the stack
        int damage = (family == ComputerFamily.Advanced) ? 1 : 0;
        ItemStack result = new ItemStack( this, 1, damage );
        if( id >= 0 || modem )
        {
            NBTTagCompound compound = new NBTTagCompound();
            if( id >= 0 )
            {
                compound.setInteger( "computerID", id );
            }
            if( modem )
            {
                compound.setInteger( "upgrade", 1 );
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
    public void getSubItems( Item itemID, CreativeTabs tabs, List list )
    {
        list.add( PocketComputerItemFactory.create( -1, null, ComputerFamily.Normal, false ) );
        list.add( PocketComputerItemFactory.create( -1, null, ComputerFamily.Normal, true ) );
        list.add( PocketComputerItemFactory.create( -1, null, ComputerFamily.Advanced, false ) );
        list.add( PocketComputerItemFactory.create( -1, null, ComputerFamily.Advanced, true ) );
    }

    @Override
    public void onUpdate( ItemStack stack, World world, Entity entity, int slotNum, boolean selected )
    {
        if( !world.isRemote )
        {
            // Server side
            IInventory inventory = (entity instanceof EntityPlayer) ? ((EntityPlayer)entity).inventory : null;
            ServerComputer computer = createServerComputer( world, inventory, stack );
            if( computer != null )
            {
                // Ping computer
                computer.keepAlive();
                computer.setWorld( world );

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

                // Update modem
                IPeripheral peripheral = computer.getPeripheral( 2 );
                if( peripheral != null && peripheral instanceof PocketModemPeripheral )
                {
                    // Location
                    PocketModemPeripheral modem = (PocketModemPeripheral)peripheral;
                    if( entity instanceof EntityLivingBase )
                    {
                        EntityLivingBase player = (EntityLivingBase)entity;
                        modem.setLocation( world, player.posX, player.posY + player.getEyeHeight(), player.posZ );
                    }
                    else
                    {
                        modem.setLocation( world, entity.posX, entity.posY, entity.posZ );
                    }

                    // Light
                    boolean modemLight = modem.isActive();
                    NBTTagCompound modemNBT = computer.getUserData();
                    if( modemNBT.getBoolean( "modemLight" ) != modemLight )
                    {
                        modemNBT.setBoolean( "modemLight", modemLight );
                        computer.updateUserData();
                    }
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

    @Override
    public ActionResult<ItemStack> onItemRightClick( ItemStack stack, World world, EntityPlayer player, EnumHand hand )
    {
        if( !world.isRemote )
        {
            ServerComputer computer = createServerComputer( world, player.inventory, stack );
            if( computer != null )
            {
                computer.turnOn();
            }
            ComputerCraft.openPocketComputerGUI( player, hand );
        }
        return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, stack );
    }

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

    @Override
    public String getItemStackDisplayName( ItemStack stack )
    {
        String baseString = getUnlocalizedName( stack );
        boolean modem = getHasModem( stack );
        if( modem )
        {
            return StringUtil.translateToLocalFormatted(
                baseString + ".upgraded.name",
                StringUtil.translateToLocal( "upgrade.computercraft:wireless_modem.adjective" )
            );
        }
        else
        {
            return StringUtil.translateToLocal( baseString + ".name" );
        }
    }

    @Override
    public void addInformation( ItemStack stack, EntityPlayer player, List list, boolean debug )
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

    private ServerComputer createServerComputer( final World world, IInventory inventory, ItemStack stack )
    {
        if( world.isRemote )
        {
            return null;
        }

        ServerComputer computer;
        int instanceID = getInstanceID( stack );
        int sessionID = getSessionID( stack );
        int correctSessionID = ComputerCraft.serverComputerRegistry.getSessionID();

        if( instanceID >= 0 && sessionID == correctSessionID &&
            ComputerCraft.serverComputerRegistry.contains( instanceID ) )
        {
            computer = ComputerCraft.serverComputerRegistry.get( instanceID );
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
            computer = new ServerComputer(
                world,
                computerID,
                getLabel( stack ),
                instanceID,
                getFamily( stack ),
                ComputerCraft.terminalWidth_pocketComputer,
                ComputerCraft.terminalHeight_pocketComputer
            );
            computer.addAPI( new PocketAPI() );
            if( getHasModem( stack ) )
            {
                computer.setPeripheral( 2, new PocketModemPeripheral( false ) );
            }
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
    public String getLabel( ItemStack stack )
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
    public boolean setLabel( ItemStack stack, String label )
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
    public String getAudioTitle( ItemStack stack )
    {
        return null;
    }

    @Override
    public SoundEvent getAudio( ItemStack stack )
    {
        return null;
    }

    @Override
    public IMount createDataMount( ItemStack stack, World world )
    {
        ServerComputer computer = createServerComputer( world, null, stack );
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

    public ComputerState getState( ItemStack stack )
    {
        ClientComputer computer = getClientComputer( stack );
        if( computer != null && computer.isOn() )
        {
            return computer.isCursorDisplayed() ? ComputerState.Blinking : ComputerState.On;
        }
        return ComputerState.Off;
    }

    public boolean getModemState( ItemStack stack )
    {
        ClientComputer computer = getClientComputer( stack );
        if( computer != null && computer.isOn() )
        {
            NBTTagCompound computerNBT = computer.getUserData();
            if( computerNBT != null && computerNBT.getBoolean( "modemLight" ) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean getHasModem( ItemStack stack )
    {
        NBTTagCompound compound = stack.getTagCompound();
        if( compound != null && compound.hasKey( "upgrade" ) )
        {
            return (compound.getInteger( "upgrade" ) == 1);
        }
        return false;
    }
}

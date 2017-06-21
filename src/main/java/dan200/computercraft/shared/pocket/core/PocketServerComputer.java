package dan200.computercraft.shared.pocket.core;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public class PocketServerComputer extends ServerComputer implements IPocketAccess
{
    private IPocketUpgrade m_upgrade;
    private Entity m_entity;
    private ItemStack m_stack;

    public PocketServerComputer( World world, int computerID, String label, int instanceID, ComputerFamily family )
    {
        super( world, computerID, label, instanceID, family, ComputerCraft.terminalWidth_pocketComputer, ComputerCraft.terminalHeight_pocketComputer );
    }

    @Nullable
    @Override
    public Entity getEntity()
    {
        return m_entity;
    }

    @Override
    public int getColour()
    {
        return ComputerCraft.Items.pocketComputer.getColour( m_stack );
    }

    @Override
    public void setColour( int colour )
    {
        ComputerCraft.Items.pocketComputer.setColourDirect( m_stack, colour );
        updateUpgradeNBTData();
    }

    @Override
    public int getLight()
    {
        NBTTagCompound tag = getUserData();
        if( tag.hasKey( "modemLight", Constants.NBT.TAG_ANY_NUMERIC ) )
        {
            return tag.getInteger( "modemLight" );
        }
        else
        {
            return -1;
        }
    }

    @Override
    public void setLight( int colour )
    {
        NBTTagCompound tag = getUserData();
        if( colour >= 0 && colour <= 0xFFFFFF )
        {
            if( !tag.hasKey( "modemLight", Constants.NBT.TAG_ANY_NUMERIC ) || tag.getInteger( "modemLight" ) != colour )
            {
                tag.setInteger( "modemLight", colour );
                updateUserData();
            }
        }
        else if( tag.hasKey( "modemLight", Constants.NBT.TAG_ANY_NUMERIC ) )
        {
            tag.removeTag( "modemLight" );
            updateUserData();
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpgradeNBTData()
    {
        return ComputerCraft.Items.pocketComputer.getUpgradeInfo( m_stack );
    }

    @Override
    public void updateUpgradeNBTData()
    {
        InventoryPlayer inventory = m_entity instanceof EntityPlayer ? ((EntityPlayer) m_entity).inventory : null;
        if( inventory != null )
        {
            inventory.markDirty();
        }
    }

    @Override
    public void invalidatePeripheral()
    {
        IPeripheral peripheral = m_upgrade == null ? null : m_upgrade.createPeripheral( this );
        setPeripheral( 2, peripheral );
    }

    @Nonnull
    @Override
    public Map<ResourceLocation, IPeripheral> getUpgrades()
    {
        if( m_upgrade == null )
        {
            return Collections.emptyMap();
        }
        else
        {
            return Collections.singletonMap( m_upgrade.getUpgradeID(), getPeripheral( 2 ) );
        }
    }

    public IPocketUpgrade getUpgrade()
    {
        return m_upgrade;
    }

    /**
     * Set the upgrade for this pocket computer, also updating the item stack.
     *
     * Note this method is not thread safe - it must be called from the server thread.
     *
     * @param upgrade The new upgrade to set it to, may be {@code null}.
     */
    public void setUpgrade( IPocketUpgrade upgrade )
    {
        if( this.m_upgrade == upgrade ) return;

        synchronized (this)
        {
            ComputerCraft.Items.pocketComputer.setUpgrade( m_stack, upgrade );
            if( m_entity instanceof EntityPlayer ) ((EntityPlayer) m_entity).inventory.markDirty();

            this.m_upgrade = upgrade;
            invalidatePeripheral();
        }
    }

    public synchronized void updateValues( Entity entity, ItemStack stack, IPocketUpgrade upgrade )
    {
        if( entity != null )
        {
            setWorld( entity.getEntityWorld() );
            setPosition( entity.getPosition() );
        }

        m_entity = entity;
        m_stack = stack;

        if( this.m_upgrade != upgrade )
        {
            this.m_upgrade = upgrade;
            invalidatePeripheral();
        }
    }
}

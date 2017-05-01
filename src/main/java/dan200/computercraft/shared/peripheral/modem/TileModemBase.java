/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.modem;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.common.BlockGeneric;
import dan200.computercraft.shared.peripheral.common.TilePeripheralBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public abstract class TileModemBase extends TilePeripheralBase
{
	protected ModemPeripheral m_modem;

    protected TileModemBase()
    {
        m_modem = createPeripheral();
    }
    
    protected abstract ModemPeripheral createPeripheral();
            
    @Override
    public synchronized void destroy()
    {
    	if( m_modem != null )
    	{
			m_modem.destroy();
			m_modem = null;
		}
    }

    @Override
    public boolean isSolidOnSide( int side )
    {
        return false;
    }

    @Override
    public void onNeighbourChange()
    {
        EnumFacing dir = getDirection();
        if( !worldObj.isSideSolid(
            getPos().offset( dir ),
            dir.getOpposite()
        ) )
        {
            // Drop everything and remove block
            ((BlockGeneric)getBlockType()).dropAllItems( worldObj, getPos(), false );
            worldObj.setBlockToAir( getPos() );
        }
    }

    @Override
    public AxisAlignedBB getBounds()
    {
        switch( getDirection() )
        {
            case UP:
            default:
            {
                return new AxisAlignedBB( 0.125, 0.8125, 0.125, 0.875, 1.0, 0.875 );
            }
            case DOWN:
            {
                return new AxisAlignedBB( 0.125, 0.0, 0.125, 0.875, 0.1875, 0.875 );
            }
            case NORTH:
            {
                return new AxisAlignedBB( 0.125, 0.125, 0.0, 0.875, 0.875, 0.1875 );
            }
            case SOUTH:
            {
                return new AxisAlignedBB( 0.125, 0.125, 0.8125, 0.875, 0.875, 1.0 );
            }
            case WEST:
            {
                return new AxisAlignedBB( 0.0, 0.125, 0.125, 0.1875, 0.875, 0.875 );
            }
            case EAST:
            {
                return new AxisAlignedBB( 0.8125, 0.125, 0.125, 1.0, 0.875, 0.875 );
            }
        }
    }

    @Override
    public void update()
    {
    	super.update();
		if( !worldObj.isRemote && m_modem.pollChanged() )
		{
			updateAnim();
		}
	}
	
	protected void updateAnim()
	{
		if( m_modem.isActive() )
		{
			setAnim(1);
		}
		else
		{
			setAnim(0);
		}
    }

    @Override
    public final void readDescription( NBTTagCompound nbttagcompound )
    {
        super.readDescription( nbttagcompound );
        updateBlock();
    }

    // IPeripheralTile implementation

    @Override
    public IPeripheral getPeripheral( EnumFacing side )
    {
        if( side == getDirection() )
        {
            return m_modem;
        }
        return null;
    }

    protected boolean isAttached()
    {
    	return (m_modem != null) && (m_modem.getComputer() != null);
    }
}

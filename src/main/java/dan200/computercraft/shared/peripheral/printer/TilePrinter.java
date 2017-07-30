/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.peripheral.printer;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.TilePeripheralBase;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class TilePrinter extends TilePeripheralBase
    implements IInventory, ISidedInventory
{
    // Statics

    private static final int[] bottomSlots = { 7, 8, 9, 10, 11, 12 };
    private static final int[] topSlots = { 1, 2, 3, 4, 5, 6 };
    private static final int[] sideSlots = { 0 };

    // Members

    private final ItemStack[] m_inventory;
    private final IItemHandlerModifiable m_itemHandlerAll = new InvWrapper( this );
    private IItemHandlerModifiable[] m_itemHandlerSides;
    
    private final Terminal m_page;
    private String m_pageTitle;
    private boolean m_printing;

    public TilePrinter()
    {
        m_inventory = new ItemStack[13];
        m_page = new Terminal( ItemPrintout.LINE_MAX_LENGTH, ItemPrintout.LINES_PER_PAGE );
        m_pageTitle = "";
        m_printing = false;
    }

    @Override
    public void destroy()
    {
        ejectContents();
    }

    @Override
    public boolean onActivate( EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ )
    {
        if( !player.isSneaking() )
        {
            if( !worldObj.isRemote )
            {
                ComputerCraft.openPrinterGUI( player, this );
            }
            return true;
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readFromNBT(nbttagcompound);
            
        // Read page
        synchronized( m_page )
        {
            m_printing = nbttagcompound.getBoolean( "printing" );
            m_pageTitle = nbttagcompound.getString( "pageTitle" );
            m_page.readFromNBT( nbttagcompound );
        }
        
        // Read inventory
        synchronized( m_inventory )
        {
            NBTTagList nbttaglist = nbttagcompound.getTagList( "Items", Constants.NBT.TAG_COMPOUND );
            for( int i=0; i<nbttaglist.tagCount(); ++i )
            {
                NBTTagCompound itemTag = nbttaglist.getCompoundTagAt( i );
                int j = itemTag.getByte("Slot") & 0xff;
                if (j >= 0 && j < m_inventory.length)
                {
                    m_inventory[j] = ItemStack.loadItemStackFromNBT(itemTag);
                }
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound = super.writeToNBT(nbttagcompound);

        // Write page
        synchronized( m_page )
        {
            nbttagcompound.setBoolean( "printing", m_printing );
            nbttagcompound.setString( "pageTitle", m_pageTitle );
            m_page.writeToNBT( nbttagcompound );
        }
        
        // Write inventory
        synchronized( m_inventory )
        {
            NBTTagList nbttaglist = new NBTTagList();
            for(int i=0; i<m_inventory.length; ++i)
            {
                if (m_inventory[i] != null)
                {
                    NBTTagCompound itemtag = new NBTTagCompound();
                    itemtag.setByte("Slot", (byte)i);
                    m_inventory[i].writeToNBT(itemtag);
                    nbttaglist.appendTag(itemtag);
                }
            }
            nbttagcompound.setTag("Items", nbttaglist);
        }

        return nbttagcompound;
    }

    @Override
    public final void readDescription( @Nonnull NBTTagCompound nbttagcompound )
    {
        super.readDescription( nbttagcompound );
        updateBlock();
    }

    @Override
    public boolean shouldRefresh( World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState )
    {
        return super.shouldRefresh( world, pos, oldState, newState ) || ComputerCraft.Blocks.peripheral.getPeripheralType( newState ) != PeripheralType.Printer;
    }

    public boolean isPrinting()
    {
        return m_printing;
    }

    // IInventory implementation
    
    @Override    
    public int getSizeInventory()
    {
        return m_inventory.length;
    }

    @Override    
    public ItemStack getStackInSlot(int i)
    {
        synchronized( m_inventory )
        {
            return m_inventory[i];
        }
    }

    @Override    
    public ItemStack removeStackFromSlot(int i)
    {
        synchronized( m_inventory )
        {
            ItemStack result = m_inventory[i];
            m_inventory[i] = null;
            updateAnim();
            return result;
        }
    }
    
    @Override    
    public ItemStack decrStackSize(int i, int j)
    {
        synchronized( m_inventory )
        {
            if( m_inventory[i] == null )
            {
                return null;
            }
            
            if( m_inventory[i].stackSize <= j )
            {
                ItemStack itemstack = m_inventory[i];
                m_inventory[i] = null;
                markDirty();
                updateAnim();
                return itemstack;
            }
            
            ItemStack part = m_inventory[i].splitStack(j);
            if( m_inventory[i].stackSize == 0 )
            {
                m_inventory[i] = null;
                updateAnim();
            }
            markDirty();
            return part;
        }
    }

    @Override    
    public void setInventorySlotContents( int i, ItemStack stack )
    {                    
        synchronized( m_inventory )
        {
            m_inventory[i] = stack;
            markDirty();
            updateAnim();
        }
    }

    @Override
    public void clear()
    {
        synchronized( m_inventory )
        {
            for( int i=0; i<m_inventory.length; ++i )
            {
                m_inventory[i] = null;
            }
            markDirty();
            updateAnim();
        }
    }

    @Override
    public boolean hasCustomName()
    {
        return getLabel() != null;
    }

    @Nonnull
    @Override
    public String getName()
    {
        String label = getLabel();
        if( label != null )
        {
            return label;
        }
        else
        {
            return "tile.computercraft:printer.name";
        }
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName()
    {
        if( hasCustomName() )
        {
            return new TextComponentString( getName() );
        }
        else
        {
            return new TextComponentTranslation( getName() );
        }
    }

    @Override    
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override    
    public void openInventory( @Nonnull EntityPlayer player )
    {
    }
    
    @Override    
    public void closeInventory( @Nonnull EntityPlayer player )
    {
    }

    @Override
    public boolean isItemValidForSlot( int slot, @Nonnull ItemStack itemstack )
    {
        return true;
    }

    @Override
    public boolean isUseableByPlayer( @Nonnull EntityPlayer player )
    {
        return isUsable( player, false );
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    // ISidedInventory implementation
    
    @Nonnull
    @Override
    public int[] getSlotsForFace( @Nonnull EnumFacing side )
    {
        switch( side )
        {
            case DOWN:    return bottomSlots;    // Bottom (Out tray)
            case UP:    return topSlots; // Top (In tray)
            default: return sideSlots;     // Sides (Ink)
        }
    }
    
    @Override
    public boolean canInsertItem( int slot, @Nonnull ItemStack itemstack, @Nonnull EnumFacing face )
    {
        return isItemValidForSlot( slot, itemstack );
    }

    @Override
    public boolean canExtractItem( int slot, @Nonnull ItemStack itemstack, @Nonnull EnumFacing face )
    {
        return true;
    }

    // IPeripheralTile implementation

    @Override
    public IPeripheral getPeripheral( EnumFacing side )
    {
        return new PrinterPeripheral( this );
    }

    public Terminal getCurrentPage()
    {
        if( m_printing )
        {
            return m_page;
        }
        return null;
    }

    public boolean startNewPage()
    {
        synchronized( m_inventory )
        {
            if( canInputPage() )
            {
                if( m_printing && !outputPage() )
                {
                    return false;
                }
                if( inputPage() )
                {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean endCurrentPage()
    {
        synchronized( m_inventory )
        {
            if( m_printing && outputPage() )
            {
                return true;
            }
        }
        return false;
    }

    public int getInkLevel()
    {
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory[0];
            if( inkStack != null && isInk(inkStack) )
            {
                return inkStack.stackSize;
            }
        }
        return 0;
    }

    public int getPaperLevel()
    {
        int count = 0;
        synchronized( m_inventory )
        {
            for( int i=1; i<7; ++i )
            {
                ItemStack paperStack = m_inventory[i];
                if( paperStack != null && isPaper(paperStack) )
                {
                    count += paperStack.stackSize;
                }
            }
        }
        return count;
    }

    public void setPageTitle( String title )
    {
        if( m_printing )
        {
            m_pageTitle = title;
        }
    }
    
    private boolean isInk( ItemStack stack )
    {
        return (stack.getItem() == Items.DYE);
    }

    private boolean isPaper( ItemStack stack )
    {
        Item item = stack.getItem();
        return ( item == Items.PAPER || (item instanceof ItemPrintout && ItemPrintout.getType( stack ) == ItemPrintout.Type.Single) );
    }

    private boolean canInputPage()
    {
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory[ 0 ];
            return inkStack != null && isInk( inkStack ) && getPaperLevel() > 0;
        }
    }
    
    private boolean inputPage()
    {        
        synchronized( m_inventory )
        {
            ItemStack inkStack = m_inventory[0];
            if( inkStack == null || !isInk(inkStack) )
            {
                return false;
            }
            
            for( int i=1; i<7; ++i )
            {
                ItemStack paperStack = m_inventory[i];
                if( paperStack != null && isPaper(paperStack) )
                {
                    // Decrement ink
                    inkStack.stackSize--;
                    if( inkStack.stackSize <= 0 )
                    {
                        m_inventory[0] = null;
                    }
                                        
                    // Decrement paper
                    paperStack.stackSize--;
                    if( paperStack.stackSize <= 0 )
                    {
                        m_inventory[i] = null;
                        updateAnim();
                    }
                    
                    // Setup the new page
                    int colour = inkStack.getItemDamage();
                    if( colour >= 0 && colour < 16 ) {
                        m_page.setTextColour( 15 - colour );
                    } else {
                        m_page.setTextColour( 15 );
                    }
                    
                    m_page.clear();
                    if( paperStack.getItem() instanceof ItemPrintout )
                    {
                        m_pageTitle = ItemPrintout.getTitle( paperStack );
                        String[] text = ItemPrintout.getText( paperStack );
                        String[] textColour = ItemPrintout.getColours( paperStack );
                        for( int y=0; y<m_page.getHeight(); ++y )
                        {
                            m_page.setLine( y, text[y], textColour[y], "" );
                        }
                    }
                    else
                    {
                        m_pageTitle = "";
                    }
                    m_page.setCursorPos( 0, 0 );
                    
                    markDirty();
                    m_printing = true;
                    return true;
                }
            }
            return false;
        }
    }
    
    private boolean outputPage()
    {        
        synchronized( m_page )
        {
            int height = m_page.getHeight();
            String[] lines = new String[height];
            String[] colours = new String[height];
            for( int i=0; i<height; ++i )
            {
                lines[i] = m_page.getLine(i).toString();
                colours[i] = m_page.getTextColourLine(i).toString();
            }
            
            ItemStack stack = ItemPrintout.createSingleFromTitleAndText( m_pageTitle, lines, colours );
            synchronized( m_inventory )
            {
                ItemStack remainder = InventoryUtil.storeItems( stack, m_itemHandlerAll, 7, 6, 7 );
                if( remainder == null )
                {
                    m_printing = false;
                    return true;
                }
            }
            return false;
        }
    }

    private void ejectContents()
    {
        synchronized( m_inventory )
        {
            for( int i=0; i<13; ++i ) 
            {
                ItemStack stack = m_inventory[i];
                if( stack != null )
                {
                    // Remove the stack from the inventory
                    setInventorySlotContents( i, null );
        
                    // Spawn the item in the world
                    BlockPos pos = getPos();
                    double x = (double)pos.getX() + 0.5;
                    double y = (double)pos.getY() + 0.75;
                    double z = (double)pos.getZ() + 0.5;
                    EntityItem entityitem = new EntityItem( worldObj, x, y, z, stack );
                    entityitem.motionX = worldObj.rand.nextFloat() * 0.2 - 0.1;
                    entityitem.motionY = worldObj.rand.nextFloat() * 0.2 - 0.1;
                    entityitem.motionZ = worldObj.rand.nextFloat() * 0.2 - 0.1;
                    worldObj.spawnEntityInWorld(entityitem);
                }
            }
        }
    }
    
    private void updateAnim()
    {
        synchronized( m_inventory )
        {
            int anim = 0;
            for( int i=1;i<7;++i )
            {
                ItemStack stack = m_inventory[i];
                if( stack != null && isPaper(stack) )
                {
                    anim += 1;
                    break;
                }
            }
            for( int i=7;i<13;++i )
            {
                ItemStack stack = m_inventory[i];
                if( stack != null && isPaper(stack) )
                {
                    anim += 2;
                    break;
                }
            }
            setAnim( anim );
        }
    }

    @Override
    public boolean hasCapability( @Nonnull Capability<?> capability, @Nullable EnumFacing facing )
    {
        return capability == ITEM_HANDLER_CAPABILITY || super.hasCapability( capability, facing );
    }

    @Nonnull
    @Override
    public <T> T getCapability( @Nonnull Capability<T> capability, @Nullable EnumFacing facing )
    {
        if( capability == ITEM_HANDLER_CAPABILITY )
        {
            if( facing == null )
            {
                return ITEM_HANDLER_CAPABILITY.cast( m_itemHandlerAll );
            }
            else
            {
                IItemHandlerModifiable[] handlers = m_itemHandlerSides;
                if( handlers == null ) handlers = m_itemHandlerSides = new IItemHandlerModifiable[ 6 ];

                int i = facing.ordinal();
                IItemHandlerModifiable handler = handlers[ i ];
                if( handler == null ) handler = handlers[ i ] = new SidedInvWrapper( this, facing );

                return ITEM_HANDLER_CAPABILITY.cast( handler );
            }
        }
        return super.getCapability( capability, facing );
    }
}

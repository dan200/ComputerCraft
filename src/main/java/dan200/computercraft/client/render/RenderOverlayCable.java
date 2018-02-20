package dan200.computercraft.client.render;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.BlockCable;
import dan200.computercraft.shared.peripheral.modem.TileCable;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class RenderOverlayCable
{
    private static final float EXPAND = 0.002f;
    private static final double MIN = TileCable.MIN - EXPAND;
    private static final double MAX = TileCable.MAX + EXPAND;

    @SubscribeEvent
    public void drawHighlight( DrawBlockHighlightEvent event )
    {
        if( event.getTarget().typeOfHit != RayTraceResult.Type.BLOCK ) return;

        BlockPos pos = event.getTarget().getBlockPos();
        World world = event.getPlayer().getEntityWorld();

        IBlockState state = world.getBlockState( pos );
        if( state.getBlock() != ComputerCraft.Blocks.cable ) return;

        TileEntity tile = world.getTileEntity( pos );
        if( tile == null || !(tile instanceof TileCable) ) return;

        event.setCanceled( true );
        TileCable cable = (TileCable) tile;

        PeripheralType type = cable.getPeripheralType();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0 );
        GlStateManager.color( 0.0f, 0.0f, 0.0f, 0.4f );
        GlStateManager.glLineWidth( 2.0F );
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask( false );
        GlStateManager.pushMatrix();

        EnumFacing direction = type != PeripheralType.Cable ? cable.getDirection() : null;

        {
            EntityPlayer player = event.getPlayer();
            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

            GlStateManager.translate( -x + pos.getX(), -y + pos.getY(), -z + pos.getZ() );
        }

        if( type != PeripheralType.Cable && WorldUtil.isVecInsideInclusive( cable.getModemBounds(), event.getTarget().hitVec.subtract( pos.getX(), pos.getY(), pos.getZ() ) ) )
        {
            RenderGlobal.drawSelectionBoundingBox( cable.getModemBounds(), 0, 0, 0, 0.4f );
        }
        else
        {
            int flags = 0;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin( GL11.GL_LINES, DefaultVertexFormats.POSITION );
            for( EnumFacing facing : EnumFacing.VALUES )
            {
                if( direction == facing || BlockCable.isCable( world, pos.offset( facing ) ) )
                {
                    flags |= 1 << facing.ordinal();

                    double offset = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? -EXPAND : 1 + EXPAND;
                    double centre = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? MIN : MAX;

                    // Draw the part end
                    drawLineAdjacent( buffer, facing.getAxis(), offset, MIN, MIN, MIN, MAX );
                    drawLineAdjacent( buffer, facing.getAxis(), offset, MIN, MAX, MAX, MAX );
                    drawLineAdjacent( buffer, facing.getAxis(), offset, MAX, MAX, MAX, MIN );
                    drawLineAdjacent( buffer, facing.getAxis(), offset, MAX, MIN, MIN, MIN );

                    // Draw the connecting lines to the middle
                    drawLineAlong( buffer, facing.getAxis(), MIN, MIN, offset, centre );
                    drawLineAlong( buffer, facing.getAxis(), MAX, MIN, offset, centre );
                    drawLineAlong( buffer, facing.getAxis(), MAX, MAX, offset, centre );
                    drawLineAlong( buffer, facing.getAxis(), MIN, MAX, offset, centre );
                }
            }

            // Draw the cable core and any additional grids
            drawCore( buffer, flags, EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.Axis.Z );
            drawCore( buffer, flags, EnumFacing.WEST, EnumFacing.UP, EnumFacing.Axis.Z );
            drawCore( buffer, flags, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.Axis.Z );
            drawCore( buffer, flags, EnumFacing.EAST, EnumFacing.UP, EnumFacing.Axis.Z );

            drawCore( buffer, flags, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.Axis.Y );
            drawCore( buffer, flags, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.Axis.Y );
            drawCore( buffer, flags, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.Axis.Y );
            drawCore( buffer, flags, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.Axis.Y );

            drawCore( buffer, flags, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.Axis.X );
            drawCore( buffer, flags, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.Axis.X );
            drawCore( buffer, flags, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.Axis.X );
            drawCore( buffer, flags, EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.Axis.X );

            tessellator.draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.depthMask( true );
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void drawCore( BufferBuilder buffer, int flags, EnumFacing a, EnumFacing b, EnumFacing.Axis other )
    {
        if( ((flags >> a.ordinal()) & 1) != ((flags >> b.ordinal()) & 1) ) return;

        double offA = a.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? MIN : MAX;
        double offB = b.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? MIN : MAX;
        drawLineAlong( buffer, other, offA, offB, MIN, MAX );
    }

    /**
     * Draw a line parallel to an axis
     *
     * @param buffer The buffer to write to
     * @param axis   The axis do draw along
     * @param offA   The offset on the first "other" axis
     * @param offB   The offset on the second "other" axis
     * @param start  The start coordinate on this axis
     * @param end    The enc coordinate on this axis
     */
    private static void drawLineAlong( BufferBuilder buffer, EnumFacing.Axis axis, double offA, double offB, double start, double end )
    {
        switch( axis )
        {
            case X:
                buffer.pos( start, offA, offB ).endVertex();
                buffer.pos( end, offA, offB ).endVertex();
                break;
            case Y:
                buffer.pos( offA, start, offB ).endVertex();
                buffer.pos( offA, end, offB ).endVertex();
                break;
            case Z:
                buffer.pos( offA, offB, start ).endVertex();
                buffer.pos( offA, offB, end ).endVertex();
                break;
        }
    }

    /**
     * Draw a line perpendicular to an axis
     *
     * @param buffer The buffer to write to
     * @param axis   The axis to draw perpendicular to
     * @param offset The offset along this axis
     * @param startA The start coordinate for the first "other" axis
     * @param startB The start coordinate for the second "other" axis
     * @param endA   The end coordinate for the first "other" axis
     * @param endB   The end coordinate for the second "other" axis
     */
    private static void drawLineAdjacent( BufferBuilder buffer, EnumFacing.Axis axis, double offset, double startA, double startB, double endA, double endB )
    {
        switch( axis )
        {
            case X:
                buffer.pos( offset, startA, startB ).endVertex();
                buffer.pos( offset, endA, endB ).endVertex();
                break;
            case Y:
                buffer.pos( startA, offset, startB ).endVertex();
                buffer.pos( endA, offset, endB ).endVertex();
                break;
            case Z:
                buffer.pos( startA, startB, offset ).endVertex();
                buffer.pos( endA, endB, offset ).endVertex();
                break;
        }
    }
}

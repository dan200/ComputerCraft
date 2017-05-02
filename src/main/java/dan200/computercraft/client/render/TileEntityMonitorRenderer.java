/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.common.ClientTerminal;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class TileEntityMonitorRenderer extends TileEntitySpecialRenderer<TileMonitor>
{
    public TileEntityMonitorRenderer()
    {
    }

    @Override
    public void renderTileEntityAt( TileMonitor tileEntity, double posX, double posY, double posZ, float f, int i )
    {
        if( tileEntity != null )
        {
            renderMonitorAt( tileEntity, posX, posY, posZ, f, i );
        }
    }

    private void renderMonitorAt( TileMonitor monitor, double posX, double posY, double posZ, float f, int i )
    {
        // Render from the origin monitor
        TileMonitor origin = monitor.getOrigin();
        if( origin == null )
        {
            return;
        }

        // Ensure each monitor is rendered only once
        long renderFrame = ComputerCraft.getRenderFrame();
        if( origin.m_lastRenderFrame == renderFrame )
        {
            return;
        }
        else
        {
            origin.m_lastRenderFrame = renderFrame;
        }

        boolean redraw = origin.pollChanged();
        BlockPos monitorPos = monitor.getPos();
        BlockPos originPos = origin.getPos();
        posX += originPos.getX() - monitorPos.getX();
        posY += originPos.getY() - monitorPos.getY();
        posZ += originPos.getZ() - monitorPos.getZ();

        // Determine orientation
        EnumFacing dir = origin.getDirection();
        EnumFacing front = origin.getFront();
        float yaw = DirectionUtil.toYawAngle( dir );
        float pitch = DirectionUtil.toPitchAngle( front );

        GlStateManager.pushMatrix();
        try
        {
            // Setup initial transform
            GlStateManager.translate( posX + 0.5, posY + 0.5, posZ + 0.5 );
            GlStateManager.rotate( -yaw, 0.0f, 1.0f, 0.0f );
            GlStateManager.rotate( pitch, 1.0f, 0.0f, 0.0f );
            GlStateManager.translate(
                -0.5 + TileMonitor.RENDER_BORDER + TileMonitor.RENDER_MARGIN,
                ((double)origin.getHeight() - 0.5) - (TileMonitor.RENDER_BORDER + TileMonitor.RENDER_MARGIN),
                0.5
            );
            double xSize = (double)origin.getWidth() - 2.0 * ( TileMonitor.RENDER_MARGIN + TileMonitor.RENDER_BORDER );
            double ySize = (double)origin.getHeight() - 2.0 * ( TileMonitor.RENDER_MARGIN + TileMonitor.RENDER_BORDER );

            // Get renderers
            Minecraft mc = Minecraft.getMinecraft();
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer renderer = tessellator.getBuffer();

            // Get terminal
            ClientTerminal clientTerminal = (ClientTerminal)origin.getTerminal();
            Terminal terminal = (clientTerminal != null) ? clientTerminal.getTerminal() : null;
            redraw = redraw || (clientTerminal != null && clientTerminal.hasTerminalChanged());

            // Draw the contents
            GlStateManager.depthMask( false );
            GlStateManager.disableLighting();
            try
            {
                if( terminal != null )
                {
                    // Allocate display lists
                    if( origin.m_renderDisplayList < 0 )
                    {
                        origin.m_renderDisplayList = GL11.glGenLists( 3 );
                        redraw = true;
                    }

                    // Draw a terminal
                    boolean greyscale = !clientTerminal.isColour();
                    int width = terminal.getWidth();
                    int height = terminal.getHeight();
                    int cursorX = terminal.getCursorX();
                    int cursorY = terminal.getCursorY();
                    FixedWidthFontRenderer fontRenderer = (FixedWidthFontRenderer)ComputerCraft.getFixedWidthFontRenderer();

                    GlStateManager.pushMatrix();
                    try
                    {
                        double xScale = xSize / (double) ( width * FixedWidthFontRenderer.FONT_WIDTH );
                        double yScale = ySize / (double) ( height * FixedWidthFontRenderer.FONT_HEIGHT );
                        GlStateManager.scale( xScale, -yScale, 1.0 );

                        // Draw background
                        mc.getTextureManager().bindTexture( FixedWidthFontRenderer.background );

                        double marginXSize = TileMonitor.RENDER_MARGIN / xScale;
                        double marginYSize = TileMonitor.RENDER_MARGIN / yScale;
                        double marginSquash = marginYSize / (double) FixedWidthFontRenderer.FONT_HEIGHT;

                        // Top and bottom margins
                        GlStateManager.pushMatrix();
                        try
                        {
                            renderer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR );

                            fontRenderer.drawStringBackgroundPart(
                                    renderer, 0, (int) (-marginYSize / marginSquash),
                                    terminal.getBackgroundColourLine( 0 ),
                                    marginXSize, marginXSize,
                                    greyscale
                            );

                            fontRenderer.drawStringBackgroundPart(
                                    renderer, 0, (int) ((-marginYSize + height * FixedWidthFontRenderer.FONT_HEIGHT) / marginSquash),
                                    terminal.getBackgroundColourLine( height - 1 ),
                                    marginXSize, marginXSize,
                                    greyscale
                            );

                            GlStateManager.scale( 1.0, marginSquash, 1.0 );
                            tessellator.draw();
                        }
                        finally
                        {
                            GlStateManager.popMatrix();
                        }

                        // Backgrounds
                        renderer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR );

                        for( int y = 0; y < height; ++y )
                        {
                            fontRenderer.drawStringBackgroundPart(
                                    renderer,
                                    0, FixedWidthFontRenderer.FONT_HEIGHT * y,
                                    terminal.getBackgroundColourLine( y ),
                                    marginXSize, marginXSize,
                                    greyscale
                            );
                        }

                        tessellator.draw();

                        // Draw text
                        mc.getTextureManager().bindTexture( FixedWidthFontRenderer.font );

                        renderer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR );

                        // Lines
                        for( int y = 0; y < height; ++y )
                        {
                            fontRenderer.drawStringTextPart(
                                    renderer,
                                    0, FixedWidthFontRenderer.FONT_HEIGHT * y,
                                    terminal.getLine( y ),
                                    terminal.getTextColourLine( y ),
                                    greyscale
                            );
                        }

                        tessellator.draw();

                        // Draw cursor
                        mc.getTextureManager().bindTexture( FixedWidthFontRenderer.font );

                        // Cursor
                        if( ComputerCraft.getGlobalCursorBlink() && terminal.getCursorBlink() && cursorX >= 0 && cursorX < width && cursorY >= 0 && cursorY < height )
                        {
                            TextBuffer cursor = new TextBuffer( "_" );
                            TextBuffer cursorColour = new TextBuffer( "0123456789abcdef".charAt( terminal.getTextColour() ), 1 );

                            fontRenderer.drawString(
                                    cursor,
                                    FixedWidthFontRenderer.FONT_WIDTH * cursorX,
                                    FixedWidthFontRenderer.FONT_HEIGHT * cursorY,
                                    cursorColour, null,
                                    0, 0,
                                    greyscale
                            );
                        }
                    }
                    finally
                    {
                        GlStateManager.popMatrix();
                    }
                }
                else
                {
                    // Draw a big black quad
                    mc.getTextureManager().bindTexture( FixedWidthFontRenderer.background );
                    final Colour colour = Colour.Black;

                    final float r = colour.getR();
                    final float g = colour.getG();
                    final float b = colour.getB();

                    renderer.begin( GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR );
                    // Top left
                    renderer.pos( -TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 0.0, 0.0 ).color( r, g, b, 1.0f ).endVertex();
                    // Bottom left
                    renderer.pos( -TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 0.0, 1.0 ).color( r, g, b, 1.0f ).endVertex();
                    // Top right
                    renderer.pos( xSize + TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 1.0, 0.0 ).color( r, g, b, 1.0f ).endVertex();
                    // Bottom right
                    renderer.pos( xSize + TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 1.0, 1.0 ).color( r, g, b, 1.0f ).endVertex();
                    tessellator.draw();
                }
            }
            finally
            {
                GlStateManager.depthMask( true );
                GlStateManager.enableLighting();
            }

            // Draw the depth blocker
            GlStateManager.colorMask( false, false, false, false );
            try
            {
                mc.getTextureManager().bindTexture( FixedWidthFontRenderer.background );
                renderer.begin( GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX );
                // Top left
                renderer.pos( -TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 0.0, 0.0 ).endVertex();
                // Bottom left
                renderer.pos( -TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 0.0, 1.0 ).endVertex();
                // Top right
                renderer.pos( xSize + TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 1.0, 0.0 ).endVertex();
                // Bottom right
                renderer.pos( xSize + TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 1.0, 1.0 ).endVertex();
                tessellator.draw();
            }
            finally
            {
                GlStateManager.colorMask( true, true, true, true );
            }
        }
        finally
        {
            GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
            GlStateManager.popMatrix();
        }
    }
}

/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.client.gui.FontDefinition;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.common.ClientTerminal;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.Palette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileEntityMonitorRenderer extends TileEntitySpecialRenderer<TileMonitor>
{
    public TileEntityMonitorRenderer()
    {
    }

    @Override
    public void render( TileMonitor tileEntity, double posX, double posY, double posZ, float f, int i, float f2 )
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

    	final FontDefinition font = (FontDefinition) ComputerCraft.getFont(origin.getFontName());
		
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
            BufferBuilder renderer = tessellator.getBuffer();

            // Get terminal
            ClientTerminal clientTerminal = (ClientTerminal)origin.getTerminal();
            Terminal terminal = (clientTerminal != null) ? clientTerminal.getTerminal() : null;
            redraw = redraw || (clientTerminal != null && clientTerminal.hasTerminalChanged());

            // Draw the contents
            GlStateManager.depthMask( false );
            GlStateManager.disableLighting();
            mc.entityRenderer.disableLightmap();
            try
            {
                if( terminal != null )
                {
                    Palette palette = terminal.getPalette();

                    // Allocate display lists
                    if( origin.m_renderDisplayList < 0 )
                    {
                        origin.m_renderDisplayList = GlStateManager.glGenLists( 3 );
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
                        if( redraw )
                        {
                            // Build background display list
                            GlStateManager.glNewList( origin.m_renderDisplayList, GL11.GL_COMPILE );
                            try
                            {
                                double marginXSize = TileMonitor.RENDER_MARGIN / xScale;
                                double marginYSize = TileMonitor.RENDER_MARGIN / yScale;
                                double marginSquash = marginYSize / (double) FixedWidthFontRenderer.FONT_HEIGHT;

                                // Top and bottom margins
                                GlStateManager.pushMatrix();
                                try
                                {
                                    GlStateManager.scale( 1.0, marginSquash, 1.0 );
                                    GlStateManager.translate( 0.0, -marginYSize / marginSquash, 0.0 );
                                    fontRenderer.drawStringBackgroundPart( 0, 0, terminal.getBackgroundColourLine( 0 ), marginXSize, marginXSize, greyscale, palette );
                                    GlStateManager.translate( 0.0, ( marginYSize + height * FixedWidthFontRenderer.FONT_HEIGHT ) / marginSquash, 0.0 );
                                    fontRenderer.drawStringBackgroundPart( 0, 0, terminal.getBackgroundColourLine( height - 1 ), marginXSize, marginXSize, greyscale, palette );
                                }
                                finally
                                {
                                    GlStateManager.popMatrix();
                                }

                                // Backgrounds
                                for( int y = 0; y < height; ++y )
                                {
                                    fontRenderer.drawStringBackgroundPart(
                                            0, FixedWidthFontRenderer.FONT_HEIGHT * y,
                                            terminal.getBackgroundColourLine( y ),
                                            marginXSize, marginXSize,
                                            greyscale,
                                            palette
                                    );
                                }
                            }
                            finally
                            {
                                GlStateManager.glEndList();
                            }
                        }
                        GlStateManager.callList( origin.m_renderDisplayList );
                        GlStateManager.resetColor();

                        // Draw text
                        fontRenderer.bindFont(font);
                        if( redraw )
                        {
                            // Build text display list
                            GlStateManager.glNewList( origin.m_renderDisplayList + 1, GL11.GL_COMPILE );
                            try
                            {
                                // Lines
                                for( int y = 0; y < height; ++y )
                                {
                                    fontRenderer.drawStringTextPart(
                                    		font, 
                                            0, FixedWidthFontRenderer.FONT_HEIGHT * y,
                                            terminal.getLine( y ),
                                            terminal.getTextColourLine( y ),
                                            greyscale,
                                            palette
                                    );
                                }
                            }
                            finally
                            {
                                GlStateManager.glEndList();
                            }
                        }
                        GlStateManager.callList( origin.m_renderDisplayList + 1 );
                        GlStateManager.resetColor();

                        // Draw cursor
                        fontRenderer.bindFont(font);
                        if( redraw )
                        {
                            // Build cursor display list
                            GlStateManager.glNewList( origin.m_renderDisplayList + 2, GL11.GL_COMPILE );
                            try
                            {
                                // Cursor
                                if( terminal.getCursorBlink() && cursorX >= 0 && cursorX < width && cursorY >= 0 && cursorY < height )
                                {
                                    TextBuffer cursor = new TextBuffer( "_" );
                                    TextBuffer cursorColour = new TextBuffer( "0123456789abcdef".charAt( terminal.getTextColour() ), 1 );
                                    fontRenderer.drawString(
                                    		font, 
                                            cursor,
                                            FixedWidthFontRenderer.FONT_WIDTH * cursorX,
                                            FixedWidthFontRenderer.FONT_HEIGHT * cursorY,
                                            cursorColour, null,
                                            0, 0,
                                            greyscale,
                                            palette
                                    );
                                }
                            }
                            finally
                            {
                                GlStateManager.glEndList();
                            }
                        }
                        if( ComputerCraft.getGlobalCursorBlink() )
                        {
                            GlStateManager.callList( origin.m_renderDisplayList + 2 );
                            GlStateManager.resetColor();
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
                    renderer.pos( -TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 0.0, 0.0 ).color( r, g, b, 1.0f ).endVertex();
                    renderer.pos( -TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 0.0, 1.0 ).color( r, g, b, 1.0f ).endVertex();
                    renderer.pos( xSize + TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 1.0, 0.0 ).color( r, g, b, 1.0f ).endVertex();
                    renderer.pos( xSize + TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 1.0, 1.0 ).color( r, g, b, 1.0f ).endVertex();
                    tessellator.draw();
                }
            }
            finally
            {
                GlStateManager.depthMask( true );
                mc.entityRenderer.enableLightmap();
                GlStateManager.enableLighting();
            }

            // Draw the depth blocker
            GlStateManager.colorMask( false, false, false, false );
            try
            {
                mc.getTextureManager().bindTexture( FixedWidthFontRenderer.background );
                renderer.begin( GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION );
                renderer.pos( -TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0 ).endVertex();
                renderer.pos( -TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).endVertex();
                renderer.pos( xSize + TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0 ).endVertex();
                renderer.pos( xSize + TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).endVertex();
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

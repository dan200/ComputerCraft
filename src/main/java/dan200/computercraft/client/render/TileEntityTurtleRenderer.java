/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.entity.TurtleVisionCamera;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.Holiday;
import dan200.computercraft.shared.util.HolidayUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import java.util.Iterator;
import java.util.List;

public class TileEntityTurtleRenderer extends TileEntitySpecialRenderer<TileTurtle>
{
    private static ModelResourceLocation NORMAL_TURTLE_MODEL = new ModelResourceLocation( "computercraft:CC-Turtle", "inventory" );
    private static ModelResourceLocation ADVANCED_TURTLE_MODEL = new ModelResourceLocation( "computercraft:CC-TurtleAdvanced", "inventory" );
    private static ModelResourceLocation[] COLOUR_TURTLE_MODELS = new ModelResourceLocation[] {
        new ModelResourceLocation( "computercraft:turtle_black", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_red", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_green", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_brown", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_blue", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_purple", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_cyan", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_lightGrey", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_grey", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_pink", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_lime", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_yellow", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_lightBlue", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_magenta", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_orange", "inventory" ),
        new ModelResourceLocation( "computercraft:turtle_white", "inventory" ),
    };
    private static ModelResourceLocation BEGINNER_TURTLE_MODEL = new ModelResourceLocation( "computercraftedu:CC-TurtleJunior", "inventory" );
    private static ModelResourceLocation[] BEGINNER_TURTLE_COLOUR_MODELS = new ModelResourceLocation[] {
        new ModelResourceLocation( "computercraftedu:turtleJunior_black", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_red", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_green", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_brown", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_blue", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_purple", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_cyan", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_lightGrey", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_grey", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_pink", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_lime", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_yellow", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_lightBlue", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_magenta", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_orange", "inventory" ),
        new ModelResourceLocation( "computercraftedu:turtleJunior_white", "inventory" ),
    };
    private static ModelResourceLocation ELF_OVERLAY_MODEL = new ModelResourceLocation( "computercraft:turtle_elf_overlay", "inventory" );

    public TileEntityTurtleRenderer()
    {
    }

    @Override
    public void renderTileEntityAt( TileTurtle tileEntity, double posX, double posY, double posZ, float f, int i )
    {
        if( tileEntity != null )
        {
            // Check the turtle isn't first person
            Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
            if( viewEntity != null && viewEntity instanceof TurtleVisionCamera )
            {
                TurtleVisionCamera camera = (TurtleVisionCamera)viewEntity;
                if( camera.getTurtle() == tileEntity.getAccess() )
                {
                    return;
                }
            }

            // Render the turtle
            renderTurtleAt( tileEntity, posX, posY, posZ, f, i );
        }
    }

    public static ModelResourceLocation getTurtleModel( ComputerFamily family, Colour colour )
    {
        switch( family )
        {
            case Normal:
            default:
            {
                if( colour != null )
                {
                    return COLOUR_TURTLE_MODELS[ colour.ordinal() ];
                }
                else
                {
                    return NORMAL_TURTLE_MODEL;
                }
            }
            case Advanced:
            {
                if( colour != null )
                {
                    return COLOUR_TURTLE_MODELS[ colour.ordinal() ];
                }
                else
                {
                    return ADVANCED_TURTLE_MODEL;
                }
            }
            case Beginners:
            {
                if( colour != null )
                {
                    return BEGINNER_TURTLE_COLOUR_MODELS[ colour.ordinal() ];
                }
                else
                {
                    return BEGINNER_TURTLE_MODEL;
                }
            }
        }
    }

    public static ModelResourceLocation getTurtleOverlayModel( ComputerFamily family, ResourceLocation overlay, boolean christmas )
    {
        if( overlay != null )
        {
            return new ModelResourceLocation( overlay, "inventory" );
        }
        else if( christmas && family != ComputerFamily.Beginners )
        {
            return ELF_OVERLAY_MODEL;
        }
        else
        {
            return null;
        }
    }

    private void renderTurtleAt( TileTurtle turtle, double posX, double posY, double posZ, float f, int i )
    {
        GlStateManager.pushMatrix();
        try
        {
            // Setup the transform
            Vec3 offset;
            float yaw;
            if( turtle != null )
            {
                offset = turtle.getRenderOffset( f );
                yaw = turtle.getRenderYaw( f );
            }
            else
            {
                offset = new Vec3( 0.0, 0.0, 0.0 );
                yaw = 0.0f;
            }
            GlStateManager.translate( posX + offset.xCoord, posY + offset.yCoord, posZ + offset.zCoord );

            // Render the label
            IComputer computer = (turtle != null) ? turtle.getComputer() : null;
            String label = (computer != null) ? computer.getLabel() : null;
            if( label != null )
            {
                renderLabel( turtle.getAccess().getPosition(), label );
            }

            // Render the turtle
            GlStateManager.translate( 0.5f, 0.0f, 0.5f );
            GlStateManager.rotate( 180.0f - yaw, 0.0f, 1.0f, 0.0f );
            GlStateManager.translate( -0.5f, 0.0f, -0.5f );

            // Render the turtle
            Colour colour;
            ComputerFamily family;
            ResourceLocation overlay;
            if( turtle != null )
            {
                colour = turtle.getColour();
                family = turtle.getFamily();
                overlay = turtle.getOverlay();
            }
            else
            {
                colour = null;
                family = ComputerFamily.Normal;
                overlay = null;
            }
            renderModel( getTurtleModel( family, colour ) );

            // Render the overlay
            ModelResourceLocation overlayModel = getTurtleOverlayModel(
                family,
                overlay,
                HolidayUtil.getCurrentHoliday() == Holiday.Christmas
            );
            if( overlayModel != null )
            {
                GlStateManager.disableCull();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
                try
                {
                    renderModel( overlayModel );
                }
                finally
                {
                    GlStateManager.disableBlend();
                    GlStateManager.enableCull();
                }
            }

            // Render the upgrades
            if( turtle != null )
            {
                renderUpgrade( turtle, TurtleSide.Left, f );
                renderUpgrade( turtle, TurtleSide.Right, f );
            }
        }
        finally
        {
            GlStateManager.popMatrix();
        }
    }

    private void renderUpgrade( TileTurtle turtle, TurtleSide side, float f )
    {
        ITurtleUpgrade upgrade = turtle.getUpgrade( side );
        if( upgrade != null )
        {
            GlStateManager.pushMatrix();
            try
            {
                float toolAngle = turtle.getToolRenderAngle( side, f );
                GlStateManager.translate( 0.0f, 0.5f, 0.5f );
                GlStateManager.rotate( -toolAngle, 1.0f, 0.0f, 0.0f );
                GlStateManager.translate( 0.0f, -0.5f, -0.5f );

                Pair<IBakedModel, Matrix4f> pair  = upgrade.getModel( turtle.getAccess(), side );
                if( pair != null )
                {
                    if( pair.getRight() != null )
                    {
                        ForgeHooksClient.multiplyCurrentGlMatrix( pair.getRight() );
                    }
                    if( pair.getLeft() != null )
                    {
                        renderModel( pair.getLeft() );
                    }
                }
            }
            finally
            {
                GlStateManager.popMatrix();
            }
        }
    }

    private void renderModel( ModelResourceLocation modelLocation )
    {
        Minecraft mc = Minecraft.getMinecraft();
        ModelManager modelManager = mc.getRenderItem().getItemModelMesher().getModelManager();
        renderModel( modelManager.getModel( modelLocation ) );
    }

    private void renderModel( IBakedModel model )
    {
        if( model instanceof IFlexibleBakedModel )
        {
            renderModel( (IFlexibleBakedModel) model );
        }
        else
        {
            renderModel( new IFlexibleBakedModel.Wrapper( model, DefaultVertexFormats.ITEM ) );
        }
    }

    private void renderModel( IFlexibleBakedModel model )
    {
        Minecraft mc = Minecraft.getMinecraft();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        mc.getTextureManager().bindTexture( TextureMap.locationBlocksTexture );
        renderer.begin( GL11.GL_QUADS, model.getFormat() );
        for( EnumFacing facing : EnumFacing.VALUES )
        {
            renderQuads( renderer, model.getFaceQuads( facing ) );
        }
        renderQuads( renderer, model.getGeneralQuads() );
        tessellator.draw();
    }

    private void renderQuads( WorldRenderer renderer, List quads )
    {
        int color = 0xFFFFFFFF;
        Iterator it = quads.iterator();
        while( it.hasNext() )
        {
            BakedQuad quad = (BakedQuad)it.next();
            net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor( renderer, quad, color );
        }
    }

    private void renderLabel( BlockPos position, String label )
    {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition mop = mc.objectMouseOver;
        if( mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mop.getBlockPos().equals( position ) )
        {
            RenderManager renderManager = mc.getRenderManager();
            FontRenderer fontrenderer = renderManager.getFontRenderer();
            float scale = 0.016666668F * 1.6f;

            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
            try
            {
                GlStateManager.translate( 0.5f, 1.25f, 0.5f );
                GlStateManager.rotate( -renderManager.playerViewY, 0.0F, 1.0F, 0.0F );
                GlStateManager.rotate( renderManager.playerViewX, 1.0F, 0.0F, 0.0F );
                GlStateManager.scale( -scale, -scale, scale );

                int yOffset = 0;
                int xOffset = fontrenderer.getStringWidth( label ) / 2;

                // Draw background
                GlStateManager.depthMask( false );
                GlStateManager.disableDepth();
                try
                {
                    // Quad
                    GlStateManager.disableTexture2D();
                    try
                    {
                        Tessellator tessellator = Tessellator.getInstance();
                        WorldRenderer renderer = tessellator.getWorldRenderer();
                        renderer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR );
                        renderer.pos( (double) ( -xOffset - 1 ), (double) ( -1 + yOffset ), 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).endVertex();
                        renderer.pos( (double) ( -xOffset - 1 ), (double) ( 8 + yOffset ), 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).endVertex();
                        renderer.pos( (double) ( xOffset + 1 ), (double) ( 8 + yOffset ), 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).endVertex();
                        renderer.pos( (double) ( xOffset + 1 ), (double) ( -1 + yOffset ), 0.0D ).color( 0.0F, 0.0F, 0.0F, 0.25F ).endVertex();
                        tessellator.draw();
                    }
                    finally
                    {
                        GlStateManager.enableTexture2D();
                    }

                    // Text
                    fontrenderer.drawString( label, -fontrenderer.getStringWidth( label ) / 2, yOffset, 0x20ffffff );
                }
                finally
                {
                    GlStateManager.enableDepth();
                    GlStateManager.depthMask( true );
                }

                // Draw foreground text
                fontrenderer.drawString( label, -fontrenderer.getStringWidth( label ) / 2, yOffset, -1 );
            }
            finally
            {
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }
}

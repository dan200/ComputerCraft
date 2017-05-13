/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import com.google.common.base.Objects;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.ItemTurtleBase;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.Holiday;
import dan200.computercraft.shared.util.HolidayUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TurtleSmartItemModel implements IBakedModel, IResourceManagerReloadListener
{
    private static class TurtleModelCombination
    {
        public final ComputerFamily m_family;
        public final Colour m_colour;
        public final ITurtleUpgrade m_leftUpgrade;
        public final ITurtleUpgrade m_rightUpgrade;
        public final ResourceLocation m_overlay;
        public final boolean m_christmas;

        public TurtleModelCombination( ComputerFamily family, Colour colour, ITurtleUpgrade leftUpgrade, ITurtleUpgrade rightUpgrade, ResourceLocation overlay, boolean christmas )
        {
            m_family = family;
            m_colour = colour;
            m_leftUpgrade = leftUpgrade;
            m_rightUpgrade = rightUpgrade;
            m_overlay = overlay;
            m_christmas = christmas;
        }

        @Override
        public boolean equals( Object other )
        {
            if( other == this ) {
                return true;
            }
            if( other instanceof TurtleModelCombination ) {
                TurtleModelCombination otherCombo = (TurtleModelCombination)other;
                if( otherCombo.m_family == m_family &&
                    otherCombo.m_colour == m_colour &&
                    otherCombo.m_leftUpgrade == m_leftUpgrade &&
                    otherCombo.m_rightUpgrade == m_rightUpgrade &&
                    Objects.equal( otherCombo.m_overlay, m_overlay ) &&
                    otherCombo.m_christmas == m_christmas )
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + m_family.hashCode();
            result = prime * result + (m_colour != null ? m_colour.hashCode() : 0);
            result = prime * result + (m_leftUpgrade != null ? m_leftUpgrade.hashCode() : 0);
            result = prime * result + (m_rightUpgrade != null ? m_rightUpgrade.hashCode() : 0);
            result = prime * result + (m_overlay != null ? m_overlay.hashCode() : 0);
            result = prime * result + (m_christmas ? 1 : 0);
            return result;
        }
    }

    private ItemStack m_defaultItem;
    private HashMap<TurtleModelCombination, IBakedModel> m_cachedModels;
    private ItemOverrideList m_overrides;

    public TurtleSmartItemModel()
    {
        m_defaultItem = TurtleItemFactory.create( -1, null, null, ComputerFamily.Normal, null, null, 0, null );
        m_cachedModels = new HashMap<TurtleModelCombination, IBakedModel>();
        m_overrides = new ItemOverrideList( new ArrayList<ItemOverride>() )
        {
            @Nonnull
            @Override
            public IBakedModel handleItemState( @Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity)
            {
                ItemTurtleBase turtle = (ItemTurtleBase) stack.getItem();
                ComputerFamily family = turtle.getFamily( stack );
                Colour colour = turtle.getColour( stack );
                ITurtleUpgrade leftUpgrade = turtle.getUpgrade( stack, TurtleSide.Left );
                ITurtleUpgrade rightUpgrade = turtle.getUpgrade( stack, TurtleSide.Right );
                ResourceLocation overlay = turtle.getOverlay( stack );
                boolean christmas = HolidayUtil.getCurrentHoliday() == Holiday.Christmas;
                TurtleModelCombination combo = new TurtleModelCombination( family, colour, leftUpgrade, rightUpgrade, overlay, christmas );
                if( m_cachedModels.containsKey( combo ) )
                {
                    return m_cachedModels.get( combo );
                }
                else
                {
                    IBakedModel model = buildModel( combo );
                    m_cachedModels.put( combo, model );
                    return model;
                }
            }
        };
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides()
    {
        return m_overrides;
    }

    @Override
    public void onResourceManagerReload( @Nonnull IResourceManager resourceManager )
    {
        m_cachedModels.clear();
    }

    private IBakedModel buildModel( TurtleModelCombination combo )
    {
        Minecraft mc = Minecraft.getMinecraft();
        ModelManager modelManager = mc.getRenderItem().getItemModelMesher().getModelManager();
        ModelResourceLocation baseModelLocation = TileEntityTurtleRenderer.getTurtleModel( combo.m_family, combo.m_colour );
        ModelResourceLocation overlayModelLocation = TileEntityTurtleRenderer.getTurtleOverlayModel( combo.m_family, combo.m_overlay, combo.m_christmas );
        IBakedModel baseModel = modelManager.getModel( baseModelLocation );
        IBakedModel overlayModel = (overlayModelLocation != null) ? modelManager.getModel( baseModelLocation ) : null;
        Pair<IBakedModel, Matrix4f> leftModel = (combo.m_leftUpgrade != null) ? combo.m_leftUpgrade.getModel( null, TurtleSide.Left ) : null;
        Pair<IBakedModel, Matrix4f> rightModel = (combo.m_rightUpgrade != null) ? combo.m_rightUpgrade.getModel( null, TurtleSide.Right ) : null;
        if( leftModel != null && rightModel != null )
        {
            return new TurtleMultiModel( baseModel, overlayModel, leftModel.getLeft(), leftModel.getRight(), rightModel.getLeft(), rightModel.getRight() );
        }
        else if( leftModel != null )
        {
            return new TurtleMultiModel( baseModel, overlayModel, leftModel.getLeft(), leftModel.getRight(), null, null );
        }
        else if( rightModel != null )
        {
            return new TurtleMultiModel( baseModel, overlayModel, null, null, rightModel.getLeft(), rightModel.getRight() );
        }
        else if( overlayModel != null )
        {
            return new TurtleMultiModel( baseModel, overlayModel, null, null, null, null );
        }
        else
        {
            return baseModel;
        }
    }

    // These should not be called:

    @Nonnull
    @Override
    public List<BakedQuad> getQuads( IBlockState state, EnumFacing facing, long rand )
    {
        return getDefaultModel().getQuads( state, facing, rand );
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return getDefaultModel().isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return getDefaultModel().isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return getDefaultModel().isBuiltInRenderer();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return getDefaultModel().getParticleTexture();
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return getDefaultModel().getItemCameraTransforms();
    }

    private IBakedModel getDefaultModel()
    {
        return m_overrides.handleItemState( this, m_defaultItem, null, null );
    }
}

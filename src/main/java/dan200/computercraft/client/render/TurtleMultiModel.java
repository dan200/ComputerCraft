package dan200.computercraft.client.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.List;

public class TurtleMultiModel implements IFlexibleBakedModel
{
    private IFlexibleBakedModel m_baseModel;
    private IFlexibleBakedModel m_overlayModel;
    private IFlexibleBakedModel m_leftUpgradeModel;
    private IFlexibleBakedModel m_rightUpgradeModel;

    private List<BakedQuad> m_generalQuads;
    private List<BakedQuad>[] m_faceQuads;

    public TurtleMultiModel( IBakedModel baseModel, IBakedModel overlayModel, IBakedModel leftUpgradeModel, Matrix4f leftUpgradeTransform, IBakedModel rightUpgradeModel, Matrix4f rightUpgradeTransform )
    {
        // Get the models
        m_baseModel = makeFlexible( baseModel );
        m_overlayModel = makeFlexible( overlayModel );
        m_leftUpgradeModel = makeFlexible( leftUpgradeModel );
        m_rightUpgradeModel = makeFlexible( rightUpgradeModel );

        // Bake the quads
        m_generalQuads = new ArrayList<BakedQuad>();
        m_generalQuads.addAll( m_baseModel.getGeneralQuads() );
        if( m_overlayModel != null )
        {
            m_generalQuads.addAll( m_overlayModel.getGeneralQuads() );
        }
        if( m_leftUpgradeModel != null )
        {
            m_generalQuads.addAll( transformQuads( m_leftUpgradeModel.getFormat(), m_leftUpgradeModel.getGeneralQuads(), leftUpgradeTransform ) );
        }
        if( m_rightUpgradeModel != null )
        {
            m_generalQuads.addAll( transformQuads( m_rightUpgradeModel.getFormat(), m_rightUpgradeModel.getGeneralQuads(), rightUpgradeTransform ) );
        }

        m_faceQuads = new List[ EnumFacing.VALUES.length ];
        for( EnumFacing facing : EnumFacing.VALUES )
        {
            List<BakedQuad> faces = new ArrayList<BakedQuad>();
            faces.addAll( m_baseModel.getFaceQuads( facing ) );
            if( m_overlayModel != null )
            {
                faces.addAll( m_overlayModel.getFaceQuads( facing ) );
            }
            if( m_leftUpgradeModel != null )
            {
                faces.addAll( transformQuads( m_leftUpgradeModel.getFormat(), m_leftUpgradeModel.getFaceQuads( facing ), leftUpgradeTransform ) );
            }
            if( m_rightUpgradeModel != null )
            {
                faces.addAll( transformQuads( m_rightUpgradeModel.getFormat(), m_rightUpgradeModel.getFaceQuads( facing ), rightUpgradeTransform ) );
            }
            m_faceQuads[ facing.getIndex() ] = faces;
        }
    }

    @Override
    public List<BakedQuad> getFaceQuads( EnumFacing side )
    {
        return m_faceQuads[ side.getIndex() ];
    }

    @Override
    public List<BakedQuad> getGeneralQuads()
    {
        return m_generalQuads;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return m_baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return m_baseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return m_baseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return m_baseModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return m_baseModel.getItemCameraTransforms();
    }

    @Override
    public VertexFormat getFormat()
    {
        return m_baseModel.getFormat();
    }

    private List<BakedQuad> transformQuads( VertexFormat format, List<BakedQuad> input, Matrix4f transform )
    {
        if( transform == null || input.size() == 0 )
        {
            return input;
        }
        else
        {
            List<BakedQuad> output = new ArrayList<BakedQuad>( input.size() );
            for( int i=0; i<input.size(); ++i )
            {
                BakedQuad quad = input.get( i );
                output.add( transformQuad( format, quad, transform ) );
            }
            return output;
        }
    }

    private BakedQuad transformQuad( VertexFormat format, BakedQuad quad, Matrix4f transform )
    {
        int[] vertexData = quad.getVertexData().clone();
        BakedQuad copy = new BakedQuad( vertexData, quad.getTintIndex(), quad.getFace() );
        int offset = 0;
        for( int i=0; i<format.getElementCount(); ++i ) // For each vertex element
        {
            VertexFormatElement element = format.getElement( i );
            if( element.isPositionElement() &&
                element.getType() == VertexFormatElement.EnumType.FLOAT &&
                element.getElementCount() == 3 ) // When we find a position element
            {
                for( int j=0; j<4; ++j ) // For each corner of the quad
                {
                    int start = offset + j * format.getNextOffset();
                    if( (start % 4) == 0 )
                    {
                        start = start / 4;

                        // Extract the position
                        Point3f pos = new Point3f(
                            Float.intBitsToFloat( vertexData[ start ] ),
                            Float.intBitsToFloat( vertexData[ start + 1 ] ),
                            Float.intBitsToFloat( vertexData[ start + 2 ] )
                        );

                        // Transform the position
                        transform.transform( pos );

                        // Insert the position
                        vertexData[ start ] = Float.floatToRawIntBits( pos.x );
                        vertexData[ start + 1 ] = Float.floatToRawIntBits( pos.y );
                        vertexData[ start + 2 ] = Float.floatToRawIntBits( pos.z );
                    }
                }
            }
            offset += element.getSize();
        }
        return copy;
    }

    private IFlexibleBakedModel makeFlexible( IBakedModel model )
    {
        if( model == null )
        {
            return null;
        }
        else if( model instanceof IFlexibleBakedModel )
        {
            return (IFlexibleBakedModel)model;
        }
        else
        {
            return new IFlexibleBakedModel.Wrapper( model, DefaultVertexFormats.ITEM );
        }
    }
}

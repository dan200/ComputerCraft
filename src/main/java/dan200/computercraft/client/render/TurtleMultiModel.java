package dan200.computercraft.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.List;

public class TurtleMultiModel implements IBakedModel
{
    private IBakedModel m_baseModel;
    private IBakedModel m_overlayModel;
    private IBakedModel m_leftUpgradeModel;
    private Matrix4f m_leftUpgradeTransform;
    private IBakedModel m_rightUpgradeModel;
    private Matrix4f m_rightUpgradeTransform;
    private List<BakedQuad> m_generalQuads;
    private List<BakedQuad> m_faceQuads[];

    public TurtleMultiModel( IBakedModel baseModel, IBakedModel overlayModel, IBakedModel leftUpgradeModel, Matrix4f leftUpgradeTransform, IBakedModel rightUpgradeModel, Matrix4f rightUpgradeTransform )
    {
        // Get the models
        m_baseModel = baseModel;
        m_overlayModel = overlayModel;
        m_leftUpgradeModel = leftUpgradeModel;
        m_leftUpgradeTransform = leftUpgradeTransform;
        m_rightUpgradeModel = rightUpgradeModel;
        m_rightUpgradeTransform = rightUpgradeTransform;
        m_generalQuads = null;
        m_faceQuads = new List[6];
    }

    @Override
    public List<BakedQuad> getQuads( IBlockState state, EnumFacing side, long rand )
    {
        if( side != null )
        {
            if( m_faceQuads[ side.ordinal() ] == null )
            {
                ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>();
                if( m_overlayModel != null )
                {
                    quads.addAll( m_overlayModel.getQuads( state, side, rand ) );
                }
                if( m_leftUpgradeModel != null )
                {
                    quads.addAll( transformQuads( m_leftUpgradeModel.getQuads( state, side, rand ), m_leftUpgradeTransform ) );
                }
                if( m_rightUpgradeModel != null )
                {
                    quads.addAll( transformQuads( m_rightUpgradeModel.getQuads( state, side, rand ), m_rightUpgradeTransform ) );
                }
                quads.trimToSize();
                m_faceQuads[ side.ordinal() ] = quads;
            }
            return  m_faceQuads[ side.ordinal() ];
        }
        else
        {
            if( m_generalQuads == null )
            {
                ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>();
                quads.addAll( m_baseModel.getQuads( state, side, rand ) );
                if( m_overlayModel != null )
                {
                    quads.addAll( m_overlayModel.getQuads( state, side, rand ) );
                }
                if( m_leftUpgradeModel != null )
                {
                    quads.addAll( transformQuads( m_leftUpgradeModel.getQuads( state, side, rand ), m_leftUpgradeTransform ) );
                }
                if( m_rightUpgradeModel != null )
                {
                    quads.addAll( transformQuads( m_rightUpgradeModel.getQuads( state, side, rand ), m_rightUpgradeTransform ) );
                }
                quads.trimToSize();
                m_generalQuads = quads;
            }
            return m_generalQuads;
        }
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
    public ItemOverrideList getOverrides()
    {
        return ItemOverrideList.NONE;
    }

    private List<BakedQuad> transformQuads( List<BakedQuad> input, Matrix4f transform )
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
                output.add( transformQuad( quad, transform ) );
            }
            return output;
        }
    }

    private BakedQuad transformQuad( BakedQuad quad, Matrix4f transform )
    {
        int[] vertexData = quad.getVertexData().clone();
        int offset = 0;
        BakedQuad copy = new BakedQuad( vertexData, -1, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat() );
        VertexFormat format = copy.getFormat();
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
}

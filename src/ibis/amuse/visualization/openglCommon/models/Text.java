package ibis.amuse.visualization.openglCommon.models;

import ibis.amuse.visualization.openglCommon.GLSLAttrib;
import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.VBO;
import ibis.amuse.visualization.openglCommon.math.Vec2;
import ibis.amuse.visualization.openglCommon.math.Vec3;
import ibis.amuse.visualization.openglCommon.math.VectorMath;
import ibis.amuse.visualization.openglCommon.text.Font;
import ibis.amuse.visualization.openglCommon.text.GlyphShape;
import ibis.amuse.visualization.openglCommon.text.GlyphString;
import ibis.amuse.visualization.openglCommon.text.OutlineShape;
import ibis.amuse.visualization.openglCommon.text.Region;
import ibis.amuse.visualization.openglCommon.text.RenderState;
import ibis.amuse.visualization.openglCommon.text.RenderStateImpl;
import ibis.amuse.visualization.openglCommon.text.TypecastFont;
import ibis.amuse.visualization.openglCommon.text.VBORegion2PES2;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;

import com.jogamp.graph.geom.Triangle;
import com.jogamp.graph.geom.Vertex;
import com.jogamp.graph.geom.opengl.SVertex;
import com.jogamp.opengl.util.glsl.ShaderState;

public class Text extends Model {
    private static final boolean ENABLED = false;

    private boolean initialized = false;
    private final GlyphString cachedGlyphString = null;
    private final String cachedString = "";
    private int type = Region.VBAA_RENDERING_BIT;
    private VBORegion2PES2 region = null;
    private RenderState rs = null;

    private final FloatBuffer gca_Vertices, gca_texCoords, indices;

    public Text(Material material, int type) {
        super(material, vertex_format.TRIANGLES);
        this.type = type;

        int numVertices = 0;

        Vec3[] indices = new Vec3[numVertices];
        Vec3[] points = new Vec3[numVertices];
        Vec2[] tCoords = new Vec2[numVertices];

        this.numVertices = numVertices;
        this.gca_Vertices = VectorMath.toBuffer(points);
        this.gca_texCoords = VectorMath.toBuffer(tCoords);
        this.indices = VectorMath.toBuffer(indices);
    }

    public void drawString3D(GL2ES2 gl, TypecastFont font, String str,
            float[] position, int fontSize, int texSize, int pWidth, int pHeight) {

        if (ENABLED) {
            GlyphString glyphString = cachedGlyphString;
            if (null == glyphString || str.compareTo(cachedString) != 0) {
                rs = new RenderStateImpl(new ShaderState(), SVertex.factory());

                // Get the outline shapes for this font
                ArrayList<OutlineShape> shapes = font.getOutlineShapes(str,
                        fontSize, SVertex.factory());

                // Make a glyph string from the outlines
                glyphString = new GlyphString(
                        font.getName(Font.NAME_UNIQUNAME), str);
                glyphString.createfromOutlineShapes(SVertex.factory(), shapes);

                // Create region based on the glyph string
                region = new VBORegion2PES2(type,
                        Region.TWO_PASS_DEFAULT_TEXTURE_UNIT);

                for (int i = 0; i < glyphString.glyphs.size(); i++) {
                    final GlyphShape glyph = glyphString.glyphs.get(i);
                    ArrayList<Triangle> gtris = glyph.triangulate();
                    region.addTriangles(gtris);

                    final ArrayList<Vertex> gVertices = glyph.getVertices();
                    for (int j = 0; j < gVertices.size(); j++) {
                        final Vertex gVert = gVertices.get(j);
                        gVert.setId(numVertices++);
                        region.addVertex(gVert);
                    }
                }

                // Extract the final number of vertices from the region
                numVertices = region.getNumVertices();

                // Extract the triangles from the region and turn them into
                // indices and vertices

                // for (int i = 0; i < region.triangles.size(); i++) {
                // final Triangle t = region.triangles.get(i);
                // final Vertex[] t_vertices = t.getVertices();
                //
                // if (t_vertices[0].getId() == Integer.MAX_VALUE) {
                // t_vertices[0].setId(numVertices++);
                // t_vertices[1].setId(numVertices++);
                // t_vertices[2].setId(numVertices++);
                //
                // vertices.add(t_vertices[0]);
                // vertices.add(t_vertices[1]);
                // vertices.add(t_vertices[2]);
                //
                // indicesTxt.puts((short) t_vertices[0].getId());
                // indicesTxt.puts((short) t_vertices[1].getId());
                // indicesTxt.puts((short) t_vertices[2].getId());
                // } else {
                // indicesTxt.puts((short) t_vertices[0].getId());
                // indicesTxt.puts((short) t_vertices[1].getId());
                // indicesTxt.puts((short) t_vertices[2].getId());
                // }
                // }
                //
                // cachedGlyphString = glyphString;
                // }
                //
                // region.update(gl, rs);
                //
                // this.numVertices = region.getNumVertices();
                // this.gca_Vertices = (FloatBuffer)
                // region.verticeTxtAttr.getBuffer();
                // this.gca_texCoords = (FloatBuffer) region.texCoordTxtAttr
                // .getBuffer();
                // this.indices = (FloatBuffer) region.indicesTxt.getBuffer();
                //
                // region.drawImpl(gl, rs, pWidth, pHeight, texSize);
                //
            }
        }
    }

    @Override
    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex", 4);
            GLSLAttrib nAttrib = new GLSLAttrib(normals, "MCnormal", 3);
            GLSLAttrib tAttrib = new GLSLAttrib(texCoords, "MCtexCoord", 3);

            vbo = new VBO(gl, vAttrib, nAttrib, tAttrib);
        }
        initialized = true;
    }

}

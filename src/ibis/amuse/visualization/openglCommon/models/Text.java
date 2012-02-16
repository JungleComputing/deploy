package ibis.amuse.visualization.openglCommon.models;

import ibis.amuse.visualization.openglCommon.GLSLAttrib;
import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.VBO;
import ibis.amuse.visualization.openglCommon.math.Mat4;
import ibis.amuse.visualization.openglCommon.math.MatrixMath;
import ibis.amuse.visualization.openglCommon.math.Vec3;
import ibis.amuse.visualization.openglCommon.math.Vec4;
import ibis.amuse.visualization.openglCommon.math.VectorMath;
import ibis.amuse.visualization.openglCommon.text.GlyphShape;
import ibis.amuse.visualization.openglCommon.text.OutlineShape;
import ibis.amuse.visualization.openglCommon.text.TypecastFont;
import ibis.amuse.visualization.openglCommon.textures.Texture2D;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import com.jogamp.graph.geom.Triangle;
import com.jogamp.graph.geom.Vertex;
import com.jogamp.graph.geom.opengl.SVertex;

public class Text extends Model {
    private boolean initialized = false;
    private String cachedString = "";
    private Texture2D intermediateTex;

    public Text(Material material) {
        super(material, vertex_format.TRIANGLES);

        int numVertices = 0;

        Vec3[] points = new Vec3[numVertices];

        this.numVertices = numVertices;
        this.vertices = VectorMath.toBuffer(points);
    }

    public void setString(GL3 gl, TypecastFont font, String str, int fontSize) {
        if (str.compareTo(cachedString) != 0) {
            // Get the outline shapes for the current string in this font
            ArrayList<OutlineShape> shapes = font.getOutlineShapes(str,
                    fontSize, SVertex.factory());

            // Make a set of glyph shapes from the outlines
            int numGlyps = shapes.size();
            ArrayList<GlyphShape> glyphs = new ArrayList<GlyphShape>();

            for (int index = 0; index < numGlyps; index++) {
                if (shapes.get(index) == null) {
                    continue;
                }
                GlyphShape glyphShape = new GlyphShape(SVertex.factory(),
                        shapes.get(index));

                if (glyphShape.getNumVertices() < 3) {
                    continue;
                }
                glyphs.add(glyphShape);
            }

            // Create list of vertices based on the glyph shapes
            ArrayList<Vertex> vertices = new ArrayList<Vertex>();
            for (int i = 0; i < glyphs.size(); i++) {
                GlyphShape glyph = glyphs.get(i);

                ArrayList<Triangle> gtris = glyph.triangulate();
                for (Triangle t : gtris) {
                    vertices.add(t.getVertices()[0]);
                    vertices.add(t.getVertices()[1]);
                    vertices.add(t.getVertices()[2]);
                }
            }

            // Transform the vertices from Vertex objects to Vec4 objects and
            // update BoundingBox.
            Vec4[] myVertices = new Vec4[vertices.size()];
            BoundingBox bbox = new BoundingBox();
            int i = 0;
            for (Vertex v : vertices) {
                Vec3 vec = new Vec3(v.getX(), v.getY(), v.getZ());
                bbox.resize(vec);

                myVertices[i++] = new Vec4(vec, 1f);
            }

            // intermediateTex = new PostProcessTexture(bbox.getWidth(), bbox
            // .getHeight(), i);

            // TODO: DEBUG
            // System.out.println("Bounding Box:");
            // try {
            // System.out.println(bbox.getMin());
            // System.out.println(bbox.getMax());
            // } catch (UninitializedException e) {
            // e.printStackTrace();
            // }

            this.vertices = VectorMath.toBuffer(myVertices);
            this.numVertices = vertices.size();
            this.cachedString = str;
        }
        GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex", 4);
        vbo.delete(gl);
        vbo = new VBO(gl, vAttrib);

        initialized = true;
    }

    @Override
    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex", 4);
            vbo = new VBO(gl, vAttrib);
        }
        initialized = true;
    }

    public static Mat4 getModelViewForHUD(float RasterPosX, float RasterPosY) {
        Mat4 mv = new Mat4();
        mv = mv.mul(MatrixMath.translate(-.55f + RasterPosX,
                -.35f + RasterPosY, -1f));

        return mv;
    }

}

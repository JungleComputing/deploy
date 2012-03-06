package ibis.amuse.visualization.openglCommon.models;

import ibis.amuse.visualization.openglCommon.FBO;
import ibis.amuse.visualization.openglCommon.GLSLAttrib;
import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.VBO;
import ibis.amuse.visualization.openglCommon.exceptions.CompilationFailedException;
import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;
import ibis.amuse.visualization.openglCommon.math.Mat4;
import ibis.amuse.visualization.openglCommon.math.MatrixMath;
import ibis.amuse.visualization.openglCommon.math.Vec2;
import ibis.amuse.visualization.openglCommon.math.Vec3;
import ibis.amuse.visualization.openglCommon.math.Vec4;
import ibis.amuse.visualization.openglCommon.math.VectorMath;
import ibis.amuse.visualization.openglCommon.models.base.Quad;
import ibis.amuse.visualization.openglCommon.shaders.FragmentShader;
import ibis.amuse.visualization.openglCommon.shaders.Program;
import ibis.amuse.visualization.openglCommon.shaders.VertexShader;
import ibis.amuse.visualization.openglCommon.text.GlyphShape;
import ibis.amuse.visualization.openglCommon.text.OutlineShape;
import ibis.amuse.visualization.openglCommon.text.TypecastFont;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;

import com.jogamp.graph.geom.Triangle;
import com.jogamp.graph.geom.Vertex;
import com.jogamp.graph.geom.opengl.SVertex;

public class Text extends Model {
    private boolean initialized = false;
    private boolean directRendering = false;

    private String cachedString = "";
    private FBO fbo;
    private int glMultitexUnit = 0;
    private Program curveProgram;

    private final BoundingBox bbox;
    private Quad textureQuad;
    private final FloatBuffer tCoords;

    public Text(Material material, int glMultitexUnit, boolean directRendering) {
        super(material, vertex_format.TRIANGLES);
        this.glMultitexUnit = glMultitexUnit;
        this.directRendering = directRendering;

        int numVertices = 0;

        Vec4[] points = new Vec4[numVertices];
        Vec2[] texCoords = new Vec2[numVertices];

        this.numVertices = numVertices;
        this.vertices = VectorMath.toBuffer(points);

        this.bbox = new BoundingBox();
        this.tCoords = VectorMath.toBuffer(texCoords);
    }

    public void setString(GL3 gl, Program program, TypecastFont font,
            String str, int fontSize) {
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
            int i = 0;
            for (Vertex v : vertices) {
                Vec3 vec = new Vec3(v.getX(), v.getY(), v.getZ());
                bbox.resize(vec);

                myVertices[i++] = new Vec4(vec, 1f);
            }

            vbo.delete(gl);
            this.vertices = VectorMath.toBuffer(myVertices);
            this.numVertices = vertices.size();
            this.cachedString = str;
        }
        GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex", 4);
        GLSLAttrib tAttrib = new GLSLAttrib(tCoords, "tCoords", 2);
        vbo = new VBO(gl, vAttrib, tAttrib);

        if (!directRendering) {
            // Render the whole scene to a texture using FBO's and RBO's
            int width = (int) bbox.getWidth();
            int height = (int) bbox.getHeight();

            if (fbo != null) {
                fbo.delete(gl);
            }
            fbo = new FBO(width, height, glMultitexUnit);
            fbo.init(gl);

            try {
                fbo.bind(gl);

                gl.glViewport(0, 0, width, height);
                gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT
                        | GL2ES2.GL_DEPTH_BUFFER_BIT);

                renderDirect(gl, program, new Mat4());

                fbo.unBind(gl);
            } catch (UninitializedException e) {
                e.printStackTrace();
            }

            // Create a quad to render the new texture onto
            if (!directRendering) {
                try {
                    VertexShader curveVS = new VertexShader(
                            "src/ibis/amuse/visualization/openglCommon/shaders/src/vs_curveShader.vp");
                    curveVS.init(gl);
                    FragmentShader curveFS = new FragmentShader(
                            "src/ibis/amuse/visualization/openglCommon/shaders/src/fs_curveShader.fp");
                    curveFS.init(gl);
                    curveProgram = new Program(curveVS, curveFS);
                    curveProgram.init(gl);

                    curveProgram.setUniformMatrix("PMatrix", new Mat4());
                    curveProgram.setUniform("RBOTexture", glMultitexUnit);
                    curveProgram.setUniformVector("TextureSize", new Vec2(
                            width, height));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (CompilationFailedException e) {
                    e.printStackTrace();
                }
            }
            textureQuad = new Quad(curveProgram, material, width, height,
                    new Vec3(0, 0, 0));
            textureQuad.init(gl);
        }

        initialized = true;
    }

    private void renderDirect(GL3 gl, Program program, Mat4 MVMatrix) {
        program.setUniformVector("DiffuseMaterial", material.diffuse);
        program.setUniformVector("AmbientMaterial", material.ambient);
        program.setUniformVector("SpecularMaterial", material.specular);
        program.setUniformMatrix("MVMatrix", MVMatrix);

        try {
            program.use(gl);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        vbo.bind(gl);

        program.linkAttribs(gl, vbo.getAttribs());

        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
    }

    private void renderFBO(GL3 gl, Mat4 MVMatrix) {
        curveProgram.setUniformVector("ColorStatic", new Vec3(1f, 1f, 1f));
        curveProgram.setUniform("Alpha", 1f);
        curveProgram.setUniform("Weight", 1f);

        textureQuad.draw(gl, curveProgram, MVMatrix);
    }

    @Override
    public void draw(GL3 gl, Program program, Mat4 MVMatrix) {
        if (initialized) {
            if (directRendering) {
                renderDirect(gl, program, MVMatrix);
            } else {
                renderFBO(gl, MVMatrix);
            }
        }
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

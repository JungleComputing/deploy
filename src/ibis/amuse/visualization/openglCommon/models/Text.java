package ibis.amuse.visualization.openglCommon.models;

import ibis.amuse.visualization.openglCommon.FBO;
import ibis.amuse.visualization.openglCommon.GLSLAttrib;
import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.VBO;
import ibis.amuse.visualization.openglCommon.exceptions.CompilationFailedException;
import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;
import ibis.amuse.visualization.openglCommon.math.MatF4;
import ibis.amuse.visualization.openglCommon.math.MatrixFMath;
import ibis.amuse.visualization.openglCommon.math.VecF2;
import ibis.amuse.visualization.openglCommon.math.VecF3;
import ibis.amuse.visualization.openglCommon.math.VecF4;
import ibis.amuse.visualization.openglCommon.math.VectorFMath;
import ibis.amuse.visualization.openglCommon.models.base.RBOQuad;
import ibis.amuse.visualization.openglCommon.shaders.FragmentShader;
import ibis.amuse.visualization.openglCommon.shaders.Program;
import ibis.amuse.visualization.openglCommon.shaders.VertexShader;
import ibis.amuse.visualization.openglCommon.text.GlyphShape;
import ibis.amuse.visualization.openglCommon.text.OutlineShape;
import ibis.amuse.visualization.openglCommon.text.TypecastFont;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;

import com.jogamp.graph.geom.Triangle;
import com.jogamp.graph.geom.Vertex;
import com.jogamp.graph.geom.opengl.SVertex;

public class Text extends Model {
    private boolean initialized = false;
    private boolean twoPass = false;

    private String cachedString = "";
    private FBO fbo;
    private int glMultitexUnit = 0;
    private Program curveProgram;

    private final BoundingBox bbox;
    private RBOQuad textureQuad;
    private FloatBuffer tCoords;

    public Text(Material material, int glMultitexUnit, boolean twoPass) {
        super(material, vertex_format.TRIANGLES);
        this.glMultitexUnit = glMultitexUnit;
        this.twoPass = twoPass;

        int numVertices = 0;

        VecF4[] points = new VecF4[numVertices];
        VecF2[] texCoords = new VecF2[numVertices];

        this.numVertices = numVertices;

        this.vertices = VectorFMath.toBuffer(points);

        this.bbox = new BoundingBox();
        this.tCoords = VectorFMath.toBuffer(texCoords);
    }

    public void setString(GL3 gl, Program program, TypecastFont font,
            String str, int fontSize, int textureWidth) {
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
            VecF4[] myVertices = new VecF4[vertices.size()];
            VecF2[] myTexCoords = new VecF2[vertices.size()];
            int i = 0;
            for (Vertex v : vertices) {
                VecF3 vec = new VecF3(v.getX(), v.getY(), v.getZ());
                bbox.resize(vec);

                myVertices[i] = new VecF4(vec, 1f);
                myTexCoords[i] = new VecF2(v.getTexCoord()[0],
                        v.getTexCoord()[1]);

                i++;
            }

            vbo.delete(gl);
            this.vertices = VectorFMath.toBuffer(myVertices);
            this.tCoords = VectorFMath.toBuffer(myTexCoords);
            this.numVertices = vertices.size();
            this.cachedString = str;

            GLSLAttrib vAttrib = new GLSLAttrib(this.vertices, "MCvertex",
                    GLSLAttrib.SIZE_FLOAT, 4);
            GLSLAttrib tAttrib = new GLSLAttrib(tCoords, "MCtexCoord",
                    GLSLAttrib.SIZE_FLOAT, 2);
            vbo = new VBO(gl, vAttrib, tAttrib);

            int textureHeight = (int) (((textureWidth * bbox.getHeight()) / bbox
                    .getWidth()) + 0.5f);

            // System.out.println("texture width: " + textureWidth);
            // System.out.println("texture height: " + textureHeight);

            // Create and initialize the shader
            try {
                VertexShader curveVS = new VertexShader(
                        "src/ibis/amuse/visualization/openglCommon/shaders/src/vs_curveShader.vp");
                curveVS.init(gl);
                FragmentShader curveFS = new FragmentShader(
                        "src/ibis/amuse/visualization/openglCommon/shaders/src/fs_curveShader.fp");
                curveFS.init(gl);
                curveProgram = new Program(curveVS, curveFS);
                curveProgram.init(gl);

                curveProgram.setUniform("RBOTexture", glMultitexUnit);
                curveProgram.setUniformVector("TextureSize", new VecF2(
                        textureWidth, textureHeight));
                curveProgram.setUniformVector("ColorStatic", new VecF3(1f, 1f,
                        1f));
                curveProgram.setUniform("Alpha", 1f);
                curveProgram.setUniform("Weight", 1f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (CompilationFailedException e) {
                e.printStackTrace();
            }

            // System.out.println("bbox center: " + bbox.getCenter());
            // System.out.println("bbox width: " + bbox.getWidth());
            // System.out.println("bbox height: " + bbox.getHeight());

            // Create a quad to render the new texture onto
            textureQuad = new RBOQuad(curveProgram, material, bbox.getWidth(),
                    bbox.getHeight(), bbox.getCenter());
            textureQuad.init(gl);

            // TODO: REMOVE
            // setString2(gl, font, str, fontSize);
            initialized = true;
        }
    }

    private void renderDirect(GL3 gl, MatF4 MVMatrix, MatF4 PMatrix) {
        MatF4 PMVMatrix = MVMatrix.mul(PMatrix);
        curveProgram.setUniformMatrix("PMVMatrix", PMVMatrix);

        try {
            curveProgram.use(gl);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        vbo.bind(gl);

        curveProgram.linkAttribs(gl, vbo.getAttribs());

        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
    }

    private void render2FBO(GL3 gl, MatF4 MVMatrix, int textureWidth) {
        // Render the whole scene to a texture using FBO's and RBO's
        if (fbo != null) {
            fbo.delete(gl);
        }

        int textureHeight = (int) (((textureWidth * bbox.getHeight()) / bbox
                .getWidth()) + 0.5f);

        fbo = new FBO(textureWidth, textureHeight, glMultitexUnit);
        fbo.init(gl);

        try {
            // Store the old viewport values and set the new viewport
            IntBuffer viewportVars = IntBuffer.allocate(4);
            gl.glGetIntegerv(GL3.GL_VIEWPORT, viewportVars);
            gl.glViewport(0, 0, textureWidth, textureHeight);

            // Bind the FBO and render
            fbo.bind(gl);

            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT | GL2ES2.GL_DEPTH_BUFFER_BIT);

            // MatF4 PMatrix = new MatF4();
            // System.out.println("Min:\n" + bbox.getMin());
            // System.out.println("Max:\n" + bbox.getMax());
            MatF4 PMatrix = MatrixFMath.ortho((int) bbox.getMin().get(0),
                    (int) bbox.getMax().get(0), (int) bbox.getMin().get(1),
                    (int) bbox.getMax().get(1), -1, 1);
            // System.out.println("PMatrix:\n" + PMVMatrix);

            renderDirect(gl, MVMatrix, PMatrix);

            fbo.unBind(gl);

            // Restore the old viewport
            gl.glViewport(viewportVars.get(0), viewportVars.get(1),
                    viewportVars.get(2), viewportVars.get(3));

        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void renderFBO(GL3 gl, MatF4 PMVMatrix) {
        textureQuad.draw(gl, curveProgram, PMVMatrix);
    }

    @Override
    public void draw(GL3 gl, Program program, MatF4 MVMatrix) {
        if (initialized) {
            MatF4 SMatrix = MatrixFMath.scale(0.02f);
            // MVMatrix = MVMatrix.mul(SMatrix);

            if (!twoPass) {
                renderDirect(gl, MVMatrix, new MatF4());
                // TODO DEBUG
                // if (iVBO != null) {
                // renderDirect2(gl, program, MVMatrix);
                // }
            } else {
                render2FBO(gl, new MatF4(), 1024);
                renderFBO(gl, MVMatrix);
            }
        }
    }

    @Override
    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex",
                    GLSLAttrib.SIZE_FLOAT, 4);
            GLSLAttrib tAttrib = new GLSLAttrib(tCoords, "MCtexCoord",
                    GLSLAttrib.SIZE_FLOAT, 2);
            vbo = new VBO(gl, vAttrib, tAttrib);
        }
        initialized = true;
    }

    public static MatF4 getModelViewForHUD(float RasterPosX, float RasterPosY) {
        MatF4 mv = new MatF4();
        mv = mv.mul(MatrixFMath.translate(-.55f + RasterPosX, -.35f
                + RasterPosY, -1f));

        return mv;
    }

}

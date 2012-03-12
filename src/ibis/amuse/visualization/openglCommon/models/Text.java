package ibis.amuse.visualization.openglCommon.models;

import ibis.amuse.visualization.openglCommon.FBO;
import ibis.amuse.visualization.openglCommon.GLSLAttrib;
import ibis.amuse.visualization.openglCommon.IndexedVBO;
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
import ibis.amuse.visualization.openglCommon.math.VectorSMath;
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
import java.nio.ShortBuffer;
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
    private final FloatBuffer tCoords;

    private IndexedVBO iVBO = null;

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

    public void setString2(GL3 gl, TypecastFont font, String str, int fontSize) {
        // if (str.compareTo(cachedString) != 0) {
        ArrayList<Short> indicesTxtList = new ArrayList<Short>();
        ArrayList<Float> verticesTxtList = new ArrayList<Float>();
        ArrayList<Float> texCoordsTxtList = new ArrayList<Float>();

        // Get the outline shapes for the current string in this font
        ArrayList<OutlineShape> shapes = font.getOutlineShapes(str, fontSize,
                SVertex.factory());

        // Make a set of glyph shapes from the outlines
        int numGlyps = shapes.size();
        ArrayList<GlyphShape> glyphs = new ArrayList<GlyphShape>();

        for (int index = 0; index < numGlyps; index++) {
            if (shapes.get(index) == null) {
                continue;
            }
            GlyphShape glyphShape = new GlyphShape(SVertex.factory(), shapes
                    .get(index));

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
        int i = 0;
        for (Vertex v : vertices) {
            VecF3 vec = new VecF3(v.getX(), v.getY(), v.getZ());
            bbox.resize(vec);

            indicesTxtList.add((short) (i * 3));
            indicesTxtList.add((short) (i * 3 + 1));
            indicesTxtList.add((short) (i * 3 + 2));

            verticesTxtList.add(v.getX());
            verticesTxtList.add(v.getY());
            verticesTxtList.add(v.getZ());

            final float[] tex = v.getTexCoord();
            texCoordsTxtList.add(tex[0]);
            texCoordsTxtList.add(tex[1]);

            i++;
        }

        vbo.delete(gl);
        this.numVertices = vertices.size();
        this.cachedString = str;

        ShortBuffer indicesTxt = VectorSMath.listToBuffer(indicesTxtList);
        FloatBuffer verticesTxt = VectorFMath.listToBuffer(verticesTxtList);
        FloatBuffer texCoordsTxt = VectorFMath.listToBuffer(texCoordsTxtList);

        GLSLAttrib iAttrib = new GLSLAttrib(indicesTxt, "MCindex",
                GLSLAttrib.SIZE_SHORT, 3);
        GLSLAttrib vAttrib = new GLSLAttrib(verticesTxt, "MCvertex",
                GLSLAttrib.SIZE_FLOAT, 3);
        GLSLAttrib tAttrib = new GLSLAttrib(texCoordsTxt, "MCtexCoord",
                GLSLAttrib.SIZE_FLOAT, 3);

        iVBO = new IndexedVBO(gl, iAttrib, vAttrib, tAttrib);
        // }
    }

    private void renderDirect2(GL3 gl, Program program, MatF4 MVMatrix) {
        program.setUniformVector("DiffuseMaterial", material.diffuse);
        program.setUniformVector("AmbientMaterial", material.ambient);
        program.setUniformVector("SpecularMaterial", material.specular);
        program.setUniformMatrix("MVMatrix", MVMatrix);

        try {
            program.use(gl);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        iVBO.bind(gl);

        program.linkAttribs(gl, iVBO.getAttribs());

        gl.glDrawElements(GL3.GL_TRIANGLES, numVertices, GL3.GL_UNSIGNED_SHORT,
                iVBO.getIndicesPointer());
    }

    public void setString(GL3 gl, Program program, TypecastFont font,
            String str, int fontSize, float width) {
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
            int i = 0;
            for (Vertex v : vertices) {
                VecF3 vec = new VecF3(v.getX(), v.getY(), v.getZ());
                bbox.resize(vec);

                myVertices[i++] = new VecF4(vec, 1f);
            }

            vbo.delete(gl);
            this.vertices = VectorFMath.toBuffer(myVertices);
            this.numVertices = vertices.size();
            this.cachedString = str;

            GLSLAttrib vAttrib = new GLSLAttrib(this.vertices, "MCvertex",
                    GLSLAttrib.SIZE_FLOAT, 4);
            GLSLAttrib tAttrib = new GLSLAttrib(tCoords, "MCtexCoord",
                    GLSLAttrib.SIZE_FLOAT, 2);
            vbo = new VBO(gl, vAttrib, tAttrib);

            int height = (int) (((width * bbox.getHeight()) / bbox.getWidth()) + 0.5f);

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
                curveProgram.setUniformVector("TextureSize", new VecF2(width,
                        height));
                curveProgram.setUniformVector("ColorStatic", new VecF3(1f, 1f,
                        1f));
                curveProgram.setUniform("Alpha", 1f);
                curveProgram.setUniform("Weight", 1f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (CompilationFailedException e) {
                e.printStackTrace();
            }

            // Create a quad to render the new texture onto
            try {
                float centerX = bbox.getMax().get(0) - bbox.getMin().get(0);
                float centerY = bbox.getMax().get(1) - bbox.getMin().get(1);
                float centerZ = bbox.getMin().get(2);

                textureQuad = new RBOQuad(curveProgram, material, width,
                        height, new VecF3(centerX, centerY, centerZ));
                textureQuad.init(gl);
            } catch (UninitializedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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

    private void render2FBO(GL3 gl, MatF4 MVMatrix) {
        // Render the whole scene to a texture using FBO's and RBO's
        if (fbo != null) {
            fbo.delete(gl);
        }
        int width = (int) textureQuad.getWidth();
        int height = (int) textureQuad.getHeight();
        fbo = new FBO(width, height, glMultitexUnit);
        fbo.init(gl);

        try {
            // Store the old viewport values and set the new viewport
            IntBuffer viewportVars = IntBuffer.allocate(4);
            gl.glGetIntegerv(GL3.GL_VIEWPORT, viewportVars);
            gl.glViewport(0, 0, width, height);

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

    private void renderFBO(GL3 gl, MatF4 MVMatrix) {
        MatF4 PMVMatrix = MVMatrix;
        curveProgram.setUniformMatrix("PMVMatrix", PMVMatrix);

        textureQuad.draw(gl, curveProgram, MVMatrix);
    }

    @Override
    public void draw(GL3 gl, Program program, MatF4 MVMatrix) {
        if (initialized) {
            if (!twoPass) {
                renderDirect(gl, MVMatrix, new MatF4());
                // TODO DEBUG
                // if (iVBO != null) {
                // renderDirect2(gl, program, MVMatrix);
                // }
            } else {
                render2FBO(gl, new MatF4());
                renderFBO(gl, MVMatrix);
            }
        }
    }

    @Override
    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex",
                    GLSLAttrib.SIZE_FLOAT, 4);
            vbo = new VBO(gl, vAttrib);
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

package ibis.amuse.visualization.openglCommon.models;

import ibis.amuse.visualization.openglCommon.GLSLAttrib;
import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.VBO;
import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;
import ibis.amuse.visualization.openglCommon.math.MatF4;
import ibis.amuse.visualization.openglCommon.shaders.Program;

import java.nio.FloatBuffer;

import javax.media.opengl.GL3;

public class Model {
    public static enum vertex_format {
        TRIANGLES, POINTS, LINES
    };

    public vertex_format format;
    public FloatBuffer vertices, normals, texCoords;
    public VBO vbo;
    public int numVertices;

    public Program program;
    public Material material;

    private boolean initialized = false;

    public Model(Program program, Material material, vertex_format format) {
        vertices = null;
        normals = null;
        texCoords = null;
        numVertices = 0;

        this.program = program;
        this.material = material;
        this.format = format;
    }

    public Model(Material material, vertex_format format) {
        vertices = null;
        normals = null;
        texCoords = null;
        numVertices = 0;

        this.material = material;
        this.format = format;
    }

    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex",
                    GLSLAttrib.SIZE_FLOAT, 4);
            GLSLAttrib nAttrib = new GLSLAttrib(normals, "MCnormal",
                    GLSLAttrib.SIZE_FLOAT, 3);
            GLSLAttrib tAttrib = new GLSLAttrib(texCoords, "MCtexCoord",
                    GLSLAttrib.SIZE_FLOAT, 3);

            vbo = new VBO(gl, vAttrib, nAttrib, tAttrib);
        }
        initialized = true;
    }

    public void delete(GL3 gl) {
        vertices = null;
        normals = null;
        texCoords = null;

        if (initialized) {
            vbo.delete(gl);
        }
    }

    public void draw(GL3 gl, MatF4 MVMatrix) {
        vbo.bind(gl);

        program.linkAttribs(gl, vbo.getAttribs());

        program.setUniformVector("DiffuseMaterial", material.diffuse);
        program.setUniformVector("AmbientMaterial", material.ambient);
        program.setUniformVector("SpecularMaterial", material.specular);

        program.setUniformMatrix("MVMatrix", MVMatrix);

        try {
            program.use(gl);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        if (format == vertex_format.TRIANGLES) {
            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
        } else if (format == vertex_format.POINTS) {
            gl.glDrawArrays(GL3.GL_POINTS, 0, numVertices);
        } else if (format == vertex_format.LINES) {
            gl.glDrawArrays(GL3.GL_LINES, 0, numVertices);
        }
    }

    public void draw(GL3 gl, Program program, MatF4 MVMatrix) {

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

        if (format == vertex_format.TRIANGLES) {
            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
        } else if (format == vertex_format.POINTS) {
            gl.glDrawArrays(GL3.GL_POINTS, 0, numVertices);
        } else if (format == vertex_format.LINES) {
            gl.glDrawArrays(GL3.GL_LINES, 0, numVertices);
        }
    }
}

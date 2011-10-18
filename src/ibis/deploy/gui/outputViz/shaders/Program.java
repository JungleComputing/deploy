package ibis.deploy.gui.outputViz.shaders;

import ibis.deploy.gui.outputViz.common.GLSLAttrib;
import ibis.deploy.gui.outputViz.common.math.Matrix;
import ibis.deploy.gui.outputViz.common.math.Vector;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class Program {
    public int pointer;
    private final VertexShader vs;
    private GeometryShader gs;
    private final FragmentShader fs;

    private final HashMap<String, FloatBuffer> uniformFloatMatrices;
    private final HashMap<String, FloatBuffer> uniformFloatVectors;
    private final HashMap<String, Integer> uniformInts;
    private final HashMap<String, Float> uniformFloats;

    private boolean geometry_enabled = false;

    public Program(VertexShader vs, FragmentShader fs) {
        pointer = 0;
        this.vs = vs;
        this.fs = fs;
        uniformFloatMatrices = new HashMap<String, FloatBuffer>();
        uniformFloatVectors = new HashMap<String, FloatBuffer>();
        uniformInts = new HashMap<String, Integer>();
        uniformFloats = new HashMap<String, Float>();
    }

    public Program(VertexShader vs, GeometryShader gs, FragmentShader fs) {
        pointer = 0;
        this.vs = vs;
        this.gs = gs;
        this.fs = fs;
        uniformFloatMatrices = new HashMap<String, FloatBuffer>();
        uniformFloatVectors = new HashMap<String, FloatBuffer>();
        uniformInts = new HashMap<String, Integer>();
        uniformFloats = new HashMap<String, Float>();

        geometry_enabled = true;
    }

    public int init(GL3 gl) {
        pointer = gl.glCreateProgram();

        try {
            gl.glAttachShader(pointer, vs.getShader());
            if (geometry_enabled) {
                gl.glAttachShader(pointer, gs.getShader());
            }
            gl.glAttachShader(pointer, fs.getShader());
        } catch (UninitializedException e) {
            System.out.println("Shaders not initialized properly");
            System.exit(0);
        }

        gl.glLinkProgram(pointer);

        // Check for errors
        IntBuffer buf = Buffers.newDirectIntBuffer(1);
        gl.glGetProgramiv(pointer, GL3.GL_LINK_STATUS, buf);
        if (buf.get(0) == 0) {
            System.err.print("Link error");
            printError(gl);
        }

        return pointer;
    }

    public void detachShaders(GL3 gl) {
        try {
            gl.glDetachShader(pointer, vs.getShader());
            gl.glDeleteShader(vs.getShader());

            if (geometry_enabled) {
                gl.glDetachShader(pointer, gs.getShader());
                gl.glDeleteShader(gs.getShader());
            }

            gl.glDetachShader(pointer, fs.getShader());
            gl.glDeleteShader(fs.getShader());
        } catch (UninitializedException e) {
            System.out.println("Shaders not initialized properly");
            System.exit(0);
        }
    }

    public void use(GL3 gl) throws UninitializedException {
        if (pointer == 0)
            throw new UninitializedException();

        for (Entry<String, FloatBuffer> var : uniformFloatMatrices.entrySet()) {
            passUniformMat(gl, var.getKey(), var.getValue());
        }
        for (Entry<String, FloatBuffer> var : uniformFloatVectors.entrySet()) {
            passUniformVec(gl, var.getKey(), var.getValue());
        }
        for (Entry<String, Integer> var : uniformInts.entrySet()) {
            passUniform(gl, var.getKey(), var.getValue());
        }
        for (Entry<String, Float> var : uniformFloats.entrySet()) {
            passUniform(gl, var.getKey(), var.getValue());
        }

        gl.glUseProgram(pointer);
    }

    public void linkAttribs(GL3 gl, GLSLAttrib... attribs) {
        int nextStart = 0;
        for (GLSLAttrib attrib : attribs) {
            int ptr = gl.glGetAttribLocation(pointer, attrib.name);
            gl.glVertexAttribPointer(ptr, attrib.vectorSize, GL3.GL_FLOAT,
                    false, 0, nextStart);
            gl.glEnableVertexAttribArray(ptr);

            nextStart += attrib.buffer.capacity() * Buffers.SIZEOF_FLOAT;
        }
    }

    private void printError(GL3 gl) {
        IntBuffer buf = Buffers.newDirectIntBuffer(1);
        gl.glGetProgramiv(pointer, GL3.GL_INFO_LOG_LENGTH, buf);
        int logLength = buf.get(0);
        ByteBuffer reason = ByteBuffer.wrap(new byte[logLength]);
        gl.glGetProgramInfoLog(pointer, logLength, null, reason);

        System.err.println(new String(reason.array()));
    }

    public void setUniformVector(String name, Vector var) {
        uniformFloatVectors.put(name, var.asBuffer());
    }

    public void setUniformMatrix(String name, Matrix var) {
        uniformFloatMatrices.put(name, var.asBuffer());
    }

    public void setUniform(String name, Integer var) {
        uniformInts.put(name, var);
    }

    public void setUniform(String name, Float var) {
        uniformFloats.put(name, var);
    }

    public void passUniformVec(GL3 gl, String pointerNameInShader,
            FloatBuffer var) {
        int ptr = gl.glGetUniformLocation(pointer, pointerNameInShader);

        int vecSize = var.capacity();
        if (vecSize == 1) {
            gl.glUniform1fv(ptr, 1, var);
        } else if (vecSize == 2) {
            gl.glUniform2fv(ptr, 1, var);
        } else if (vecSize == 3) {
            gl.glUniform3fv(ptr, 1, var);
        } else if (vecSize == 4) {
            gl.glUniform4fv(ptr, 1, var);
        }
    }

    public void passUniformMat(GL3 gl, String pointerNameInShader,
            FloatBuffer var) {
        int ptr = gl.glGetUniformLocation(pointer, pointerNameInShader);

        int matSize = var.capacity();
        if (matSize == 4) {
            gl.glUniformMatrix2fv(ptr, 1, true, var);
        } else if (matSize == 9) {
            gl.glUniformMatrix3fv(ptr, 1, true, var);
        } else if (matSize == 16) {
            gl.glUniformMatrix4fv(ptr, 1, true, var);
        }
    }

    private void passUniform(GL3 gl, String pointerNameInShader, int var) {
        int ptr = gl.glGetUniformLocation(pointer, pointerNameInShader);
        gl.glUniform1i(ptr, var);
    }

    private void passUniform(GL3 gl, String pointerNameInShader, float var) {
        int ptr = gl.glGetUniformLocation(pointer, pointerNameInShader);
        gl.glUniform1f(ptr, var);
    }

    public void delete(GL3 gl) {
        gl.glDeleteProgram(pointer);
    }
}

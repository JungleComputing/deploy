package ibis.amuse.visualization.openglCommon.shaders;


import ibis.amuse.visualization.openglCommon.GLSLAttrib;
import ibis.amuse.visualization.openglCommon.exceptions.CompilationFailedException;
import ibis.amuse.visualization.openglCommon.math.MatrixF;
import ibis.amuse.visualization.openglCommon.math.VectorF;

import java.io.FileNotFoundException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL3;


import com.jogamp.common.nio.Buffers;

public class ProgramLoader {
    HashMap<Integer, Program> programs;

    public ProgramLoader() {
        programs = new HashMap<Integer, Program>();
    }

    public Program createProgram(GL3 gl, String vs_src, String fs_src)
            throws FileNotFoundException, CompilationFailedException {
        VertexShader vs = new VertexShader(vs_src);
        vs.init(gl);
        FragmentShader fs = new FragmentShader(fs_src);
        fs.init(gl);

        Program program = new Program(vs, fs);
        int index = program.init(gl);
        programs.put(index, program);

        return program;
    }

    public Program createProgram(GL3 gl, String vs_src, String gs_src,
            String fs_src) throws FileNotFoundException,
            CompilationFailedException {
        VertexShader vs = new VertexShader(vs_src);
        vs.init(gl);
        GeometryShader gs = new GeometryShader(gs_src);
        gs.init(gl);
        FragmentShader fs = new FragmentShader(fs_src);
        fs.init(gl);

        Program program = new Program(vs, gs, fs);
        int index = program.init(gl);
        programs.put(index, program);

        return program;
    }

    public int createProgram(GL3 gl, VertexShader vs, FragmentShader fs) {
        Program program = new Program(vs, fs);
        int index = program.init(gl);
        programs.put(index, program);
        return index;
    }

    public void detachProgram(GL3 gl, int index) {
        programs.get(index).detachShaders(gl);
    }

    public void deleteProgram(GL3 gl, int index) {
        HashMap<Integer, Program> temp = new HashMap<Integer, Program>();
        for (Entry<Integer, Program> entry : programs.entrySet()) {
            if (entry.getKey() == index) {
                entry.getValue().detachShaders(gl);
                entry.getValue().delete(gl);
            } else {
                temp.put(entry.getKey(), entry.getValue());
            }
        }
        programs = temp;
    }

    public void cleanup(GL3 gl) {
        for (Entry<Integer, Program> entry : programs.entrySet()) {
            Program program = entry.getValue();
            program.detachShaders(gl);
            program.delete(gl);
        }
        programs.clear();
    }

    public Program getProgram(int id) {
        return programs.get(id);
    }

    public static void createVBO(GL3 gl, GLSLAttrib... attribs) {
        IntBuffer vboPointer = Buffers.newDirectIntBuffer(1);
        gl.glGenVertexArrays(1, vboPointer);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboPointer.get(0));

        int size = 0;
        for (GLSLAttrib attrib : attribs) {
            size += attrib.buffer.capacity() * Buffers.SIZEOF_FLOAT;
        }

        gl.glBufferData(GL3.GL_ARRAY_BUFFER, size, (Buffer) null,
                GL3.GL_STATIC_DRAW);

        int nextStart = 0;
        for (GLSLAttrib attrib : attribs) {
            gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, nextStart, attrib.buffer
                    .capacity()
                    * Buffers.SIZEOF_FLOAT, attrib.buffer);
            nextStart += attrib.buffer.capacity() * Buffers.SIZEOF_FLOAT;
        }
    }

    public void linkAttribs(GL3 gl, GLSLAttrib... attribs) {
        for (Map.Entry<Integer, Program> entry : programs.entrySet()) {
            Program p = entry.getValue();
            p.linkAttribs(gl, attribs);
        }
    }

    public void setUniformVector(String pointerNameInShader, VectorF var) {
        for (Map.Entry<Integer, Program> e : programs.entrySet()) {
            Program p = e.getValue();
            p.setUniformVector(pointerNameInShader, var);
        }
    }

    public void setUniformMatrix(String pointerNameInShader, MatrixF var) {
        for (Map.Entry<Integer, Program> e : programs.entrySet()) {
            Program p = e.getValue();
            p.setUniformMatrix(pointerNameInShader, var);
        }
    }

    public void setUniform(String pointerNameInShader, int var) {
        for (Map.Entry<Integer, Program> e : programs.entrySet()) {
            Program p = e.getValue();
            p.setUniform(pointerNameInShader, var);
        }
    }

    public void setUniform(String pointerNameInShader, float var) {
        for (Map.Entry<Integer, Program> e : programs.entrySet()) {
            Program p = e.getValue();
            p.setUniform(pointerNameInShader, var);
        }
    }

    public void setUniform(String pointerNameInShader, boolean var) {
        for (Map.Entry<Integer, Program> e : programs.entrySet()) {
            Program p = e.getValue();
            if (var) {
                p.setUniform(pointerNameInShader, 1);
            } else {
                p.setUniform(pointerNameInShader, 0);
            }
        }
    }
}

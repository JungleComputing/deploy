package ibis.amuse.visualization.openglCommon.shaders;

import ibis.amuse.visualization.openglCommon.exceptions.CompilationFailedException;
import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;
import java.util.Scanner;

import javax.media.opengl.GL3;


import com.jogamp.common.nio.Buffers;

public abstract class Shader {
    String filename;

    String[] source;
    int shader = -1; // This value is set by creating either a vertex or
                     // fragment shader.

    public Shader(String filename) throws FileNotFoundException {
        this.filename = filename;

        // Read file
        StringBuffer buf = new StringBuffer();
        File file = new File(filename);
        Scanner scan;
        scan = new Scanner(file);

        while (scan.hasNext()) {
            buf.append(scan.nextLine());
            buf.append("\n");
        }

        source = new String[] { buf.toString() };
    }

    public void init(GL3 gl) throws CompilationFailedException {
        gl.glShaderSource(shader, 1, source, (int[]) null, 0);
        gl.glCompileShader(shader);

        IntBuffer buf = Buffers.newDirectIntBuffer(1);
        gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, buf);
        int status = buf.get(0);
        if (status == GL3.GL_FALSE) {
            gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, buf);
            int logLength = buf.get(0);
            byte[] reason = new byte[logLength];
            gl.glGetShaderInfoLog(shader, logLength, null, 0, reason, 0);

            throw new CompilationFailedException("Compilation of " + filename + " failed, " + new String(reason));
        }
    }

    public int getShader() throws UninitializedException {
        if (shader == -1)
            throw new UninitializedException();
        return shader;
    }
}

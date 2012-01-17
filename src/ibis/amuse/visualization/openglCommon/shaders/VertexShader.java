package ibis.amuse.visualization.openglCommon.shaders;
import ibis.amuse.visualization.openglCommon.exceptions.CompilationFailedException;

import java.io.FileNotFoundException;

import javax.media.opengl.GL3;



public class VertexShader extends Shader {
    public VertexShader(String filename) throws FileNotFoundException {
        super(filename);
    }

    public void init(GL3 gl) throws CompilationFailedException {
        shader = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
        super.init(gl);
    }
}

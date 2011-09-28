package ibis.deploy.gui.outputViz.shaders;
import java.io.FileNotFoundException;

import javax.media.opengl.GL3;

import ibis.deploy.gui.outputViz.exceptions.CompilationFailedException;

public class VertexShader extends Shader {
    public VertexShader(String filename) throws FileNotFoundException {
        super(filename);
    }

    public void init(GL3 gl) throws CompilationFailedException {
        shader = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
        super.init(gl);
    }
}

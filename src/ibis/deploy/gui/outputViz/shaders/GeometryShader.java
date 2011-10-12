package ibis.deploy.gui.outputViz.shaders;

import ibis.deploy.gui.outputViz.exceptions.CompilationFailedException;

import java.io.FileNotFoundException;

import javax.media.opengl.GL3;

public class GeometryShader extends Shader {
    public GeometryShader(String filename) throws FileNotFoundException {
        super(filename);
    }

    @Override
    public void init(GL3 gl) throws CompilationFailedException {
        shader = gl.glCreateShader(GL3.GL_GEOMETRY_SHADER);
        // gl.glProgramParameteri(shader, GL3.GL_GEOMETRY_INPUT_TYPE,
        // GL3.GL_TRIANGLES);
        // gl.glProgramParameteri(shader, GL3.GL_GEOMETRY_OUTPUT_TYPE,
        // GL3.GL_TRIANGLE_STRIP);
        //
        // IntBuffer n = IntBuffer.allocate(1);
        // gl.glGetIntegerv(GL3.GL_MAX_GEOMETRY_OUTPUT_VERTICES, n);
        // gl.glProgramParameteri(shader, GL3.GL_GEOMETRY_VERTICES_OUT,
        // n.get(0));

        super.init(gl);
    }
}

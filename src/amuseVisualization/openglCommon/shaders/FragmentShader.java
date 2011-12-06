package amuseVisualization.openglCommon.shaders;

import java.io.FileNotFoundException;

import javax.media.opengl.GL3;

import amuseVisualization.openglCommon.exceptions.CompilationFailedException;

public class FragmentShader extends Shader {
    public FragmentShader(String filename) throws FileNotFoundException {
        super(filename);
    }

    @Override
    public void init(GL3 gl) throws CompilationFailedException {
        shader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        super.init(gl);
    }
}

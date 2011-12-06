package amuseVisualization.openglCommon.shaders;
import java.io.FileNotFoundException;

import javax.media.opengl.GL3;

import amuseVisualization.openglCommon.exceptions.CompilationFailedException;

import ibis.deploy.gui.outputViz.exceptions.*;

public class FragmentShader extends Shader {
    public FragmentShader(String filename) throws FileNotFoundException {
        super(filename);
    }

    public void init(GL3 gl) throws CompilationFailedException {
        shader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        super.init(gl);
    }
}

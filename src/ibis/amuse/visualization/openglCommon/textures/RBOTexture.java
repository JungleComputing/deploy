package ibis.amuse.visualization.openglCommon.textures;

import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class RBOTexture extends Texture2D {

    public RBOTexture(int width, int height, int glMultiTexUnit) {
        super(glMultiTexUnit);
        this.height = height;
        this.width = width;

        // pixelBuffer = ByteBuffer.allocate(width * height * 4);
    }

    private boolean checkNoError(GL gl, String exceptionMessage,
            boolean quietlyRemoveAllPreviousErrors) {
        int error = gl.glGetError();
        if (!quietlyRemoveAllPreviousErrors) {
            if (GL.GL_NO_ERROR != error) {
                System.err.println("GL ERROR(s) " + exceptionMessage + " : ");
                while (GL.GL_NO_ERROR != error) {
                    System.err.println(" GL Error 0x"
                            + Integer.toHexString(error));
                    error = gl.glGetError();
                }
                return false;
            }
        } else {
            while (GL.GL_NO_ERROR != error) {
                error = gl.glGetError();
            }
        }
        return true;
    }

    @Override
    public void init(GL3 gl) {
        gl.glActiveTexture(glMultiTexUnit);
        gl.glEnable(GL3.GL_TEXTURE_2D);

        // Create new texture pointer and bind it so we can manipulate it.
        pointer = Buffers.newDirectIntBuffer(1);
        gl.glGenTextures(1, pointer);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, pointer.get(0));

        // Wrap and mipmap parameters.
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER,
                GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER,
                GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S,
                GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T,
                GL3.GL_CLAMP_TO_EDGE);

        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, // Mipmap level.
                GL3.GL_RGBA8, // GL.GL_RGBA, // Internal Texel Format,
                width, height, 0, // Border
                GL3.GL_BGRA, // External format from image,
                GL3.GL_UNSIGNED_BYTE, null // Imagedata as ByteBuffer
                );

        // Unbind, now ready for use
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
    }

    @Override
    public void use(GL3 gl) throws UninitializedException {
        gl.glActiveTexture(glMultiTexUnit);
        gl.glEnable(GL3.GL_TEXTURE_2D);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, getPointer());
    }
}

package ibis.deploy.gui.outputViz.common;

import ibis.deploy.gui.outputViz.exceptions.UninitializedException;

import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class RBOTexture extends Texture2D {

    public RBOTexture(int width, int height, int gLMultiTexUnit) {
        super(gLMultiTexUnit);
        this.height = height;
        this.width = width;

        // pixelBuffer = ByteBuffer.allocate(width * height * 4);
    }

    @Override
    public void init(GL3 gl) {
        gl.glActiveTexture(gLMultiTexUnit);
        gl.glEnable(GL3.GL_TEXTURE_2D);

        // Create new texture pointer and bind it so we can manipulate it.

        pointer = Buffers.newDirectIntBuffer(1);
        gl.glGenTextures(1, pointer);

        gl.glBindTexture(GL3.GL_TEXTURE_2D, pointer.get(0));

        // Wrap.
        // gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S,
        // GL3.GL_CLAMP_TO_EDGE);
        // gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T,
        // GL3.GL_CLAMP_TO_EDGE);
        // gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_R,
        // GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);

        // gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);

        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, // Mipmap level.
                GL3.GL_RGBA, // GL.GL_RGBA, // Internal Texel Format,
                width, height, 0, // Border
                GL3.GL_RGBA, // External format from image,
                GL3.GL_UNSIGNED_BYTE, null // Imagedata as ByteBuffer
        );
    }

    @Override
    public void use(GL3 gl) throws UninitializedException {
        gl.glActiveTexture(gLMultiTexUnit);
        gl.glEnable(GL3.GL_TEXTURE_2D);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, getPointer());
    }
}

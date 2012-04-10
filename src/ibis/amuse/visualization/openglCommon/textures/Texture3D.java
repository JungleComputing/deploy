package ibis.amuse.visualization.openglCommon.textures;

import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class Texture3D extends Texture {
    protected IntBuffer  pointer;
    protected ByteBuffer image;
    public int           size;

    public Texture3D(int size, int gLMultiTexUnit) {
        super(gLMultiTexUnit);
        pointer = null;
        image = Buffers.newDirectByteBuffer(size * size * size * 4);
        this.size = size;

        // makePerlin3d(size);
    }

    public Texture3D(ByteBuffer image, int gLMultiTexUnit) {
        super(gLMultiTexUnit);
        pointer = null;
        this.image = image;
        this.size = (int) Math.cbrt(image.limit());
        System.out.println("Size: " + this.size);
    }

    public ByteBuffer getImage() {
        return image;
    }

    public void init(GL3 gl) {
        if (image == null)
            System.out.println("make first!");

        gl.glActiveTexture(glMultiTexUnit);
        gl.glEnable(GL3.GL_TEXTURE_3D);

        // Create new texture pointer and bind it so we can manipulate it.

        pointer = Buffers.newDirectIntBuffer(1);
        gl.glGenTextures(1, pointer);

        gl.glBindTexture(GL3.GL_TEXTURE_3D, pointer.get(0));

        // Wrap.
        gl.glTexParameteri(GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_MIRRORED_REPEAT);
        gl.glTexParameteri(GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_MIRRORED_REPEAT);
        gl.glTexParameteri(GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_WRAP_R, GL3.GL_MIRRORED_REPEAT);
        gl.glTexParameteri(GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);

        gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);

        gl.glTexImage3D(GL3.GL_TEXTURE_3D, 0, // Mipmap level.
                GL3.GL_RGBA, // GL.GL_RGBA, // Internal Texel Format,
                size, size, size, 0, // Border
                GL3.GL_RGBA, // External format from image,
                GL3.GL_BYTE, image // Imagedata as ByteBuffer
        );
    }

    public void delete(GL3 gl) {
        gl.glDeleteTextures(1, pointer);
    }

    public void use(GL3 gl) throws UninitializedException {
        gl.glActiveTexture(glMultiTexUnit);
        gl.glEnable(GL3.GL_TEXTURE_3D);
        gl.glBindTexture(GL3.GL_TEXTURE_3D, getPointer());
    }

    public int getPointer() throws UninitializedException {
        if (pointer == null)
            throw new UninitializedException();
        return pointer.get(0);
    }
}

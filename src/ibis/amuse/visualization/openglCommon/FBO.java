package ibis.amuse.visualization.openglCommon;


import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;
import ibis.amuse.visualization.openglCommon.textures.RBOTexture;
import ibis.amuse.visualization.openglCommon.textures.Texture2D;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;


public class FBO {
    private final IntBuffer fboPointer = IntBuffer.allocate(1);
    private final IntBuffer rboPointer = IntBuffer.allocate(1);
    private RBOTexture newTex;

    private boolean asRenderBuffer = false;

    private int width, height;

    public FBO(GL3 gl) {
        gl.glGenFramebuffers(1, fboPointer);
    }

    public FBO(GL3 gl, int width, int height, int glMultitexUnit) {
        asRenderBuffer = true;

        this.width = width;
        this.height = height;

        // Generate pointers
        gl.glGenFramebuffers(1, fboPointer);
        // gl.glGenRenderbuffers(1, rboPointer);

        // Bind the buffers, assign storage
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fboPointer.get(0));

        // Attach a new texture as color buffer
        try {
            newTex = new RBOTexture(width, height, glMultitexUnit);
            newTex.init(gl);
            gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, GL3.GL_TEXTURE_2D,
                    newTex.getPointer(), 0);

        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        // gl.glBindRenderbuffer(GL3.GL_RENDERBUFFER, rboPointer.get(0));
        // gl.glRenderbufferStorage(GL3.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT,
        // width, height);

        // Attach depth buffer
        // gl.glFramebufferRenderbuffer(GL3.GL_FRAMEBUFFER,
        // GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER,
        // rboPointer.get(0));

        // gl.glGenerateMipmap(GL3.GL_TEXTURE_2D);

        // Check for errors
        int status = gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
        if (status != GL3.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer not ready!");
            if (status == GL3.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
                System.err.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT.");
            }
            if (status == GL3.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS) {
                System.err.println("GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS.");
            }
            if (status == GL3.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
                System.err.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT.");
            }
            if (status == GL3.GL_FRAMEBUFFER_UNSUPPORTED) {
                System.err.println("GL_FRAMEBUFFER_UNSUPPORTED.");
            }
        }

        // Unbind. The FBO is now ready for use.
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
    }

    public void bind(GL3 gl) {
        // gl.glEnable(GL3.GL_TEXTURE_2D);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, fboPointer.get(0));

        // gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
        // gl.glDrawBuffer(GL3.GL_COLOR_ATTACHMENT0);

        // if (asRenderBuffer) {
        // gl.glBindRenderbuffer(GL3.GL_RENDERBUFFER, rboPointer.get(0));
        // }
    }

    public Texture2D getTexture() {
        return newTex;
    }

    public void useTexture(GL3 gl) {
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
        try {
            gl.glEnable(GL3.GL_TEXTURE_2D);
            gl.glBindTexture(GL3.GL_TEXTURE_2D, newTex.getPointer());
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    public ByteBuffer getPixels(GL3 gl) {
        ByteBuffer buf = ByteBuffer.allocate(width * height * 3);
        try {
            newTex.use(gl);
            gl.glGetTexImage(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGB, GL3.GL_UNSIGNED_BYTE, buf);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        // gl.glReadBuffer(GL3.GL_COLOR_ATTACHMENT0);
        // gl.glPixelStorei(GL3.GL_PACK_ALIGNMENT, 1);
        // gl.glReadPixels(0, 0, width, height, GL3.GL_RGB,
        // GL3.GL_UNSIGNED_BYTE, buf);
        return buf;
    }

    public void unBind(GL3 gl) {
        // gl.glBindRenderbuffer(GL3.GL_RENDERBUFFER, 0);
        gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
        // gl.glDrawBuffer(GL3.GL_BACK);
    }

    public void delete(GL3 gl) {
        if (asRenderBuffer) {
            gl.glDeleteRenderbuffers(1, rboPointer);
        }

        gl.glDeleteFramebuffers(1, fboPointer);
    }

    public IntBuffer getPointer() {
        return fboPointer;
    }
}

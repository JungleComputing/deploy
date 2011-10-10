package ibis.deploy.gui.outputViz.common;

import java.nio.IntBuffer;

import javax.media.opengl.GL3;

public class FBO {
        IntBuffer pointer;

        public FBO(GL3 gl) {
                gl.glGenFramebuffers(1, pointer);
        }

        public void bind(GL3 gl) {
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, pointer.get(0));
        }

        public void unBind(GL3 gl) {
                gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
        }

        public void delete(GL3 gl) {
                gl.glDeleteFramebuffers(1, pointer);
        }
}

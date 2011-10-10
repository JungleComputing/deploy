package ibis.deploy.gui.outputViz.common;

import java.nio.IntBuffer;

import javax.media.opengl.GL3;

public class RenderBuffer {
        IntBuffer pointer;

        public RenderBuffer(GL3 gl, int width, int height) {
                gl.glGenRenderbuffers(1, pointer);

        }

        public void bind(GL3 gl) {
                gl.glBindRenderbuffer(GL3.GL_FRAMEBUFFER, pointer.get(0));
        }

        public void unBind(GL3 gl) {
                gl.glBindRenderbuffer(GL3.GL_FRAMEBUFFER, 0);
        }

        public void delete(GL3 gl) {
                gl.glDeleteRenderbuffers(1, pointer);
        }
}

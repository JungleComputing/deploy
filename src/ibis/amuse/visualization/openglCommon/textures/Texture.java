package ibis.amuse.visualization.openglCommon.textures;

import javax.media.opengl.GL;

public class Texture {
    int gLMultiTexUnit;

    public Texture(int gLMultiTexUnit) {
        this.gLMultiTexUnit = gLMultiTexUnit;
    }

    public int getGLMultiTexUnit() {
        return gLMultiTexUnit;
    }

    public int getMultitexNumber() {
        int result = -1;

        switch (gLMultiTexUnit) {
        case GL.GL_TEXTURE0:
            result = 0;
            break;
        case GL.GL_TEXTURE1:
            result = 1;
            break;
        case GL.GL_TEXTURE2:
            result = 2;
            break;
        case GL.GL_TEXTURE3:
            result = 3;
            break;
        case GL.GL_TEXTURE4:
            result = 4;
            break;
        case GL.GL_TEXTURE5:
            result = 5;
            break;
        case GL.GL_TEXTURE6:
            result = 6;
            break;
        case GL.GL_TEXTURE7:
            result = 7;
            break;
        case GL.GL_TEXTURE8:
            result = 8;
            break;
        case GL.GL_TEXTURE9:
            result = 9;
            break;
        case GL.GL_TEXTURE10:
            result = 10;
            break;
        case GL.GL_TEXTURE11:
            result = 11;
            break;
        case GL.GL_TEXTURE12:
            result = 12;
            break;
        case GL.GL_TEXTURE13:
            result = 13;
            break;
        case GL.GL_TEXTURE14:
            result = 14;
            break;
        }

        return result;
    }
}

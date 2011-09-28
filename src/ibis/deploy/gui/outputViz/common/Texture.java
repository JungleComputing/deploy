package ibis.deploy.gui.outputViz.common;

import javax.media.opengl.GL;

public class Texture {
	public static int getGLMultiTexUnit(int input) {
		int result = -1;
		
		switch (input) {
			case  0 : result = GL.GL_TEXTURE0; break;
			case  1 : result = GL.GL_TEXTURE1; break;
			case  2 : result = GL.GL_TEXTURE2; break;
			case  3 : result = GL.GL_TEXTURE3; break;
			case  4 : result = GL.GL_TEXTURE4; break;
			case  5 : result = GL.GL_TEXTURE5; break;
			case  6 : result = GL.GL_TEXTURE6; break;
			case  7 : result = GL.GL_TEXTURE7; break;
			case  8 : result = GL.GL_TEXTURE8; break;
			case  9 : result = GL.GL_TEXTURE9; break;
			case 10 : result = GL.GL_TEXTURE10; break;
			case 11 : result = GL.GL_TEXTURE11; break;
			case 12 : result = GL.GL_TEXTURE12; break;
			case 13 : result = GL.GL_TEXTURE13; break;
			case 14 : result = GL.GL_TEXTURE14; break;
		}
		
		return result;
	}
}

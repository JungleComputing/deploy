package ibis.deploy.gui.outputViz.common;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import ibis.deploy.gui.outputViz.exceptions.UninitializedException;

import com.jogamp.common.nio.Buffers;


public class CubeMap {
	final int mipmaplevel = 0;
	
	private IntBuffer pointer;
	
	Texture2D texNegX;
	Texture2D texNegY;
	Texture2D texNegZ;
	Texture2D texPosX;
	Texture2D texPosY;
	Texture2D texPosZ;
	
	public CubeMap(String[] filenames) throws Exception {
		pointer = null;
		if (filenames.length != 6) throw new Exception();
		
//		texNegX = new Texture2D(filenames[0]);
//		texNegY = new Texture2D(filenames[1]);
//		texNegZ = new Texture2D(filenames[2]);
//		texPosX = new Texture2D(filenames[3]);
//		texPosY	= new Texture2D(filenames[4]);
//		texPosZ = new Texture2D(filenames[5]);		
	}
	
	public void init(GL3 gl) {
		gl.glEnable(GL3.GL_TEXTURE_CUBE_MAP);

		//gl.glTexEnvf(GL3.GL_TEXTURE_ENV, GL3.GL_TEXTURE_ENV_MODE, GL3.GL_MODULATE);
		
		gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);		
		pointer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, pointer);
		gl.glBindTexture(GL3.GL_TEXTURE_CUBE_MAP, pointer.get(0));
				
		gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT);
		gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT);
		gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_R, GL3.GL_REPEAT);
		gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
		gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
		
		gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_X, mipmaplevel, GL3.GL_RGBA,
				texPosX.getHeight(), texPosX.getWidth(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texPosX.getPixelBuffer());
		gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, mipmaplevel, GL3.GL_RGBA,
				texNegX.getHeight(), texNegX.getWidth(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texNegX.getPixelBuffer());
		gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, mipmaplevel, GL3.GL_RGBA,
				texPosY.getHeight(), texPosY.getWidth(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texPosY.getPixelBuffer());
		gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, mipmaplevel, GL3.GL_RGBA,
				texNegY.getHeight(), texNegY.getWidth(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texNegY.getPixelBuffer());
		gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, mipmaplevel, GL3.GL_RGBA,
				texPosZ.getHeight(), texPosZ.getWidth(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texPosZ.getPixelBuffer());
		gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, mipmaplevel, GL3.GL_RGBA,
				texNegZ.getHeight(), texNegZ.getWidth(), 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texNegZ.getPixelBuffer());
		/*
		gl.glTexGeni(GL3.GL_S, GL3.GL_TEXTURE_GEN_MODE, GL3.GL_NORMAL_MAP);
		gl.glTexGeni(GL3.GL_T, GL3.GL_TEXTURE_GEN_MODE, GL3.GL_NORMAL_MAP);
		gl.glTexGeni(GL3.GL_R, GL3.GL_TEXTURE_GEN_MODE, GL3.GL_NORMAL_MAP);
		
		gl.glEnable(GL3.GL_TEXTURE_GEN_S);
		gl.glEnable(GL3.GL_TEXTURE_GEN_T);
		gl.glEnable(GL3.GL_TEXTURE_GEN_R);
		*/		
	}
	
	public int getPointer() throws UninitializedException {
		if (pointer == null) throw new UninitializedException();
		return pointer.get(0);
	}
}

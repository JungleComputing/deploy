package ibis.deploy.gui.outputViz.common;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;


public class Texture2D {

	protected ByteBuffer pixelBuffer;
	protected int width, height;	
	protected IntBuffer pointer;

	public Texture2D() {
	}
	
	public void init(GL3 gl) {
		if (pixelBuffer == null) System.out.println("init first!");
		
		gl.glEnable(GL3.GL_TEXTURE_2D);	    	
    	
		// Create new texture pointer and bind it so we can manipulate it.
				
		pointer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, pointer);
				
		gl.glBindTexture(GL3.GL_TEXTURE_2D, pointer.get(0));

		// Wrap.	
//		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER );
//		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER );
//		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_R, GL3.GL_CLAMP_TO_BORDER );
		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
		gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
					
//		gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);	

		gl.glTexImage2D( 
				GL3.GL_TEXTURE_2D, 
				0, 						// Mipmap level.
				GL3.GL_RGBA,			// GL.GL_RGBA, // Internal Texel Format, 
				width, height,
				0, 						//Border
				GL3.GL_RGBA, 			// External format from image, 
				GL3.GL_UNSIGNED_BYTE, 
				pixelBuffer	 			// Imagedata as ByteBuffer
		);		
	}
	
	public void delete(GL3 gl) {
		gl.glDeleteTextures(1, pointer);
	}
	
	public void use(GL3 gl, int multiTexUnit) throws UninitializedException {
		if (pixelBuffer == null) System.out.println("init first!");
		gl.glActiveTexture(Texture.getGLMultiTexUnit(multiTexUnit));
		gl.glEnable(GL3.GL_TEXTURE_2D);	        	 
		gl.glBindTexture(GL3.GL_TEXTURE_2D, getPointer());
	}

	public void setPixelBuffer(ByteBuffer pixelBuffer) {
		this.pixelBuffer = pixelBuffer;
	}

	public ByteBuffer getPixelBuffer() {
		return pixelBuffer;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public int getPointer() throws UninitializedException {
		if (pointer == null) throw new UninitializedException();
		return pointer.get(0);
	}

}
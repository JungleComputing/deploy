package ibis.deploy.gui.outputViz.common;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;


public class PyroclasticVolumeTexture3D {
	private IntBuffer pointer;
	private ByteBuffer image;
	public int size;
	
	public PyroclasticVolumeTexture3D(int size) {
		pointer = null;
		image = Buffers.newDirectByteBuffer( size * size * size );
		this.size = size;
		
		//makePerlin3d(size);
	}
	
	public ByteBuffer getImage() {
		return image;
	}
	
	public void init(GL3 gl) {
		if (image == null) System.out.println("make first!");
		
		gl.glEnable(GL3.GL_TEXTURE_3D);	    	
    	
		// Create new texture pointer and bind it so we can manipulate it.
				
		pointer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, pointer);
				
		gl.glBindTexture(GL3.GL_TEXTURE_3D, pointer.get(0));

		// Wrap.	
		gl.glTexParameteri( GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER );
		gl.glTexParameteri( GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER );
		gl.glTexParameteri( GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_WRAP_R, GL3.GL_CLAMP_TO_BORDER );
		gl.glTexParameteri( GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
		gl.glTexParameteri( GL3.GL_TEXTURE_3D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
		
		gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);

		gl.glTexImage3D( 
				GL3.GL_TEXTURE_3D, 
				0, 								// Mipmap level.
				GL3.GL_LUMINANCE_FLOAT32_ATI,	// GL.GL_RGBA, // Internal Texel Format, 
				size, size, size,
				0, 								//Border
				GL3.GL_LUMINANCE_FLOAT32_ATI,	// External format from image, 
				GL3.GL_UNSIGNED_BYTE, 
				image		 					// Imagedata as ByteBuffer
		);		
	}
	
		
	public int getPointer() throws UninitializedException {
		if (pointer == null) throw new UninitializedException();
		return pointer.get(0);
	}
}

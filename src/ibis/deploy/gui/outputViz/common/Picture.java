package ibis.deploy.gui.outputViz.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class Picture {
	int width, height;
	byte[] frameBufferPixels;
	int[] imagePixels;
	BufferedImage image;
	
	public Picture(int width, int height) {
		this.width = width;
		this.height = height;
		
		frameBufferPixels = new byte[width*height*4];
		imagePixels = new int[height*width];
	}
	
	public void copyFrameBuffer(GL3 gl) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);		
		gl.glReadPixels(0, 0, width, height, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, ByteBuffer.wrap(frameBufferPixels));
		
		IntBuffer pixels = IntBuffer.wrap(imagePixels);
		ByteBuffer singlePixel = Buffers.newDirectByteBuffer(4);
		
		pixels.rewind();
		for (int j = 0; j < height; j++) {
		    for (int i = 0; i < width; i++) {
		    	singlePixel.rewind();
		    	singlePixel.put((byte)((frameBufferPixels[j * width + i] >> 24) & 0xff)); //alpha
		    	singlePixel.put((byte)((frameBufferPixels[j * width + i]      ) & 0xff)); //red
		    	singlePixel.put((byte)((frameBufferPixels[j * width + i] >>  8) & 0xff)); //green
		    	singlePixel.put((byte)((frameBufferPixels[j * width + i] >> 16) & 0xff)); //blue
		    	pixels.put(singlePixel.asIntBuffer());
		    }
		}
		
		pixels.rewind();
		image.setRGB(0, 0, width, height, imagePixels, 0, 1);
		
		try {
			File newFile = new File("C:/bla.rgb");
			newFile.setWritable(true);
			ImageIO.write(image, "ARGB", newFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

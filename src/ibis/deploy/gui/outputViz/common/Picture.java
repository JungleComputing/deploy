package ibis.deploy.gui.outputViz.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.media.opengl.GL3;
import com.jogamp.opengl.util.awt.Screenshot;

public class Picture {
	int width, height;
	byte[] frameBufferPixels;
	BufferedImage image;
	
	public Picture(int width, int height) {
		this.width = width;
		this.height = height;
		
		frameBufferPixels = new byte[width*height*4];
	}
	
	public void copyFrameBufferToFile(GL3 gl, String path, int currentFrame) {
		try {
			File newDir = new File(path+"screenshots");
			if (!newDir.exists()) newDir.mkdir();
			
			File newFile = new File(path+"screenshots/"+currentFrame+".png");	
			if (newFile.createNewFile()) {
				System.out.println("Writing screenshot: "+path+"screenshots/"+currentFrame+".png");
				Screenshot.writeToFile(newFile, width, height);
			}
		} catch (IOException e) {			
			System.err.println(e.getMessage());
		}
	}
}

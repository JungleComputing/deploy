package ibis.deploy.gui.outputViz;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.swing.ImageIcon;

public class ColorIcon extends ImageIcon {
	private static final long serialVersionUID = 152208824875341752L;
	
	private static final int WIDTH = 10;
	private static final int HEIGHT = 10;
	
	BufferedImage image;	
	
	public ColorIcon(Float[] color) {
		super();
		
		makeImage(color);
	}

	public ColorIcon(int i, int j, int k) {
		super();
		
		Float[] color = {(float)i, (float)j, (float)k};
		makeImage(color);
	}	
	
	private void makeImage(Float[] color) {
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		
		ByteBuffer outBuf = ByteBuffer.allocate(WIDTH*HEIGHT*4);
		outBuf.clear();
		
		for (int i=0; i< WIDTH*HEIGHT; i++) {
			outBuf.put((byte) 0xFF);
			outBuf.put((byte) (color[0]*255));
			outBuf.put((byte) (color[1]*255));
			outBuf.put((byte) (color[2]*255));
		}
		
		outBuf.rewind();

		int [] tmp = new int[WIDTH*HEIGHT];
		outBuf.asIntBuffer().get(tmp);
		image.setRGB(0, 0, WIDTH, HEIGHT, tmp, 0, 1);
		
		setImage(image);
	}
}

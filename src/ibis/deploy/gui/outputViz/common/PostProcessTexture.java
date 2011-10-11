package ibis.deploy.gui.outputViz.common;

import java.nio.ByteBuffer;

public class PostProcessTexture extends Texture2D {

    public PostProcessTexture(int width, int height, int gLMultiTexUnit) {
        super(gLMultiTexUnit);
        this.height = height;
        this.width = width;

        pixelBuffer = ByteBuffer.allocate(width * height * 4);
    }
}

package amuseVisualization.openglCommon.textures;

import java.nio.ByteBuffer;

public class PostProcessTexture extends Texture2D {

    public PostProcessTexture(int width, int height, int gLMultiTexUnit) {
        super(gLMultiTexUnit);
        this.height = height;
        this.width = width;

        pixelBuffer = ByteBuffer.allocate(width * height * 4);
    }
}

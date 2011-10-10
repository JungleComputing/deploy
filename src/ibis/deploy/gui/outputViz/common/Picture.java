package ibis.deploy.gui.outputViz.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL3;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.awt.Screenshot;

public class Picture {
    int width, height;
    byte[] frameBufferPixels;
    BufferedImage image;

    public Picture(int width, int height) {
        this.width = width;
        this.height = height;

        frameBufferPixels = new byte[width * height * 4];
    }

    public void copyFrameBufferToFile(GL3 gl, String path, String fileName) {
        File newDir = new File(path + "screenshots");
        if (!newDir.exists())
            newDir.mkdir();

        String bareName = path + "screenshots/" + fileName; // filename
        // without
        // extension

        File newFile = new File(bareName + ".png");
        try {
            int attempt = 1;
            while (newFile.exists()) {
                String newName = bareName + " (" + attempt + ").png";
                newFile = new File(newName);

                attempt++;
            }

            System.out.println("Writing screenshot: "
                    + newFile.getAbsolutePath());
            Screenshot.writeToFile(newFile, width, height);
        } catch (GLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

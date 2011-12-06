package amuseVisualization.openglCommon.textures;

public class Perlin3D extends Texture3D {
    public int size;

    public Perlin3D(int size, int gLMultiTexUnit) {
        super(size, gLMultiTexUnit);

        makePerlin3d(size);
    }

    private void makePerlin3d(int size) {
        System.out.print("Generating noise");
        Noise n = new Noise(4, size);
        System.out.println("done");

        image = n.image;
    }
}

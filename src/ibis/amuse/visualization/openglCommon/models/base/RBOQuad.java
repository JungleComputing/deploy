package ibis.amuse.visualization.openglCommon.models.base;

import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.math.VecF2;
import ibis.amuse.visualization.openglCommon.math.VecF3;
import ibis.amuse.visualization.openglCommon.math.VectorFMath;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.shaders.Program;

public class RBOQuad extends Model {
    float width;
    float height;

    public RBOQuad(Program program, Material material, float width,
            float height, VecF3 center) {
        super(program, material, vertex_format.TRIANGLES);

        makeQuad(material, width, height, center);
    }

    public RBOQuad(Material material, float width, float height, VecF3 center) {
        super(material, vertex_format.TRIANGLES);

        makeQuad(material, width, height, center);
    }

    private void makeQuad(Material material, float height, float width,
            VecF3 center) {
        this.width = width;
        this.height = height;

        int numVertices = 6;

        // VERTICES
        float lX = center.get(0) - 0.5f * width, hX = center.get(0) + 0.5f
                * width;
        float lY = center.get(1) - 0.5f * height, hY = center.get(1) + 0.5f
                * height;
        float Z = center.get(2);

        VecF3[] verticesArray = new VecF3[numVertices];
        verticesArray[0] = new VecF3(lX, lY, Z);
        verticesArray[1] = new VecF3(lX, hY, Z);
        verticesArray[2] = new VecF3(hX, lY, Z);
        verticesArray[3] = new VecF3(lX, hY, Z);
        verticesArray[4] = new VecF3(hX, hY, Z);
        verticesArray[5] = new VecF3(hX, lY, Z);

        // NORMALS
        VecF3[] normalsArray = new VecF3[numVertices];
        for (int i = 0; i < 6; i++) {
            normalsArray[i] = new VecF3(0, 0, -1);
        }

        // TEXTURE COORDINATES
        VecF2[] texCoordsArray = new VecF2[numVertices];
        texCoordsArray[0] = new VecF2(5f, 5f);
        texCoordsArray[1] = new VecF2(5f, 6f);
        texCoordsArray[2] = new VecF2(6f, 5f);
        texCoordsArray[3] = new VecF2(5f, 6f);
        texCoordsArray[4] = new VecF2(6f, 6f);
        texCoordsArray[5] = new VecF2(6f, 5f);

        this.numVertices = numVertices;
        this.vertices = VectorFMath.toBuffer(verticesArray);
        this.normals = VectorFMath.toBuffer(normalsArray);
        this.texCoords = VectorFMath.toBuffer(texCoordsArray);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}

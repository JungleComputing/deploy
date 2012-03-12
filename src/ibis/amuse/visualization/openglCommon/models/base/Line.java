package ibis.amuse.visualization.openglCommon.models.base;

import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.math.VecF3;
import ibis.amuse.visualization.openglCommon.math.VecF4;
import ibis.amuse.visualization.openglCommon.math.VectorFMath;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.shaders.Program;

public class Line extends Model {
    public Line(Program program, Material material, VecF3 start, VecF3 end) {
        super(program, material, vertex_format.LINES);

        int numVertices = 2;

        VecF4[] points = new VecF4[numVertices];
        VecF3[] normals = new VecF3[numVertices];
        VecF3[] tCoords = new VecF3[numVertices];

        points[0] = new VecF4(start, 1f);
        points[1] = new VecF4(end, 1f);

        normals[0] = VectorFMath.normalize(start).neg();
        normals[1] = VectorFMath.normalize(end).neg();

        tCoords[0] = new VecF3(0, 0, 0);
        tCoords[1] = new VecF3(1, 1, 1);

        this.numVertices = numVertices;
        this.vertices = VectorFMath.toBuffer(points);
        this.normals = VectorFMath.toBuffer(normals);
        this.texCoords = VectorFMath.toBuffer(tCoords);
    }

    public Line(Material material, VecF3 start, VecF3 end) {
        super(material, vertex_format.LINES);

        int numVertices = 2;

        VecF4[] points = new VecF4[numVertices];
        VecF3[] normals = new VecF3[numVertices];
        VecF3[] tCoords = new VecF3[numVertices];

        points[0] = new VecF4(start, 1f);
        points[1] = new VecF4(end, 1f);

        normals[0] = VectorFMath.normalize(start).neg();
        normals[1] = VectorFMath.normalize(end).neg();

        tCoords[0] = new VecF3(0, 0, 0);
        tCoords[1] = new VecF3(1, 1, 1);

        this.numVertices = numVertices;
        this.vertices = VectorFMath.toBuffer(points);
        this.normals = VectorFMath.toBuffer(normals);
        this.texCoords = VectorFMath.toBuffer(tCoords);
    }
}

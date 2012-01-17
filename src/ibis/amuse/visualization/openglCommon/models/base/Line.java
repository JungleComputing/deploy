package ibis.amuse.visualization.openglCommon.models.base;

import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.math.Vec3;
import ibis.amuse.visualization.openglCommon.math.Vec4;
import ibis.amuse.visualization.openglCommon.math.VectorMath;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.shaders.Program;

public class Line extends Model {
    public Line(Program program, Material material, Vec3 start, Vec3 end) {
        super(program, material, vertex_format.LINES);

        int numVertices = 2;

        Vec4[] points = new Vec4[numVertices];
        Vec3[] normals = new Vec3[numVertices];
        Vec3[] tCoords = new Vec3[numVertices];

        points[0] = new Vec4(start, 1f);
        points[1] = new Vec4(end, 1f);

        normals[0] = VectorMath.normalize(start).neg();
        normals[1] = VectorMath.normalize(end).neg();

        tCoords[0] = new Vec3(0, 0, 0);
        tCoords[1] = new Vec3(1, 1, 1);

        this.numVertices = numVertices;
        this.vertices = VectorMath.toBuffer(points);
        this.normals = VectorMath.toBuffer(normals);
        this.texCoords = VectorMath.toBuffer(tCoords);
    }

    public Line(Material material, Vec3 start, Vec3 end) {
        super(material, vertex_format.LINES);

        int numVertices = 2;

        Vec4[] points = new Vec4[numVertices];
        Vec3[] normals = new Vec3[numVertices];
        Vec3[] tCoords = new Vec3[numVertices];

        points[0] = new Vec4(start, 1f);
        points[1] = new Vec4(end, 1f);

        normals[0] = VectorMath.normalize(start).neg();
        normals[1] = VectorMath.normalize(end).neg();

        tCoords[0] = new Vec3(0, 0, 0);
        tCoords[1] = new Vec3(1, 1, 1);

        this.numVertices = numVertices;
        this.vertices = VectorMath.toBuffer(points);
        this.normals = VectorMath.toBuffer(normals);
        this.texCoords = VectorMath.toBuffer(tCoords);
    }
}

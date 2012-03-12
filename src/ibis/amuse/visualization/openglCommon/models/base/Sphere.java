package ibis.amuse.visualization.openglCommon.models.base;


import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.math.VecF3;
import ibis.amuse.visualization.openglCommon.math.VecF4;
import ibis.amuse.visualization.openglCommon.math.VectorFMath;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.shaders.Program;

import java.util.ArrayList;
import java.util.List;


public class Sphere extends Model {
    private static float X = 0.525731112119133606f;
    private static float Z = 0.850650808352039932f;

    static VecF3[] vdata = { new VecF3(-X, 0f, Z), new VecF3(X, 0f, Z), new VecF3(-X, 0f, -Z), new VecF3(X, 0f, -Z),
            new VecF3(0f, Z, X), new VecF3(0f, Z, -X), new VecF3(0f, -Z, X), new VecF3(0f, -Z, -X), new VecF3(Z, X, 0f),
            new VecF3(-Z, X, 0f), new VecF3(Z, -X, 0f), new VecF3(-Z, -X, 0f) };

    static int[][] tindices = { { 1, 4, 0 }, { 4, 9, 0 }, { 4, 5, 9 }, { 8, 5, 4 }, { 1, 8, 4 }, { 1, 10, 8 },
            { 10, 3, 8 }, { 8, 3, 5 }, { 3, 2, 5 }, { 3, 7, 2 }, { 3, 10, 7 }, { 10, 6, 7 }, { 6, 11, 7 },
            { 6, 0, 11 }, { 6, 1, 0 }, { 10, 1, 6 }, { 11, 0, 9 }, { 2, 11, 9 }, { 5, 2, 9 }, { 11, 2, 7 } };

    public Sphere(Program program, Material material, int ndiv, float radius, VecF3 center) {
        super(program, material, vertex_format.TRIANGLES);

        List<VecF3> points3List = new ArrayList<VecF3>();
        List<VecF3> normals3List = new ArrayList<VecF3>();
        List<VecF3> tCoords3List = new ArrayList<VecF3>();

        for (int i = 0; i < 20; i++) {
            makeVertices(points3List, normals3List, tCoords3List, vdata[tindices[i][0]], vdata[tindices[i][1]],
                    vdata[tindices[i][2]], ndiv, radius);
        }

        List<VecF4> pointsList = new ArrayList<VecF4>();

        for (int i = 0; i < points3List.size(); i++) {
            pointsList.add(new VecF4(points3List.get(i).add(center), 1f));
        }

        numVertices = pointsList.size();

        vertices = VectorFMath.vec4ListToBuffer(pointsList);
        normals = VectorFMath.vec3ListToBuffer(normals3List);
        texCoords = VectorFMath.vec3ListToBuffer(tCoords3List);
    }

    public Sphere(Material material, int ndiv, float radius, VecF3 center) {
        super(material, vertex_format.TRIANGLES);

        List<VecF3> points3List = new ArrayList<VecF3>();
        List<VecF3> normals3List = new ArrayList<VecF3>();
        List<VecF3> tCoords3List = new ArrayList<VecF3>();

        for (int i = 0; i < 20; i++) {
            makeVertices(points3List, normals3List, tCoords3List, vdata[tindices[i][0]], vdata[tindices[i][1]],
                    vdata[tindices[i][2]], ndiv, radius);
        }

        List<VecF4> pointsList = new ArrayList<VecF4>();

        for (int i = 0; i < points3List.size(); i++) {
            pointsList.add(new VecF4(points3List.get(i).add(center), 1f));
        }

        numVertices = pointsList.size();

        vertices = VectorFMath.vec4ListToBuffer(pointsList);
        normals = VectorFMath.vec3ListToBuffer(normals3List);
        texCoords = VectorFMath.vec3ListToBuffer(tCoords3List);
    }

    private void makeVertices(List<VecF3> pointsList, List<VecF3> normalsList, List<VecF3> tCoords3List, VecF3 a, VecF3 b,
            VecF3 c, int div, float r) {
        if (div <= 0) {
            VecF3 na = new VecF3(a);
            VecF3 nb = new VecF3(b);
            VecF3 nc = new VecF3(c);

            normalsList.add(na);
            normalsList.add(nb);
            normalsList.add(nc);

            VecF3 ra = a.clone().mul(r);
            VecF3 rb = b.clone().mul(r);
            VecF3 rc = c.clone().mul(r);

            pointsList.add(ra);
            pointsList.add(rb);
            pointsList.add(rc);

            tCoords3List.add(ra.add(new VecF3(r, r, r)).div(2 * r));// new
                                                                   // Vec3((ra.get(0)
                                                                   // + r) / (2
                                                                   // * r),
                                                                   // (ra.get(1)
                                                                   // + r) / (2
                                                                   // * r),
                                                                   // (ra.get(2)
                                                                   // + r) / (2
                                                                   // * r)));
            tCoords3List.add(new VecF3((rb.get(0) + r) / (2 * r), (rb.get(1) + r) / (2 * r), (rb.get(2) + r) / (2 * r)));
            tCoords3List.add(new VecF3((rc.get(0) + r) / (2 * r), (rc.get(1) + r) / (2 * r), (rc.get(2) + r) / (2 * r)));
        } else {
            VecF3 ab = new VecF3();
            VecF3 ac = new VecF3();
            VecF3 bc = new VecF3();

            for (int i = 0; i < 3; i++) {
                ab.set(i, (a.get(i) + b.get(i)));
                ac.set(i, (a.get(i) + c.get(i)));
                bc.set(i, (b.get(i) + c.get(i)));
            }

            ab = VectorFMath.normalize(ab);
            ac = VectorFMath.normalize(ac);
            bc = VectorFMath.normalize(bc);

            makeVertices(pointsList, normalsList, tCoords3List, a, ab, ac, div - 1, r);
            makeVertices(pointsList, normalsList, tCoords3List, b, bc, ab, div - 1, r);
            makeVertices(pointsList, normalsList, tCoords3List, c, ac, bc, div - 1, r);
            makeVertices(pointsList, normalsList, tCoords3List, ab, bc, ac, div - 1, r);
        }
    }
}

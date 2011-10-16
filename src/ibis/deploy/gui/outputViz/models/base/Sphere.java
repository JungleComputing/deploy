package ibis.deploy.gui.outputViz.models.base;

import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.math.Vec4;
import ibis.deploy.gui.outputViz.common.math.VectorMath;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.shaders.Program;

import java.util.ArrayList;
import java.util.List;

public class Sphere extends Model {
    private static float X = 0.525731112119133606f;
    private static float Z = 0.850650808352039932f;

    static Vec3[] vdata = { new Vec3(-X, 0f, Z), new Vec3(X, 0f, Z), new Vec3(-X, 0f, -Z), new Vec3(X, 0f, -Z),
            new Vec3(0f, Z, X), new Vec3(0f, Z, -X), new Vec3(0f, -Z, X), new Vec3(0f, -Z, -X), new Vec3(Z, X, 0f),
            new Vec3(-Z, X, 0f), new Vec3(Z, -X, 0f), new Vec3(-Z, -X, 0f) };

    static int[][] tindices = { { 1, 4, 0 }, { 4, 9, 0 }, { 4, 5, 9 }, { 8, 5, 4 }, { 1, 8, 4 }, { 1, 10, 8 },
            { 10, 3, 8 }, { 8, 3, 5 }, { 3, 2, 5 }, { 3, 7, 2 }, { 3, 10, 7 }, { 10, 6, 7 }, { 6, 11, 7 },
            { 6, 0, 11 }, { 6, 1, 0 }, { 10, 1, 6 }, { 11, 0, 9 }, { 2, 11, 9 }, { 5, 2, 9 }, { 11, 2, 7 } };

    public Sphere(Program program, Material material, int ndiv, float radius, Vec3 center) {
        super(program, material, vertex_format.TRIANGLES);

        List<Vec3> points3List = new ArrayList<Vec3>();
        List<Vec3> normals3List = new ArrayList<Vec3>();
        List<Vec3> tCoords3List = new ArrayList<Vec3>();

        for (int i = 0; i < 20; i++) {
            makeVertices(points3List, normals3List, tCoords3List, vdata[tindices[i][0]], vdata[tindices[i][1]],
                    vdata[tindices[i][2]], ndiv, radius);
        }

        List<Vec4> pointsList = new ArrayList<Vec4>();

        for (int i = 0; i < points3List.size(); i++) {
            pointsList.add(new Vec4(points3List.get(i).add(center), 1f));
        }

        numVertices = pointsList.size();

        vertices = VectorMath.vec4ListToBuffer(pointsList);
        normals = VectorMath.vec3ListToBuffer(normals3List);
        texCoords = VectorMath.vec3ListToBuffer(tCoords3List);
    }

    public Sphere(Material material, int ndiv, float radius, Vec3 center) {
        super(material, vertex_format.TRIANGLES);

        List<Vec3> points3List = new ArrayList<Vec3>();
        List<Vec3> normals3List = new ArrayList<Vec3>();
        List<Vec3> tCoords3List = new ArrayList<Vec3>();

        for (int i = 0; i < 20; i++) {
            makeVertices(points3List, normals3List, tCoords3List, vdata[tindices[i][0]], vdata[tindices[i][1]],
                    vdata[tindices[i][2]], ndiv, radius);
        }

        List<Vec4> pointsList = new ArrayList<Vec4>();

        for (int i = 0; i < points3List.size(); i++) {
            pointsList.add(new Vec4(points3List.get(i).add(center), 1f));
        }

        numVertices = pointsList.size();

        vertices = VectorMath.vec4ListToBuffer(pointsList);
        normals = VectorMath.vec3ListToBuffer(normals3List);
        texCoords = VectorMath.vec3ListToBuffer(tCoords3List);
    }

    private void makeVertices(List<Vec3> pointsList, List<Vec3> normalsList, List<Vec3> tCoords3List, Vec3 a, Vec3 b,
            Vec3 c, int div, float r) {
        if (div <= 0) {
            Vec3 na = new Vec3(a);
            Vec3 nb = new Vec3(b);
            Vec3 nc = new Vec3(c);

            normalsList.add(na);
            normalsList.add(nb);
            normalsList.add(nc);

            Vec3 ra = a.clone().mul(r);
            Vec3 rb = b.clone().mul(r);
            Vec3 rc = c.clone().mul(r);

            pointsList.add(ra);
            pointsList.add(rb);
            pointsList.add(rc);

            tCoords3List.add(ra.add(new Vec3(r, r, r)).div(2 * r));// new
                                                                   // Vec3((ra.get(0)
                                                                   // + r) / (2
                                                                   // * r),
                                                                   // (ra.get(1)
                                                                   // + r) / (2
                                                                   // * r),
                                                                   // (ra.get(2)
                                                                   // + r) / (2
                                                                   // * r)));
            tCoords3List.add(new Vec3((rb.get(0) + r) / (2 * r), (rb.get(1) + r) / (2 * r), (rb.get(2) + r) / (2 * r)));
            tCoords3List.add(new Vec3((rc.get(0) + r) / (2 * r), (rc.get(1) + r) / (2 * r), (rc.get(2) + r) / (2 * r)));
        } else {
            Vec3 ab = new Vec3();
            Vec3 ac = new Vec3();
            Vec3 bc = new Vec3();

            for (int i = 0; i < 3; i++) {
                ab.set(i, (a.get(i) + b.get(i)));
                ac.set(i, (a.get(i) + c.get(i)));
                bc.set(i, (b.get(i) + c.get(i)));
            }

            ab = VectorMath.normalize(ab);
            ac = VectorMath.normalize(ac);
            bc = VectorMath.normalize(bc);

            makeVertices(pointsList, normalsList, tCoords3List, a, ab, ac, div - 1, r);
            makeVertices(pointsList, normalsList, tCoords3List, b, bc, ab, div - 1, r);
            makeVertices(pointsList, normalsList, tCoords3List, c, ac, bc, div - 1, r);
            makeVertices(pointsList, normalsList, tCoords3List, ab, bc, ac, div - 1, r);
        }
    }
}

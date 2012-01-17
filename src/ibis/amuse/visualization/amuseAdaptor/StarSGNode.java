package ibis.amuse.visualization.amuseAdaptor;


import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.math.Mat4;
import ibis.amuse.visualization.openglCommon.math.MatrixMath;
import ibis.amuse.visualization.openglCommon.math.Vec3;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.models.StarModel;
import ibis.amuse.visualization.openglCommon.scenegraph.SGNode;
import ibis.amuse.visualization.openglCommon.shaders.Program;

import java.util.HashMap;

import javax.media.opengl.GL3;


public class StarSGNode extends SGNode {
    protected HashMap<Model, Material> materials;

    public StarSGNode() {
        this.materials = new HashMap<Model, Material>();
    }

    public StarSGNode(StarSGNode other) {
        this.children = other.children;
        this.materials = other.materials;
        this.models = other.models;
        this.TMatrix = other.TMatrix;
    }

    @Override
    public void setTranslation(Vec3 translation) {
        this.TMatrix = MatrixMath.translate(translation);
    }

    @Override
    public void draw(GL3 gl, Mat4 MVMatrix) {
        Mat4 newM = MVMatrix.mul(TMatrix);

        for (Model m : models) {
            m.material = materials.get(m);
            m.draw(gl, newM);
        }

        for (SGNode child : children) {
            child.draw(gl, newM);
        }
    }

    @Override
    public void draw(GL3 gl, Program program, Mat4 MVMatrix) {
        Mat4 newM = MVMatrix.mul(TMatrix);

        for (Model m : models) {
            m.material = materials.get(m);
            ((StarModel) m).draw(gl, program, newM);
        }

        for (SGNode child : children) {
            child.draw(gl, program, newM);
        }
    }

    public void setModel(Model model, Material mat) {
        models.clear();
        models.add(model);

        materials.clear();
        materials.put(model, mat);
    }
}

package ibis.amuse.visualization.amuseAdaptor;


import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.math.MatF4;
import ibis.amuse.visualization.openglCommon.math.MatrixFMath;
import ibis.amuse.visualization.openglCommon.math.VecF3;
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
    public void setTranslation(VecF3 translation) {
        this.TMatrix = MatrixFMath.translate(translation);
    }

    @Override
    public void draw(GL3 gl, MatF4 MVMatrix) {
        MatF4 newM = MVMatrix.mul(TMatrix);

        for (Model m : models) {
            m.material = materials.get(m);
            m.draw(gl, newM);
        }

        for (SGNode child : children) {
            child.draw(gl, newM);
        }
    }

    @Override
    public void draw(GL3 gl, Program program, MatF4 MVMatrix) {
        MatF4 newM = MVMatrix.mul(TMatrix);

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

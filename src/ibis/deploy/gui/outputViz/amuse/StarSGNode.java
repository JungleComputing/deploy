package ibis.deploy.gui.outputViz.amuse;

import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.math.Mat4;
import ibis.deploy.gui.outputViz.common.math.MatrixMath;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.scenegraph.SGNode;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.shaders.Program;

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
            m.draw(gl, program, newM);
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

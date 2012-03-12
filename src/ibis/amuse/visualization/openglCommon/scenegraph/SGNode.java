package ibis.amuse.visualization.openglCommon.scenegraph;


import ibis.amuse.visualization.openglCommon.math.MatF4;
import ibis.amuse.visualization.openglCommon.math.MatrixFMath;
import ibis.amuse.visualization.openglCommon.math.VecF3;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.shaders.Program;

import java.util.ArrayList;

import javax.media.opengl.GL3;


public class SGNode {
    protected MatF4 TMatrix;
    // protected Mat4 RMatrix;
    // protected Mat4 SMatrix;

    protected ArrayList<SGNode> children;

    protected ArrayList<Model> models;

    private boolean initialized = false;

    public SGNode() {
        TMatrix = new MatF4();
        // RMatrix = new Mat4();
        // SMatrix = new Mat4();

        children = new ArrayList<SGNode>();
        models = new ArrayList<Model>();
    }

    public void init(GL3 gl) {
        if (!initialized) {
            for (Model m : models) {
                m.init(gl);
            }

            for (SGNode child : children) {
                child.init(gl);
            }
        }

        initialized = true;
    }

    public void delete(GL3 gl) {
        for (Model m : models) {
            m.delete(gl);
        }

        for (SGNode child : children) {
            child.delete(gl);
        }
    }

    public void addChild(SGNode child) {
        children.add(child);
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public synchronized void setTranslation(VecF3 translation) {
        this.TMatrix = MatrixFMath.translate(translation);
    }

    public void translate(VecF3 translation) {
        this.TMatrix = TMatrix.mul(MatrixFMath.translate(translation));
    }

    public void rotate(float rotation, VecF3 axis) {
        this.TMatrix = TMatrix.mul(MatrixFMath.rotate(rotation, axis));
    }

    public void rotate(VecF3 rotation) {
        this.TMatrix = TMatrix.mul(MatrixFMath.rotationX(rotation.get(0)));
        this.TMatrix = TMatrix.mul(MatrixFMath.rotationY(rotation.get(1)));
        this.TMatrix = TMatrix.mul(MatrixFMath.rotationZ(rotation.get(2)));
    }

    public synchronized void draw(GL3 gl, MatF4 MVMatrix) {

        // newM = newM.mul(SMatrix);
        MatF4 newM = MVMatrix.mul(TMatrix);
        // newM = newM.mul(RMatrix);

        for (Model m : models) {
            m.draw(gl, newM);
        }

        for (SGNode child : children) {
            child.draw(gl, newM);
        }
    }

    public synchronized void draw(GL3 gl, Program program, MatF4 MVMatrix) {
        MatF4 newM = MVMatrix.mul(TMatrix);

        for (Model m : models) {
            m.draw(gl, program, newM);
        }

        for (SGNode child : children) {
            child.draw(gl, program, newM);
        }
    }

}

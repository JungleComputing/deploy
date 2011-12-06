package amuseVisualization.openglCommon.scenegraph;


import java.util.ArrayList;

import javax.media.opengl.GL3;

import amuseVisualization.openglCommon.math.Mat4;
import amuseVisualization.openglCommon.math.MatrixMath;
import amuseVisualization.openglCommon.math.Vec3;
import amuseVisualization.openglCommon.models.Model;
import amuseVisualization.openglCommon.shaders.Program;

public class SGNode {
    protected Mat4 TMatrix;
    // protected Mat4 RMatrix;
    // protected Mat4 SMatrix;

    protected ArrayList<SGNode> children;

    protected ArrayList<Model> models;

    private boolean initialized = false;

    public SGNode() {
        TMatrix = new Mat4();
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

    public synchronized void setTranslation(Vec3 translation) {
        this.TMatrix = MatrixMath.translate(translation);
    }

    public void translate(Vec3 translation) {
        this.TMatrix = TMatrix.mul(MatrixMath.translate(translation));
    }

    public void rotate(float rotation, Vec3 axis) {
        this.TMatrix = TMatrix.mul(MatrixMath.rotate(rotation, axis));
    }

    public void rotate(Vec3 rotation) {
        this.TMatrix = TMatrix.mul(MatrixMath.rotationX(rotation.get(0)));
        this.TMatrix = TMatrix.mul(MatrixMath.rotationY(rotation.get(1)));
        this.TMatrix = TMatrix.mul(MatrixMath.rotationZ(rotation.get(2)));
    }

    public synchronized void draw(GL3 gl, Mat4 MVMatrix) {

        // newM = newM.mul(SMatrix);
        Mat4 newM = MVMatrix.mul(TMatrix);
        // newM = newM.mul(RMatrix);

        for (Model m : models) {
            m.draw(gl, newM);
        }

        for (SGNode child : children) {
            child.draw(gl, newM);
        }
    }

    public synchronized void draw(GL3 gl, Program program, Mat4 MVMatrix) {
        Mat4 newM = MVMatrix.mul(TMatrix);

        for (Model m : models) {
            m.draw(gl, program, newM);
        }

        for (SGNode child : children) {
            child.draw(gl, program, newM);
        }
    }

}

package ibis.deploy.gui.outputViz.common.scenegraph;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import ibis.deploy.gui.outputViz.common.Mat4;
import ibis.deploy.gui.outputViz.common.MatrixMath;
import ibis.deploy.gui.outputViz.common.Model;
import ibis.deploy.gui.outputViz.common.Vec3;

public class SGNode {
	protected Mat4 TMatrix;
//	protected Mat4 RMatrix;
//	protected Mat4 SMatrix;
	
	protected ArrayList<SGNode> children;
	
	protected ArrayList<Model> models;
	
	public SGNode() {
		TMatrix = new Mat4();
//		RMatrix = new Mat4();
//		SMatrix = new Mat4();
		
		children = new ArrayList<SGNode>();
		models = new ArrayList<Model>();
	}
	
	public void init(GL3 gl) {		
		for (Model m : models) {
			m.init(gl);
		}
		
		for (SGNode child : children) {
			child.init(gl);
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
	
//	public void translate(Vec3 translation) {
//		this.TMatrix = TMatrix.mul(MatrixMath.translate(translation));
//	}
	
//	public void rotate(float rotation, Vec3 axis) {
//		this.RMatrix = RMatrix.mul(MatrixMath.rotate(rotation, axis));
//	}
	
	public synchronized void draw(GL3 gl, Mat4 MVMatrix) {
		
//		newM = newM.mul(SMatrix);
		Mat4 newM = MVMatrix.mul(TMatrix);
//		newM = newM.mul(RMatrix);		
		
		for (Model m : models) {
			m.draw(gl, newM);			
		}		
		
		for (SGNode child : children) {
			child.draw(gl, newM);
		}
	}
}

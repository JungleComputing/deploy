package ibis.deploy.gui.outputViz.amuse;

import java.util.HashMap;

import javax.media.opengl.GL3;

import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.math.Mat4;
import ibis.deploy.gui.outputViz.common.math.MatrixMath;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.scenegraph.SGNode;
import ibis.deploy.gui.outputViz.models.Model;

public class StarSGNode extends SGNode {
	protected HashMap<Model, Material> materials;
	
	int animationCounter = 0;
	Vec3 speedVec;
	
	public StarSGNode() {
		materials = new HashMap<Model, Material>();
		speedVec = new Vec3();
	}
	
	public void setTranslation(Vec3 translation) {
		this.TMatrix = MatrixMath.translate(translation);
	}
	
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

	public void setSpeedVec(Vec3 speedVec) {
		animationCounter = 0;
		this.speedVec = speedVec;		
	}

	public void setModel(Model model, Material mat) {
		models.clear();
		models.add(model);
		
		materials.clear();
		materials.put(model, mat);
	}
}

package ibis.deploy.gui.outputViz.hfd5reader;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import ibis.deploy.gui.outputViz.common.Mat4;
import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.MatrixMath;
import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.common.scenegraph.SGNode;
import ibis.deploy.gui.outputViz.models.Model;

public class ParticleNode extends SGNode {
	protected ArrayList<Material> materials;
	
	int animationCounter = 0;
	Vec3 speedVec;
	
	public ParticleNode() {
		materials = new ArrayList<Material>();
		speedVec = new Vec3();
	}
	
	public void setTranslation(Vec3 translation) {
		this.TMatrix = MatrixMath.translate(translation);
	}
	
	public void draw(GL3 gl, Mat4 MVMatrix) {
		Mat4 newM = MVMatrix.mul(TMatrix);	
		
		for (int i=0; i<models.size(); i++) {
			Model m = models.get(i);
			m.material = materials.get(i);
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
		materials.add(mat);
	}
}

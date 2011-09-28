package ibis.deploy.gui.outputViz.hfd5reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL3;

import ibis.deploy.gui.outputViz.GLWindow;
import ibis.deploy.gui.outputViz.common.Mat4;
import ibis.deploy.gui.outputViz.common.MatrixMath;
import ibis.deploy.gui.outputViz.common.Model;
import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.common.Vec4;

public class CubeNode {
	private int maxElements;
	private CubeNode ppp,ppn,pnp,pnn,npp,npn,nnp,nnn;
	
	private HashMap<Vec3, Double> elements;
	private int childCounter;
	
	private Vec3 center;
	private float cubeSize;
	private boolean subdivided = false;
	private boolean initialized = false;
	
	private Mat4 TMatrix;
//	private Mat4 RMatrix;
//	private Mat4 SMatrix;
	
	private int depth;
	private List<Model> models;
	private Model model;
	private Vec4 color;
	
	private double total_u;
	private float density;
	//private Program program;
	//private Material material;
	
	public CubeNode(int maxElements, int depth, List<Model> models, Vec3 corner, float halfSize) {
		this.maxElements = maxElements;
		this.cubeSize = halfSize;
		center = corner.add(new Vec3(halfSize, halfSize, halfSize));
		//System.out.println("Cube! center: "+center);
		
		TMatrix = MatrixMath.translate(center);
//		RMatrix = new Mat4();
//		SMatrix = new Mat4();		
		
		elements = new HashMap<Vec3, Double>();
		
		childCounter = 0;
		
		this.depth = depth;
		this.models = models;
		model = models.get(depth);
		
//		color = new Vec4(.6f,.3f,.3f,0f);
		total_u = 0.0;
		
		initialized = true;
	}
	
	public CubeNode() {
		// Dummy constructor
	}

	public void init(GL3 gl) {
		if (initialized) {
			model.init(gl);			
			
			if (subdivided) {
				ppp.init(gl);
				ppn.init(gl);
				pnp.init(gl);
				pnn.init(gl);
				npp.init(gl);
				npn.init(gl);
				nnp.init(gl);
				nnn.init(gl);
			}
		}
	}
	
	private void subDiv() {	
		float size = cubeSize/2f;
		ppp = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(       0f,       0f,       0f)), size);
		ppn = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(       0f,       0f,-cubeSize)), size);
		pnp = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(       0f,-cubeSize,       0f)), size);
		pnn = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(       0f,-cubeSize,-cubeSize)), size);
		npp = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(-cubeSize,       0f,       0f)), size);
		npn = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(-cubeSize,       0f,-cubeSize)), size);
		nnp = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(-cubeSize,-cubeSize,       0f)), size);
		nnn = new CubeNode(maxElements, depth+1, models, center.add(new Vec3(-cubeSize,-cubeSize,-cubeSize)), size);
		
		for (Map.Entry<Vec3, Double> element : elements.entrySet()) {
			addSubdivided(element.getKey(), element.getValue());
		}
		
		elements.clear();
		
		subdivided = true;
	}
	
	public void addGas(Vec3 location, double u) {
		if ((location.get(0) > center.get(0)-cubeSize) &&
				(location.get(1) > center.get(1)-cubeSize) &&
				(location.get(2) > center.get(2)-cubeSize) &&
				(location.get(0) < center.get(0)+cubeSize) &&
				(location.get(1) < center.get(1)+cubeSize) &&
				(location.get(2) < center.get(2)+cubeSize)) {
			if (childCounter > maxElements && !subdivided) {
				if (depth < GLWindow.MAX_CLOUD_DEPTH) {
					subDiv();
					total_u = 0.0;
				} else {
					System.out.println("Max division!");
				}
			} 
			if (subdivided) {
				addSubdivided(location, u);
			} else {
				elements.put(location, u);	
				total_u += u;
			}
			childCounter++;
		}
	}
	
	public void doneAddingGas() {
		elements.clear();
		
		if (subdivided) {
			ppp.doneAddingGas();
			ppn.doneAddingGas();
			pnp.doneAddingGas();
			pnn.doneAddingGas();
			npp.doneAddingGas();
			npn.doneAddingGas();
			nnp.doneAddingGas();
			nnn.doneAddingGas();
		} else {	
			density = (float) (childCounter/(cubeSize*cubeSize*cubeSize*6));
			float u = (float) (Math.sqrt(total_u / (float) childCounter) / 5000.0);
			if (Float.isNaN(u)) u = 0f;
			color = new Vec4(1f-u, 0f+u, 0f+u, density);
		}
	}
	
	public void addSubdivided(Vec3 location, double u) {
		if (location.get(0) < center.get(0)) {
			if (location.get(1) < center.get(1)) {
				if (location.get(2) < center.get(2)) {
					nnn.addGas(location, u);
				} else {
					nnp.addGas(location, u);
				}
			} else {
				if (location.get(2) < center.get(2)) {
					npn.addGas(location, u);
				} else {
					npp.addGas(location, u);
				}
			}
		} else {
			if (location.get(1) < center.get(1)) {
				if (location.get(2) < center.get(2)) {
					pnn.addGas(location, u);
				} else {
					pnp.addGas(location, u);
				}
			} else {
				if (location.get(2) < center.get(2)) {
					ppn.addGas(location, u);
				} else {
					ppp.addGas(location, u);
				}
			}
		}
	}
	
	public void draw(GL3 gl, Mat4 MVMatrix) {
		if (initialized) {			
			if (subdivided) {
				draw_sorted(gl, MVMatrix);
			} else {								
				if (density > GLWindow.EPSILON) {
					Mat4 newM = MVMatrix.mul(TMatrix);
					
					model.material.setColor(color);
					model.material.setTransparency(density * GLWindow.GAS_OPACITY_FACTOR);
					model.draw(gl, newM);
				}
			}
		}
	}
	
	private void draw_sorted(GL3 gl, Mat4 MVMatrix) {
		if (GLWindow.getCurrentOctant() == GLWindow.octants.NNN) {
			ppp.draw(gl, MVMatrix);
			
			npp.draw(gl, MVMatrix);
			pnp.draw(gl, MVMatrix);
			ppn.draw(gl, MVMatrix);
						
			nnp.draw(gl, MVMatrix);
			pnn.draw(gl, MVMatrix);
			npn.draw(gl, MVMatrix);
			
			nnn.draw(gl, MVMatrix);
		} else if (GLWindow.getCurrentOctant() == GLWindow.octants.NNP) {
			ppn.draw(gl, MVMatrix);
			
			npn.draw(gl, MVMatrix);
			pnn.draw(gl, MVMatrix);			
			ppp.draw(gl, MVMatrix);
			
			nnn.draw(gl, MVMatrix);
			pnp.draw(gl, MVMatrix);
			npp.draw(gl, MVMatrix);
			
			nnp.draw(gl, MVMatrix);
		} else if (GLWindow.getCurrentOctant() == GLWindow.octants.NPN) {
			pnp.draw(gl, MVMatrix);
			
			nnp.draw(gl, MVMatrix);
			ppp.draw(gl, MVMatrix);			
			pnn.draw(gl, MVMatrix);
			
			npp.draw(gl, MVMatrix);
			ppn.draw(gl, MVMatrix);
			nnn.draw(gl, MVMatrix);
			
			npn.draw(gl, MVMatrix);
		} else if (GLWindow.getCurrentOctant() == GLWindow.octants.NPP) {
			pnn.draw(gl, MVMatrix);
			
			nnn.draw(gl, MVMatrix);
			ppn.draw(gl, MVMatrix);			
			pnp.draw(gl, MVMatrix);
			
			npn.draw(gl, MVMatrix);
			ppp.draw(gl, MVMatrix);
			nnp.draw(gl, MVMatrix);
			
			npp.draw(gl, MVMatrix);
		} else if (GLWindow.getCurrentOctant() == GLWindow.octants.PNN) {
			npp.draw(gl, MVMatrix);
			
			ppp.draw(gl, MVMatrix);
			nnp.draw(gl, MVMatrix);			
			npn.draw(gl, MVMatrix);
			
			pnp.draw(gl, MVMatrix);
			nnn.draw(gl, MVMatrix);
			ppn.draw(gl, MVMatrix);
			
			pnn.draw(gl, MVMatrix);
		} else if (GLWindow.getCurrentOctant() == GLWindow.octants.PNP) {
			npn.draw(gl, MVMatrix);
			
			ppn.draw(gl, MVMatrix);
			nnn.draw(gl, MVMatrix);			
			npp.draw(gl, MVMatrix);
			
			pnn.draw(gl, MVMatrix);
			nnp.draw(gl, MVMatrix);
			ppp.draw(gl, MVMatrix);
			
			pnp.draw(gl, MVMatrix);
		} else if (GLWindow.getCurrentOctant() == GLWindow.octants.PPN) {
			nnp.draw(gl, MVMatrix);
			
			pnp.draw(gl, MVMatrix);
			npp.draw(gl, MVMatrix);			
			nnn.draw(gl, MVMatrix);
			
			ppp.draw(gl, MVMatrix);
			npn.draw(gl, MVMatrix);
			pnn.draw(gl, MVMatrix);
			
			ppn.draw(gl, MVMatrix);
		} else if (GLWindow.getCurrentOctant() == GLWindow.octants.PPP) {
			nnn.draw(gl, MVMatrix);
			
			pnn.draw(gl, MVMatrix);
			npn.draw(gl, MVMatrix);			
			nnp.draw(gl, MVMatrix);
			
			ppn.draw(gl, MVMatrix);
			npp.draw(gl, MVMatrix);
			pnp.draw(gl, MVMatrix);
			
			ppp.draw(gl, MVMatrix);
		}
	}
}

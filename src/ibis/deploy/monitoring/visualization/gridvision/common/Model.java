package ibis.deploy.monitoring.visualization.gridvision.common;

import java.nio.FloatBuffer;

public class Model {
	public FloatBuffer points, normals, colors;
	public int numVertices;
	
	public Model() {
		points  = null;
		normals = null;
		colors  = null;
		numVertices = 0;
	}
	
	public Model(int numVertices, FloatBuffer points, FloatBuffer colors) {
		this.points = points; 
		this.colors = colors;
		this.numVertices = numVertices;
	}
	
	public Model(int numVertices, FloatBuffer points, FloatBuffer normals, FloatBuffer colors) {
		this.points = points;
		this.normals = normals; 
		this.colors = colors;
		this.numVertices = numVertices;
	}
}

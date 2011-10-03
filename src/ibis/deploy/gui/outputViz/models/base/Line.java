package ibis.deploy.gui.outputViz.models.base;

import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.shaders.Program;

public class Line extends Model {	
	public Line (Program program, Material material, Vec3 start, Vec3 end) {
		super(program, material, vertex_format.LINES);
				
		int numVertices = 2;		
				
		Vec4[] points = new Vec4[numVertices];
		Vec3[] normals = new Vec3[numVertices];
		
		points[0] = new Vec4(start, 1f);		
		points[1] = new Vec4(end, 1f);
		
		normals[0] = VectorMath.normalize(start).neg();
		normals[1] = VectorMath.normalize(end).neg();
		
		this.numVertices = numVertices;
	    this.vertices  = Vec4.toBuffer(points);
	    this.normals  = Vec3.toBuffer(normals);
	}
}

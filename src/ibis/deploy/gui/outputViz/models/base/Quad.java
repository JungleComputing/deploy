package ibis.deploy.gui.outputViz.models.base;

import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.common.math.*;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.shaders.Program;

public class Quad extends Model {	
	public Quad (Program program, Material material, float height, float width, Vec3 center) {
		super(program, material, vertex_format.TRIANGLES);
		
		Point4[] vertices = makeVertices(height, width, center);
				
		int numVertices = 6;		
				
		Point4[] points = new Point4[numVertices];
		Vec3[] normals = new Vec3[numVertices];
						
		int arrayindex = 0;		
		for(int i=arrayindex;i<arrayindex+6;i++) { normals[i] = new Vec3(0,0,-1); }
		arrayindex = newQuad(points, arrayindex, vertices, 1, 0, 3, 2 ); //FRONT
		
		this.numVertices = numVertices;
	    this.vertices  = VectorMath.toBuffer(points);
	    this.normals = VectorMath.toBuffer(normals);
	}
	
	private Point4[] makeVertices(float height, float width, Vec3 center) {
		float x = center.get(0);
		float y = center.get(1);
		
		float xpos = x + width/2f;
		float xneg = x - width/2f;
		float ypos = y + height/2f;
		float yneg = y - height/2f;		
		
		Point4[] result = new Point4[] {
				new Point4(xneg, yneg, 0.0f, 1.0f),
				new Point4(xneg, ypos, 0.0f, 1.0f),
				new Point4(xpos, ypos, 0.0f, 1.0f),
				new Point4(xpos, yneg, 0.0f, 1.0f)
		};
		
		return result;
	}
	
	private int newQuad(Point4[] points, int arrayindex, Point4[] source, int a, int b, int c, int d ) {
		points[arrayindex] = source[a]; arrayindex++;
	    points[arrayindex] = source[b]; arrayindex++;
	    points[arrayindex] = source[c]; arrayindex++;
	    points[arrayindex] = source[a]; arrayindex++;
	    points[arrayindex] = source[c]; arrayindex++;
	    points[arrayindex] = source[d]; arrayindex++;
	    
	    return arrayindex;
    }	
}

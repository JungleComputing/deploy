package ibis.deploy.gui.outputViz.models.base;

import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.shaders.Program;

public class Rectangle extends Model {	
	public Rectangle (Program program, Material material, float height, float width, float depth, Vec3 center, boolean bottom) {
		super(program, material, vertex_format.TRIANGLES);
		Point4[] vertices = makeVertices(height, width, depth, center);
				
		int numVertices;
		if (bottom) {
			numVertices = 36;		
		} else {
			numVertices = 30;
		}
		
		Point4[] points = new Point4[numVertices];
		Vec3[] normals = new Vec3[numVertices];
						
		int arrayindex = 0;		
		for(int i=arrayindex;i<arrayindex+6;i++) { normals[i] = new Vec3(0,0,-1); }
		arrayindex = newQuad(points, arrayindex, vertices, 1, 0, 3, 2 ); //FRONT
		
		for(int i=arrayindex;i<arrayindex+6;i++) { normals[i] = new Vec3(1,0,0); }
		arrayindex = newQuad(points, arrayindex, vertices, 2, 3, 7, 6 ); //RIGHT
		
		if (bottom) {
			for(int i=arrayindex;i<arrayindex+6;i++) { normals[i] = new Vec3(0,-1,0); }
			arrayindex = newQuad(points, arrayindex, vertices, 3, 0, 4, 7 ); //BOTTOM
		}
		
		for(int i=arrayindex;i<arrayindex+6;i++) { normals[i] = new Vec3(0,1,0); }
		arrayindex = newQuad(points, arrayindex, vertices, 6, 5, 1, 2 ); //TOP
		
		for(int i=arrayindex;i<arrayindex+6;i++) { normals[i] = new Vec3(0,0,1); }
		arrayindex = newQuad(points, arrayindex, vertices, 4, 5, 6, 7 ); //BACK
		
		for(int i=arrayindex;i<arrayindex+6;i++) { normals[i] = new Vec3(-1,0,0); }
		arrayindex = newQuad(points, arrayindex, vertices, 5, 4, 0, 1 ); //LEFT		
		
		this.numVertices = numVertices;
	    this.vertices  = Vec4.toBuffer(points);
	    this.normals = Vec3.toBuffer(normals);
	}
	
	private Point4[] makeVertices(float height, float width, float depth, Vec3 center) {
		float x = center.get(0);
		float y = center.get(1);
		float z = center.get(2);
		
		float xpos = x + width/2f;
		float xneg = x - width/2f;
		float ypos = y + height/2f;
		float yneg = y - height/2f;		
		float zpos = z + depth/2f;
		float zneg = z - depth/2f;		
		
		Point4[] result = new Point4[] {
				new Point4(xneg, yneg, zpos, 1.0f),
				new Point4(xneg, ypos, zpos, 1.0f),
				new Point4(xpos, ypos, zpos, 1.0f),
				new Point4(xpos, yneg, zpos, 1.0f),
				new Point4(xneg, yneg, zneg, 1.0f),
				new Point4(xneg, ypos, zneg, 1.0f),
				new Point4(xpos, ypos, zneg, 1.0f),
				new Point4(xpos, yneg, zneg, 1.0f)	
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

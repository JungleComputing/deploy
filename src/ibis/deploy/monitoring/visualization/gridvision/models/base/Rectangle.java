package ibis.deploy.monitoring.visualization.gridvision.models.base;

import ibis.deploy.monitoring.visualization.gridvision.common.*;

public class Rectangle extends Model {	
	public Rectangle (float height, float width, float depth, Point4 center, Color4 color, boolean bottom) {		
		Point4[] vertices = makeVertices(height, width, depth, center);
				
		int numVertices;
		if (bottom) {
			numVertices = 36;		
		} else {
			numVertices = 30;
		}
		
		Point4[] points = new Point4[numVertices];
		Color4[] colors = new Color4[numVertices];
						
		int arrayindex = 0;		
		arrayindex = newQuad(points, colors, arrayindex, vertices, color, 1, 0, 3, 2 ); //FRONT
		arrayindex = newQuad(points, colors, arrayindex, vertices, color, 2, 3, 7, 6 ); //RIGHT
		if (bottom) {
			arrayindex = newQuad(points, colors, arrayindex, vertices, color, 3, 0, 4, 7 ); //BOTTOM
		}
		arrayindex = newQuad(points, colors, arrayindex, vertices, color, 6, 5, 1, 2 ); //TOP
		arrayindex = newQuad(points, colors, arrayindex, vertices, color, 4, 5, 6, 7 ); //BACK
		arrayindex = newQuad(points, colors, arrayindex, vertices, color, 5, 4, 0, 1 ); //LEFT		
		
		this.numVertices = numVertices;
	    this.points  = Vec4.toBuffer(points);
	    this.colors  = Vec4.toBuffer(colors);
	}
	
	private Point4[] makeVertices(float height, float width, float depth, Point4 center) {
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
	
	private int newQuad(Point4[] points, Color4[] colors, int arrayindex, Point4[] source, Color4 color, int a, int b, int c, int d ) {
		colors[arrayindex] = color; points[arrayindex] = source[a]; arrayindex++;
	    colors[arrayindex] = color; points[arrayindex] = source[b]; arrayindex++;
	    colors[arrayindex] = color; points[arrayindex] = source[c]; arrayindex++;
	    colors[arrayindex] = color; points[arrayindex] = source[a]; arrayindex++;
	    colors[arrayindex] = color; points[arrayindex] = source[c]; arrayindex++;
	    colors[arrayindex] = color; points[arrayindex] = source[d]; arrayindex++;
	    
	    return arrayindex;
    }	
}

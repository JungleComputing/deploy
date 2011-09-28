package ibis.deploy.monitoring.visualization.gridvision.models.base;

import ibis.deploy.monitoring.visualization.gridvision.common.*;

import java.util.ArrayList;
import java.util.List;

public class Sphere extends Model {
	private static float X = 0.525731112119133606f;
	private static float Z = 0.850650808352039932f;

	static Point4[] vdata = {
		new Point4(-X, 0f,  Z, 1f),		
	    new Point4(-X, 0f,  Z, 1f), 
	    new Point4( X, 0f,  Z, 1f), 
	    new Point4(-X, 0f, -Z, 1f), 
	    new Point4( X, 0f, -Z, 1f),    
	    new Point4(0f,  Z,  X, 1f), 
	    new Point4(0f,  Z, -X, 1f), 
	    new Point4(0f, -Z,  X, 1f), 
	    new Point4(0f, -Z, -X, 1f),    
	    new Point4( Z,  X,  0f, 1f), 
	    new Point4(-Z,  X,  0f, 1f), 
	    new Point4( Z, -X,  0f, 1f), 
	    new Point4(-Z, -X,  0f, 1f) 
	};
	
	static int[][] tindices = { 
	    {0 , 4, 1}, 
	    {0 , 9, 4}, 
	    {9 , 5, 4}, 
	    {4 , 5, 8}, 
	    {4 , 8, 1},    
	    {8 ,10, 1}, 
	    {8 , 3,10}, 
	    {5 , 3, 8}, 
	    {5 , 2, 3}, 
	    {2 , 7, 3},    
	    {7 ,10, 3}, 
	    {7 , 6,10}, 
	    {7 ,11, 6},
	    {11, 0, 6}, 
	    { 0, 1, 6}, 
	    { 6, 1,10},
	    { 9, 0,11}, 
	    { 9,11, 2},
	    { 9, 2, 5},
	    { 7, 2,11}
	};
	
	public Sphere(int ndiv, float radius, Color4 color) {		
		List<Point4> pointsList = new ArrayList<Point4>();
		List<Point4> normalsList = new ArrayList<Point4>();
		List<Color4> colorsList = new ArrayList<Color4>();		

	    for (int i=0;i<20;i++) {
	    	makeVertices(pointsList,normalsList, vdata[tindices[i][0]], vdata[tindices[i][1]], vdata[tindices[i][2]], ndiv, radius);
	    }

	    for (int i=0;i<pointsList.size();i++) {
	    	colorsList.add(color);
	    }
	    
	    numVertices = pointsList.size();
	    points  = Vec4.toBuffer((Vec4[]) pointsList.toArray());
	    normals = Vec4.toBuffer((Vec4[]) normalsList.toArray());
	    colors  = Vec4.toBuffer((Vec4[]) colorsList.toArray());	
	}

	private void makeVertices(List<Point4> pointsList, List<Point4> normalsList, Point4 a, Point4 b, Point4 c, int div, float r) {		
	    if (div<=0) {
	    	Point4 na = (Point4) a.clone();
	    	Point4 nb = (Point4) b.clone();
	    	Point4 nc = (Point4) c.clone();
	    	na.set(3, 0f);
	    	nb.set(3, 0f);
	    	nc.set(3, 0f);
	    	
	    	normalsList.add(na); 
	    	normalsList.add(nb);
	    	normalsList.add(nc);
	    	
	    	pointsList.add((Point4) a.mul(r));
	    	pointsList.add((Point4) b.mul(r));
	    	pointsList.add((Point4) c.mul(r));
	    } else {
	    	Point4 ab = new Point4(0f,0f,0f,1f); 
	    	Point4 ac = new Point4(0f,0f,0f,1f);
	    	Point4 bc = new Point4(0f,0f,0f,1f);
	    	
	        for (int i=0;i<3;i++) {
	            ab.set(i, (a.get(i)+b.get(i))/2f);
	            ac.set(i, (a.get(i)+c.get(i))/2f);
	            bc.set(i, (b.get(i)+c.get(i))/2f);	            
	        } 
	        ab.set(3, 1f);
	        ac.set(3, 1f);
	        bc.set(3, 1f);
	        
	        ab = (Point4) VectorMath.normalize(ab);
	        ac = (Point4) VectorMath.normalize(ac);
	        bc = (Point4) VectorMath.normalize(bc);
	        	        
	        makeVertices(pointsList, normalsList, a, ab, ac, div-1, r);
	        makeVertices(pointsList, normalsList, b, bc, ab, div-1, r);
	        makeVertices(pointsList, normalsList, c, ac, bc, div-1, r);
	        makeVertices(pointsList, normalsList, ab, bc, ac, div-1, r);  //<--Comment this line and sphere looks really cool!
	    }  
	}
}

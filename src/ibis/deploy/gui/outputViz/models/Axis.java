package ibis.deploy.gui.outputViz.models;

import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.shaders.Program;

public class Axis extends Model {	
	public Axis (Program program, Material material, Vec3 start, Vec3 end, float majorInterval, float minorInterval) {
		super(program, material, vertex_format.LINES);
		
		float length = VectorMath.length(end.sub(start));
		int numMajorIntervals = (int) Math.floor(length / majorInterval);
		int numMinorIntervals = (int) Math.floor(length / minorInterval);
				
		int numVertices = 2 + (numMajorIntervals*2) - 4 + (numMinorIntervals*2) - 4;		
				
		Point4[] points = new Point4[numVertices];
		Vec3[] normals = new Vec3[numVertices];
		
		int arrayindex = 0;
		points[0] = new Point4(start, 1f);
		points[1] = new Point4(end, 1f);
		
		normals[0] = VectorMath.normalize(start).neg();
		normals[1] = VectorMath.normalize(end).neg();
		
		arrayindex += 2;
		
		Vec3 perpendicular;
		Vec3 vec = VectorMath.normalize((end.sub(start)));
		if (vec.get(0) > 0.5f) {
			perpendicular = new Vec3(0f,0f,1f);
		} else if (vec.get(1) > 0.5f) {
			perpendicular = new Vec3(1f,0f,0f);
		} else {
			perpendicular = new Vec3(1f,0f,0f);
		}
		
		Vec3 nil = new Vec3();
		
		for (int i=1; i< numMajorIntervals/2; i++) {
			arrayindex = addInterval(points, normals, arrayindex, nil.add(vec.mul(majorInterval*i)), perpendicular, 2f);
			arrayindex = addInterval(points, normals, arrayindex, nil.sub(vec.mul(majorInterval*i)), perpendicular, 2f);
		}	
		
		for (int i=1; i< numMinorIntervals/2; i++) {
			arrayindex = addInterval(points, normals, arrayindex, nil.add(vec.mul(minorInterval*i)), perpendicular, .5f);
			arrayindex = addInterval(points, normals, arrayindex, nil.sub(vec.mul(minorInterval*i)), perpendicular, .5f);
		
		}	
		
		this.numVertices = numVertices;
	    this.vertices  = Vec4.toBuffer(points);
	    this.normals  = Vec3.toBuffer(normals);
	}
	
	private int addInterval(Point4[] points, Vec3[] normals, int arrayindex, Vec3 center, Vec3 alignment, float size) {
		points[arrayindex] = new Point4(center.add(alignment.mul(size)), 1f);
		normals[arrayindex] = VectorMath.normalize(alignment);
		arrayindex++;
	    points[arrayindex] = new Point4(center.sub(alignment.mul(size)), 1f);
	    normals[arrayindex] = VectorMath.normalize(alignment).neg();
	    arrayindex++;
		return arrayindex;
	}
}
package ibis.deploy.gui.outputViz.models.base;

import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.common.math.*;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.shaders.Program;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL3;

public class Sphere extends Model {
	private static float X = 0.525731112119133606f;
	private static float Z = 0.850650808352039932f;
	
	private float radius;

	static Vec3[] vdata = {
		new Vec3(-X, 0f,  Z),
		new Vec3( X, 0f,  Z), 
		new Vec3(-X, 0f, -Z),
		new Vec3( X, 0f, -Z),	        
	    new Vec3(0f,  Z,  X), 
	    new Vec3(0f,  Z, -X), 
	    new Vec3(0f, -Z,  X), 
	    new Vec3(0f, -Z, -X),    
	    new Vec3( Z,  X,  0f), 
	    new Vec3(-Z,  X,  0f), 
	    new Vec3( Z, -X,  0f), 
	    new Vec3(-Z, -X,  0f) 
	};
	
	static int[][] tindices = {
		{1, 4, 0},
		{4, 9, 0},
		{4, 5, 9},
		{8, 5, 4},
		{1, 8, 4},
		{1, 10, 8},
		{10, 3, 8},
		{8, 3, 5},
		{3, 2, 5},
		{3, 7, 2},
		{3, 10, 7},
		{10, 6, 7},
		{6, 11, 7},
		{6, 0, 11},
		{6, 1, 0},
		{10, 1, 6},
		{11, 0, 9},
		{2, 11, 9},
		{5, 2, 9},
		{11, 2, 7}
	};
	
	public Sphere(Program program, Material material, int ndiv, float radius, Vec3 center) {
		super(program, material, vertex_format.TRIANGLES);
		
		this.radius = radius;
		
		List<Vec3> points3List = new ArrayList<Vec3>();
		List<Vec3> normals3List = new ArrayList<Vec3>();
	
		for (int i=0;i<20;i++) {
	    	makeVertices(points3List, normals3List, vdata[tindices[i][0]], vdata[tindices[i][1]], vdata[tindices[i][2]], ndiv, radius);
	    }
		
		List<Vec4> pointsList = new ArrayList<Vec4>();

	    for (int i=0;i<points3List.size();i++) {
	    	pointsList.add(new Vec4(points3List.get(i).add(center), 1f));
	    }
	    
	    numVertices = pointsList.size();
	    
	    vertices  = VectorMath.vec4ListToBuffer(pointsList);
	    normals = VectorMath.vec3ListToBuffer(normals3List);
	}

	private void makeVertices(List<Vec3> pointsList, List<Vec3> normalsList, Vec3 a, Vec3 b, Vec3 c, int div, float r) {		
	    if (div<=0) {
	    	Vec3 na = new Vec3(a);
	    	Vec3 nb = new Vec3(b);
	    	Vec3 nc = new Vec3(c);
	    	
	    	normalsList.add(na); 
	    	normalsList.add(nb);
	    	normalsList.add(nc);
	    	
	    	Vec3 ra = a.clone().mul(r);
	    	Vec3 rb = b.clone().mul(r);
	    	Vec3 rc = c.clone().mul(r);
	    	
	    	pointsList.add(ra);
	    	pointsList.add(rb);
	    	pointsList.add(rc);
	    } else {
	    	Vec3 ab = new Vec3(); 
	    	Vec3 ac = new Vec3();
	    	Vec3 bc = new Vec3();
	    	
	        for (int i=0;i<3;i++) {
	            ab.set(i, (a.get(i)+b.get(i)));
	            ac.set(i, (a.get(i)+c.get(i)));
	            bc.set(i, (b.get(i)+c.get(i)));	            
	        }
	        
	        ab = VectorMath.normalize(ab);
	        ac = VectorMath.normalize(ac);
	        bc = VectorMath.normalize(bc);
	        	        
	        makeVertices(pointsList, normalsList, a, ab, ac, div-1, r);
	        makeVertices(pointsList, normalsList, b, bc, ab, div-1, r);
	        makeVertices(pointsList, normalsList, c, ac, bc, div-1, r);
	        makeVertices(pointsList, normalsList, ab, bc, ac, div-1, r);  //<--Comment this line and sphere looks really cool!
	    }  
	}
	
	public void draw(GL3 gl, Mat4 MVMatrix) {
		vbo.bind(gl);
			
    	program.linkAttribs(gl, vbo.getAttribs());
    	
    	program.setUniformVector("DiffuseMaterial", material.diffuse.asBuffer());
    	program.setUniformVector("AmbientMaterial", material.ambient.asBuffer());
    	program.setUniformVector("SpecularMaterial", material.specular.asBuffer());
    	
    	program.setUniform("NoiseScale", 1f/radius); 
    	
    	program.setUniformVector("Color", material.ambient.asBuffer());
    	
    	program.setUniformMatrix("MVMatrix", MVMatrix.asBuffer());
    	
		try {
			program.use(gl);
		} catch (UninitializedException e) {
			e.printStackTrace();
		}    	
		
    	if (format == vertex_format.TRIANGLES) {
//    		gl.glDrawElements(GL3.GL_TRIANGLES, 3, GL3.GL_FLOAT, 0);
    		gl.glDrawArrays( GL3.GL_TRIANGLES, 0, numVertices );
    	} else if (format == vertex_format.POINTS) {
    		gl.glDrawArrays( GL3.GL_POINTS, 0, numVertices );
    	} else if (format == vertex_format.LINES) {
    		gl.glDrawArrays( GL3.GL_LINES, 0, numVertices );
    	}
	}
}

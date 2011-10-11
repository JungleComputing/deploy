package ibis.deploy.gui.outputViz.models;

import ibis.deploy.gui.outputViz.common.GLSLAttrib;
import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.VBO;
import ibis.deploy.gui.outputViz.common.math.Mat4;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;
import ibis.deploy.gui.outputViz.shaders.Program;

import java.nio.FloatBuffer;

import javax.media.opengl.GL3;

public class Model {	
	public static enum vertex_format { TRIANGLES, POINTS, LINES };
	public vertex_format format; 
	public FloatBuffer vertices, normals, texCoords;	
	public VBO vbo;
	public int numVertices;
	
	public Program program;
	public Material material;
	
	private boolean initialized = false;
	
	public Model(Program program, Material material, vertex_format format) {
		vertices  = null;
		normals = null;
		texCoords = null;	
		numVertices = 0;
		
		this.program = program;
		this.material = material;
		this.format = format;
	}
	
	public void init(GL3 gl) {
		if (!initialized) {
			GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex", 4);
			GLSLAttrib nAttrib = new GLSLAttrib(normals, "MCnormal", 3);
			GLSLAttrib tAttrib = new GLSLAttrib(texCoords, "MCtexCoord", 3);
			
			vbo = new VBO(gl, vAttrib, nAttrib, tAttrib);
		}
		initialized = true;
	}
	
	public void delete(GL3 gl) {
		vertices  = null;
		normals = null;
		texCoords = null;
		
		if (initialized) {
			vbo.delete(gl);
		}
	}
	
	public void draw(GL3 gl, Mat4 MVMatrix) {
		vbo.bind(gl);
					
    	program.linkAttribs(gl, vbo.getAttribs());
    	
    	program.setUniformVector("DiffuseMaterial", material.diffuse.asBuffer());
    	program.setUniformVector("AmbientMaterial", material.ambient.asBuffer());
    	program.setUniformVector("SpecularMaterial", material.specular.asBuffer());
    	    	
    	program.setUniformVector("Color", material.ambient.asBuffer());
    	
    	program.setUniformMatrix("MVMatrix", MVMatrix.asBuffer());
    	
		try {
			program.use(gl);
		} catch (UninitializedException e) {
			e.printStackTrace();
		}    	
		
    	if (format == vertex_format.TRIANGLES) {
    		gl.glDrawArrays( GL3.GL_TRIANGLES, 0, numVertices );
    	} else if (format == vertex_format.POINTS) {
    		gl.glDrawArrays( GL3.GL_POINTS, 0, numVertices );
    	} else if (format == vertex_format.LINES) {
    		gl.glDrawArrays( GL3.GL_LINES, 0, numVertices );
    	}
	}
}

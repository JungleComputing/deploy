package amuseVisualization.openglCommon;

import java.nio.Buffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class VBO {
	private IntBuffer vboPointer;
	
	private GLSLAttrib[] attribs;

	public VBO(GL3 gl, GLSLAttrib... attribs) {		
		this.attribs = attribs;
		
		vboPointer = Buffers.newDirectIntBuffer(1);
		gl.glGenVertexArrays(1, vboPointer);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboPointer.get(0));
		
		int size = 0;
		for(GLSLAttrib attrib : attribs) {
			size += attrib.buffer.capacity() * Buffers.SIZEOF_FLOAT;
		}
		
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, size, (Buffer) null, GL3.GL_STATIC_DRAW);
		
		int nextStart = 0;
		for(GLSLAttrib attrib : attribs) {
			gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, nextStart, attrib.buffer.capacity() * Buffers.SIZEOF_FLOAT, attrib.buffer);
			nextStart += attrib.buffer.capacity() * Buffers.SIZEOF_FLOAT;
		}
	}
	
	public void bind(GL3 gl) {
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboPointer.get(0));
	}
	
	public void delete(GL3 gl) {
		gl.glDeleteVertexArrays(1, vboPointer);
	}
	
	public GLSLAttrib[] getAttribs() {
		return attribs;
	}
}
